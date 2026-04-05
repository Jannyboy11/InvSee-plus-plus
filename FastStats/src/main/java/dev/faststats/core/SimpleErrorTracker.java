package dev.faststats.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SimpleErrorTracker implements ErrorTracker {
    private final Map<String, Integer> collected = new ConcurrentHashMap<>();
    private final Map<String, JsonObject> reports = new ConcurrentHashMap<>();

    private final Map<Class<? extends Throwable>, Set<Pattern>> ignoredTypedPatterns = new ConcurrentHashMap<>();
    private final Set<Class<? extends Throwable>> ignoredTypes = new CopyOnWriteArraySet<>();
    private final Set<Pattern> ignoredPatterns = new CopyOnWriteArraySet<>();

    private volatile /*@Nullable*/ BiConsumer</*@Nullable*/ ClassLoader, Throwable> errorEvent = null;
    private volatile /*@Nullable*/ UncaughtExceptionHandler originalHandler = null;

    @Override
    public void trackError(final String message) {
        trackError(message, true);
    }

    @Override
    public void trackError(final Throwable error) {
        trackError(error, true);
    }

    @Override
    public void trackError(final String message, final boolean handled) {
        trackError(new RuntimeException(message), handled);
    }

    @Override
    public void trackError(final Throwable error, final boolean handled) {
        try {
            if (isIgnored(error, Collections.newSetFromMap(new IdentityHashMap<>()))) return;
            final JsonObject compiled = ErrorHelper.compile(error, null, handled);
            final String hashed = MurmurHash3.hash(compiled);
            if (collected.compute(hashed, (k, v) -> {
                return v == null ? 1 : v + 1;
            }) > 1) return;
            reports.put(hashed, compiled);
        } catch (final NoClassDefFoundError ignored) {
        }
    }

    private boolean isIgnored(/*@Nullable*/ final Throwable error, final Set<Throwable> visited) {
        if (error == null || !visited.add(error)) return false;

        if (ignoredTypes.contains(error.getClass())) return true;

        final String message = error.getMessage() != null ? error.getMessage() : "";
        if (ignoredPatterns.stream().map(pattern -> pattern.matcher(message)).anyMatch(Matcher::find)) return true;

        final Set<Pattern> patterns = ignoredTypedPatterns.get(error.getClass());
        if (patterns != null && patterns.stream().map(pattern -> pattern.matcher(message)).anyMatch(Matcher::find))
            return true;

        return isIgnored(error.getCause(), visited);
    }

    @Override
    public ErrorTracker ignoreErrorType(final Class<? extends Throwable> type) {
        ignoredTypes.add(type);
        return this;
    }

    @Override
    public ErrorTracker ignoreError(final Pattern pattern) {
        ignoredPatterns.add(pattern);
        return this;
    }

    @Override
    public ErrorTracker ignoreError(final Class<? extends Throwable> type, final Pattern pattern) {
        ignoredTypedPatterns.computeIfAbsent(type, k -> new CopyOnWriteArraySet<>()).add(pattern);
        return this;
    }

    public JsonArray getData(final String buildId) {
        final JsonArray report = new JsonArray(reports.size());

        reports.forEach((hash, object) -> {
            final JsonObject copy = object.deepCopy();
            copy.addProperty("hash", hash);
            copy.addProperty("buildId", buildId);
            final Integer count = collected.getOrDefault(hash, 1);
            if (count > 1) copy.addProperty("count", count);
            report.add(copy);
        });

        collected.forEach((hash, count) -> {
            if (count <= 0 || reports.containsKey(hash)) return;
            final JsonObject entry = new JsonObject();

            entry.addProperty("hash", hash);
            if (count > 1) entry.addProperty("count", count);

            report.add(entry);
        });

        return report;
    }

    public void clear() {
        collected.replaceAll((k, v) -> 0);
        reports.clear();
    }

    public boolean needsFlushing() {
        if (!reports.isEmpty()) return true;
        for (final Integer value : collected.values()) {
            if (value > 0) return true;
        }
        return false;
    }

    @Override
    public synchronized void attachErrorContext(/*@Nullable*/ final ClassLoader loader) throws IllegalStateException {
        if (originalHandler != null) throw new IllegalStateException("Error context already attached");
        originalHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            final Thread.UncaughtExceptionHandler handler = originalHandler;
            if (handler != null) handler.uncaughtException(thread, error);
            try {
                if (loader != null && !ErrorTracker.isSameLoader(loader, error)) return;
                final BiConsumer<ClassLoader, Throwable> event = errorEvent;
                if (event != null) event.accept(loader, error);
                trackError(error, false);
            } catch (final Throwable t) {
                trackError(t, false);
            }
        });
    }

    @Override
    public synchronized void detachErrorContext() {
        if (originalHandler == null) return;
        Thread.setDefaultUncaughtExceptionHandler(originalHandler);
        originalHandler = null;
    }

    @Override
    public synchronized boolean isContextAttached() {
        return originalHandler != null;
    }

    @Override
    public synchronized void setContextErrorHandler(/*@Nullable*/ final BiConsumer</*@Nullable*/ ClassLoader, Throwable> errorEvent) {
        this.errorEvent = errorEvent;
    }

    @Override
    public synchronized Optional<BiConsumer</*@Nullable*/ ClassLoader, Throwable>> getContextErrorHandler() {
        return Optional.ofNullable(errorEvent);
    }
}
