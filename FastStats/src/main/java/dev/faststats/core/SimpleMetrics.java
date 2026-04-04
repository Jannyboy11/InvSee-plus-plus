package dev.faststats.core;

import com.google.gson.JsonObject;
import com.janboerman.invsee.faststats.FastStats;
import com.janboerman.invsee.faststats.Java8HttpClient;
import com.janboerman.invsee.utils.Compat;
import dev.faststats.core.data.Metric;

import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class SimpleMetrics implements Metrics {
    private final Java8HttpClient httpClient = Java8HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private /*@Nullable*/ ScheduledExecutorService executor = null;

    private final Set<Metric<?>> metrics;
    private final Config config;
    private final @Token String token;
    private final /*@Nullable*/ ErrorTracker tracker;
    private final /*@Nullable*/ Runnable flush;
    private final URI url;
    private final boolean debug;

    private final String SDK_NAME;
    private final String SDK_VERSION;
    private final String BUILD_ID;

    {
        final Properties properties = new Properties();
        try (final InputStream stream = getClass().getResourceAsStream("/META-INF/faststats.properties")) {
            if (stream != null) properties.load(stream);
        } catch (final IOException ignored) {
        }
        this.SDK_NAME = properties.getProperty("name", FastStats.SDK_NAME);
        this.SDK_VERSION = properties.getProperty("version", FastStats.SDK_VERSION);
        this.BUILD_ID = properties.getProperty("build-id", "unknown");
    }

//    @Contract(mutates = "io")
    @SuppressWarnings("PatternValidation")
    protected SimpleMetrics(final Factory<?, ?> factory, final Config config) throws IllegalStateException {
        if (factory.token == null) throw new IllegalStateException("Token must be specified");

        this.config = config;
        this.metrics = config.additionalMetrics ? Compat.setCopy(factory.metrics) : Compat.emptySet();
        this.debug = factory.debug || Boolean.getBoolean("faststats.debug") || config.debug();
        this.token = factory.token;
        this.tracker = config.errorTracking ? factory.tracker : null;
        this.flush = factory.flush;
        this.url = factory.url;
    }

//    @Contract(mutates = "io")
    protected SimpleMetrics(final Factory<?, ?> factory, final Path config) throws IllegalStateException {
        this(factory, Config.read(config));
    }

//    @VisibleForTesting
    protected SimpleMetrics(
            final Config config,
            final Set<Metric<?>> metrics,
            @Token final String token,
            /*@Nullable*/ final ErrorTracker tracker,
            /*@Nullable*/ final Runnable flush,
            final URI url,
            final boolean debug
    ) {
        if (!token.matches(Token.PATTERN)) {
            throw new IllegalArgumentException("Invalid token '" + token + "', must match '" + Token.PATTERN + "'");
        }

        this.metrics = config.additionalMetrics ? Compat.setCopy(metrics) : Compat.emptySet();
        this.config = config;
        this.debug = debug;
        this.token = token;
        this.tracker = tracker;
        this.flush = flush;
        this.url = url;
    }

    protected String getOnboardingMessage() {
        return "This plugin uses FastStats to collect anonymous usage statistics.\n"
                + "No personal or identifying information is ever collected.\n"
                + "To opt out, set 'enabled=false' in the metrics configuration file.\n"
                + "Learn more at: https://faststats.dev/info\n"
                + "\n"
                + "Since this is your first start with FastStats, metrics submission will not start\n"
                + "until you restart the server to allow you to opt out if you prefer.";
    }

    protected long getInitialDelay() {
        return TimeUnit.SECONDS.toMillis(Long.getLong("faststats.initial-delay", 30));
    }

    protected long getPeriod() {
        return TimeUnit.MINUTES.toMillis(30);
    }

//    @Async.Schedule
//    @MustBeInvokedByOverriders
    protected void startSubmitting() {
        startSubmitting(getInitialDelay(), getPeriod(), TimeUnit.MILLISECONDS);
    }

    private void startSubmitting(final long initialDelay, final long period, final TimeUnit unit) {
        if (Boolean.getBoolean("faststats.first-run")) {
            info("Skipping metrics submission due to first-run flag");
            return;
        }

        if (config.firstRun) {

            int separatorLength = 0;
            final String[] split = getOnboardingMessage().split("\n");
            for (final String s : split) if (s.length() > separatorLength) separatorLength = s.length();

            String separator = Compat.stringRepeat("-", separatorLength);
            printInfo(separator);
            for (final String s : split) printInfo(s);
            printInfo(separator);

            System.setProperty("faststats.first-run", "true");
            if (!config.externallyManaged()) return;
        }

        final boolean enabled = Boolean.parseBoolean(System.getProperty("faststats.enabled", "true"));

        if (!config.enabled() || !enabled) {
            warn("Metrics disabled, not starting submission");
            return;
        }

        if (isSubmitting()) {
            warn("Metrics already submitting, not starting again");
            return;
        }

        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "metrics-submitter");
            thread.setDaemon(true);
            return thread;
        });

        info("Starting metrics submission");
        executor.scheduleAtFixedRate(this::submit, Math.max(0, initialDelay), Math.max(1000, period), unit);
    }

    protected boolean isSubmitting() {
        return executor != null && !executor.isShutdown();
    }

    public boolean submit() {
        try {
            return submitNow();
        } catch (final Throwable t) {
            error("Failed to submit metrics", t);
            return false;
        }
    }

    private boolean submitNow() throws IOException {
        final String data = createData().toString();
        final byte[] bytes = data.getBytes(UTF_8);

        info("Uncompressed data: " + data);

        try (final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
             final GZIPOutputStream output = new GZIPOutputStream(byteOutput)) {

            output.write(bytes);
            output.finish();

            final byte[] compressed = byteOutput.toByteArray();
            info("Compressed size: " + compressed.length + " bytes");

            final Java8HttpClient.Request request = Java8HttpClient.Request.newBuilder()
                    .POST(compressed)
                    .header("Content-Encoding", "gzip")
                    .header("Content-Type", "application/octet-stream")
                    .header("Authorization", "Bearer " + getToken())
                    .header("User-Agent", "FastStats Metrics " + SDK_NAME + "/" + SDK_VERSION)
                    .timeout(Duration.ofSeconds(3))
                    .uri(url)
                    .build();

            info("Sending metrics to: " + url);
            try {
                final Java8HttpClient.Response response = httpClient.send(request);
                final int statusCode = response.statusCode();
                final String body = response.body();

                if (statusCode >= 200 && statusCode < 300) {
                    info("Metrics submitted with status code: " + statusCode + " (" + body + ")");
                    getErrorTracker().map(SimpleErrorTracker.class::cast).ifPresent(SimpleErrorTracker::clear);
                    if (flush != null) flush.run();
                    return true;
                } else if (statusCode >= 300 && statusCode < 400) {
                    warn("Received redirect response from metrics server: " + statusCode + " (" + body + ")");
                } else if (statusCode >= 400 && statusCode < 500) {
                    error("Submitted invalid request to metrics server: " + statusCode + " (" + body + ")", null);
                } else if (statusCode >= 500 && statusCode < 600) {
                    error("Received server error response from metrics server: " + statusCode + " (" + body + ")", null);
                } else {
                    warn("Received unexpected response from metrics server: " + statusCode + " (" + body + ")");
                }
            } catch (final ConnectException t) {
                error("Failed to connect to metrics server: " + url, null);
            } catch (final IOException t) {
                error("Metrics submission timed out after 3 seconds: " + url, null);
            } catch (final Exception e) {
                error("Failed to submit metrics", e);
            }
            return false;
        }
    }

    private final String javaVendor = System.getProperty("java.vendor");
    private final String javaVersion = System.getProperty("java.version");
    private final String osArch = System.getProperty("os.arch");
    private final String osName = System.getProperty("os.name");
    private final String osVersion = System.getProperty("os.version");
    private final int coreCount = Runtime.getRuntime().availableProcessors();

    protected JsonObject createData() {
        final JsonObject data = new JsonObject();
        final JsonObject metrics = new JsonObject();

        metrics.addProperty("core_count", coreCount);
        metrics.addProperty("java_vendor", javaVendor);
        metrics.addProperty("java_version", javaVersion);
        metrics.addProperty("os_arch", osArch);
        metrics.addProperty("os_name", osName);
        metrics.addProperty("os_version", osVersion);

        try {
            appendDefaultData(metrics);
        } catch (final Throwable t) {
            error("Failed to append default data", t);
            getErrorTracker().ifPresent(tracker -> tracker.trackError(t));
        }

        this.metrics.forEach(metric -> {
            try {
                metric.getData().ifPresent(element -> metrics.add(metric.getId(), element));
            } catch (final Throwable t) {
                error("Failed to build metric data: " + metric.getId(), t);
                getErrorTracker().ifPresent(tracker -> tracker.trackError(t));
            }
        });

        data.addProperty("identifier", config.serverId().toString());
        data.add("data", metrics);

        getErrorTracker().map(SimpleErrorTracker.class::cast)
                .map(tracker -> tracker.getData(BUILD_ID))
                .filter(errors -> !errors.isEmpty())
                .ifPresent(errors -> data.add("errors", errors));
        return data;
    }

    @Override
    public @Token String getToken() {
        return token;
    }

    @Override
    public Optional<ErrorTracker> getErrorTracker() {
        return Optional.ofNullable(tracker);
    }

    @Override
    public Config getConfig() {
        return config;
    }

//    @Contract(mutates = "param1")
    protected abstract void appendDefaultData(JsonObject metrics);

    protected void error(final String message, /*@Nullable*/ final Throwable throwable) {
        if (debug) printError("[" + getClass().getName() + "]: " + message, throwable);
    }

    protected void warn(final String message) {
        if (debug) printWarning("[" + getClass().getName() + "]: " + message);
    }

    protected void info(final String message) {
        if (debug) printInfo("[" + getClass().getName() + "]: " + message);
    }

    protected abstract void printError(String message, /*(@Nullable*/ Throwable throwable);

    protected abstract void printInfo(String message);

    protected abstract void printWarning(String message);

    @Override
    public void shutdown() {
        getErrorTracker().ifPresent(ErrorTracker::detachErrorContext);
        if (executor != null) try {
            info("Shutting down metrics submission");
            executor.shutdown();
            getErrorTracker().map(SimpleErrorTracker.class::cast)
                    .filter(SimpleErrorTracker::needsFlushing)
                    .ifPresent(ignored -> submit());
        } catch (final Throwable t) {
            error("Failed to submit metrics on shutdown", t);
        } finally {
            executor = null;
        }
    }

    public abstract static class Factory<T, F extends Metrics.Factory<T, F>> implements Metrics.Factory<T, F> {
        private final Set<Metric<?>> metrics = new HashSet<>(0);
        private URI url = URI.create("https://metrics.faststats.dev/v1/collect");
        private /*@Nullable*/ ErrorTracker tracker;
        private /*@Nullable*/ Runnable flush;
        private /*@Nullable*/ String token;
        private boolean debug = false;

        @Override
        @SuppressWarnings("unchecked")
        public F addMetric(final Metric<?> metric) throws IllegalArgumentException {
            if (!metrics.add(metric)) throw new IllegalArgumentException("Metric already added: " + metric.getId());
            return (F) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public F onFlush(final Runnable flush) {
            this.flush = flush;
            return (F) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public F errorTracker(final ErrorTracker tracker) {
            this.tracker = tracker;
            return (F) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public F debug(final boolean enabled) {
            this.debug = enabled;
            return (F) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public F token(@Token final String token) throws IllegalArgumentException {
            if (!token.matches(Token.PATTERN)) {
                throw new IllegalArgumentException("Invalid token '" + token + "', must match '" + Token.PATTERN + "'");
            }
            this.token = token;
            return (F) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public F url(final URI url) {
            this.url = url;
            return (F) this;
        }
    }

    public static final class Config implements Metrics.Config {

        private final UUID serverId;
        private final boolean additionalMetrics;
        private final boolean debug;
        private final boolean enabled;
        private final boolean errorTracking;
        private final boolean firstRun;
        private final boolean externallyManaged;

        public Config(
            UUID serverId,
            boolean additionalMetrics,
            boolean debug,
            boolean enabled,
            boolean errorTracking,
            boolean firstRun,
            boolean externallyManaged
        ) {
            this.serverId = serverId;
            this.additionalMetrics = additionalMetrics;
            this.debug = debug;
            this.enabled = enabled;
            this.errorTracking = errorTracking;
            this.firstRun = firstRun;
            this.externallyManaged = externallyManaged;
        }

        public UUID serverId() { return serverId; }
        public boolean additionalMetrics() { return additionalMetrics; }
        public boolean debug() { return debug; }
        public boolean enabled() { return enabled; }
        public boolean errorTracking() { return errorTracking; }
        public boolean firstRun() { return firstRun; }
        public boolean externallyManaged() { return externallyManaged; }

        public static final String DEFAULT_COMMENT =
                 "FastStats (https://faststats.dev) collects anonymous usage statistics for plugin developers.\n"
                + "# This helps developers understand how their projects are used in the real world.\n"
                + "#\n"
                + "# No IP addresses, player data, or personal information is collected.\n"
                + "# The server ID below is randomly generated and can be regenerated at any time.\n"
                + "#\n"
                + "# Enabling metrics has no noticeable performance impact.\n"
                + "# Keeping metrics enabled is recommended, but you can opt out by setting\n"
                + "# 'enabled=false' in plugins/faststats/config.properties.\n"
                + "#\n"
                + "# If you suspect a plugin is collecting personal data or bypassing the "enabled" option,\n"
                + "# please report it at: https://faststats.dev/abuse\n"
                + "#\n"
                + "# For more information, visit: https://faststats.dev/info\n";

//        @Contract(mutates = "io")
        public static Config read(final Path file) throws RuntimeException {
            return read(file, DEFAULT_COMMENT, false, false);
        }

//        @Contract(mutates = "io")
        public static Config read(final Path file, final String comment, final boolean externallyManaged, final boolean externallyEnabled) throws RuntimeException {
            final Optional<Properties> properties = readOrEmpty(file);
            final boolean firstRun = !properties.isPresent();
            final AtomicBoolean saveConfig = new AtomicBoolean(firstRun);

            final UUID serverId = properties.map(object -> object.getProperty("serverId")).map(string -> {
                try {
                    final String trimmed = string.trim();
                    final String corrected = trimmed.length() > 36 ? trimmed.substring(0, 36) : trimmed;
                    if (!corrected.equals(string)) saveConfig.set(true);
                    return UUID.fromString(corrected);
                } catch (final IllegalArgumentException e) {
                    saveConfig.set(true);
                    return UUID.randomUUID();
                }
            }).orElseGet(() -> {
                saveConfig.set(true);
                return UUID.randomUUID();
            });

            final BiPredicate<String, Boolean> predicate = (key, defaultValue) -> {
                return properties.map(object -> object.getProperty(key)).map(Boolean::parseBoolean).orElseGet(() -> {
                    saveConfig.set(true);
                    return defaultValue;
                });
            };

            final boolean enabled = externallyManaged ? externallyEnabled : predicate.test("enabled", true);
            final boolean errorTracking = predicate.test("submitErrors", true);
            final boolean additionalMetrics = predicate.test("submitAdditionalMetrics", true);
            final boolean debug = predicate.test("debug", false);

            if (saveConfig.get()) try {
                save(file, externallyManaged, comment, serverId, enabled, errorTracking, additionalMetrics, debug);
            } catch (final IOException e) {
                throw new RuntimeException("Failed to save metrics config", e);
            }

            return new Config(serverId, additionalMetrics, debug, enabled, errorTracking, firstRun, externallyManaged);
        }

        private static Optional<Properties> readOrEmpty(final Path file) throws RuntimeException {
            if (!Files.isRegularFile(file)) return Optional.empty();
            try (final BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
                final Properties properties = new Properties();
                properties.load(reader);
                return Optional.of(properties);
            } catch (final IOException e) {
                throw new RuntimeException("Failed to read metrics config", e);
            }
        }

        private static void save(final Path file, final boolean externallyManaged, final String comment, final UUID serverId, final boolean enabled, final boolean errorTracking, final boolean additionalMetrics, final boolean debug) throws IOException {
            Files.createDirectories(file.getParent());
            try (final OutputStream out = Files.newOutputStream(file);
                final OutputStreamWriter writer = new OutputStreamWriter(out, UTF_8)) {
                final Properties properties = new Properties();

                properties.setProperty("serverId", serverId.toString());
                if (!externallyManaged) properties.setProperty("enabled", Boolean.toString(enabled));
                properties.setProperty("submitErrors", Boolean.toString(errorTracking));
                properties.setProperty("submitAdditionalMetrics", Boolean.toString(additionalMetrics));
                properties.setProperty("debug", Boolean.toString(debug));

                properties.store(writer, comment);
            }
        }
    }
}
