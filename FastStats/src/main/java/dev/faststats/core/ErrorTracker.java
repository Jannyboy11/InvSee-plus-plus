package dev.faststats.core;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * An error tracker.
 *
 * @since 0.10.0
 */
public interface ErrorTracker permits SimpleErrorTracker {
    /**
     * Create and attach a new context-aware error tracker.
     * <p>
     * This tracker will automatically track errors that occur in the same class loader as the tracker itself.
     * <p>
     * You can still manually track errors using {@code #trackError}.
     *
     * @return the error tracker
     * @see #contextUnaware()
     * @see #trackError(String, boolean)
     * @see #trackError(Throwable, boolean)
     * @since 0.10.0
     */
//    @Contract(value = " -> new")
    static ErrorTracker contextAware() {
        final SimpleErrorTracker tracker = new SimpleErrorTracker();
        tracker.attachErrorContext(ErrorTracker.class.getClassLoader());
        return tracker;
    }

    /**
     * Create a new context-unaware error tracker.
     * <p>
     * This tracker will not automatically track any errors.
     * <p>
     * You have to manually track errors using {@code #trackError}.
     *
     * @return the error tracker
     * @see #contextAware()
     * @see #trackError(String)
     * @see #trackError(Throwable)
     * @since 0.10.0
     */
//    @Contract(value = " -> new")
    static ErrorTracker contextUnaware() {
        return new SimpleErrorTracker();
    }

    /**
     * Tracks a handled  error.
     *
     * @param message the error message
     * @see #trackError(Throwable)
     * @see #trackError(String, boolean)
     * @since 0.10.0
     */
//    @Contract(mutates = "this")
    void trackError(String message);

    /**
     * Tracks a handled error.
     *
     * @param error the error
     * @see #trackError(Throwable, boolean)
     * @since 0.10.0
     */
//    @Contract(mutates = "this")
    void trackError(Throwable error);

    /**
     * Tracks an error.
     * <p>
     * A {@code handled=true} error is expected and properly handled.
     *
     * @param message the error message
     * @param handled whether the error was handled
     * @see #trackError(Throwable, boolean)
     * @since 0.20.0
     */
//    @Contract(mutates = "this")
    void trackError(String message, boolean handled);

    /**
     * Tracks an error.
     * <p>
     * A {@code handled=true} error is expected and properly handled.
     *
     * @param error   the error
     * @param handled whether the error was handled
     * @since 0.20.0
     */
//    @Contract(mutates = "this")
    void trackError(Throwable error, boolean handled);

    /**
     * Adds an error type that will not be reported to FastStats.
     * <p>
     * Matching is done exactly. If, for example {@link LinkageError} was ignored,
     * {@link NoClassDefFoundError} would still be reported, even though it extends {@link LinkageError}
     *
     * @param type the error type
     * @return the error tracker
     * @since 0.21.0
     */
//    @Contract(value = "_ -> this", mutates = "this")
    ErrorTracker ignoreErrorType(Class<? extends Throwable> type);

    /**
     * Adds a pattern that will be matched against all error messages.
     * <p>
     * If an error's message matches the given pattern, it will not be reported to FastStats.
     * <pre>{@code
     * // Exact match
     * tracker.ignoreError(Pattern.compile("No space left on device"));
     *
     * // Regex match
     * tracker.ignoreError(Pattern.compile("No serializer for: class .*"));
     * }</pre>
     *
     * @param pattern the regex pattern to match against error messages
     * @return the error tracker
     * @since 0.21.0
     */
//    @Contract(value = "_ -> this", mutates = "this")
    ErrorTracker ignoreError(Pattern pattern);

    /**
     * Adds a pattern that will be matched against all error messages.
     * <p>
     * If an error's message matches the given pattern, it will not be reported to FastStats.
     *
     * @param pattern the regex pattern string to match against error messages
     * @return the error tracker
     * @see #ignoreError(Pattern)
     * @since 0.21.0
     */
//    @Contract(value = "_ -> this", mutates = "this")
    default ErrorTracker ignoreError(/*@RegExp*/ final String pattern) {
        return ignoreError(Pattern.compile(pattern));
    }

    /**
     * Adds an error type combined with a message pattern that will not be reported to FastStats.
     * <p>
     * An error is ignored only if its class matches the given type exactly and its message matches the given pattern.
     * <pre>{@code
     * tracker.ignoreError(IOException.class, Pattern.compile("No space left on device"));
     * }</pre>
     *
     * @param type    the error type
     * @param pattern the regex pattern to match against error messages
     * @return the error tracker
     * @since 0.21.0
     */
//    @Contract(value = "_, _ -> this", mutates = "this")
    ErrorTracker ignoreError(Class<? extends Throwable> type, Pattern pattern);

    /**
     * Adds an error type combined with a message pattern that will not be reported to FastStats.
     * <p>
     * An error is ignored only if its class matches the given type exactly and its message matches the given pattern.
     *
     * @param type    the error type
     * @param pattern the regex pattern string to match against error messages
     * @return the error tracker
     * @see #ignoreError(Class, Pattern)
     * @since 0.21.0
     */
//    @Contract(value = "_, _ -> this", mutates = "this")
    default ErrorTracker ignoreError(final Class<? extends Throwable> type, /*@RegExp*/ final String pattern) {
        return ignoreError(type, Pattern.compile(pattern));
    }

    /**
     * Attaches an error context to the tracker.
     * <p>
     * If the class loader is {@code null}, the tracker will track all errors.
     *
     * @param loader the class loader
     * @throws IllegalStateException if the error context is already attached
     * @since 0.10.0
     */
    void attachErrorContext(/*@Nullable*/ ClassLoader loader) throws IllegalStateException;

    /**
     * Detaches the error context from the tracker.
     * <p>
     * This restores the original uncaught exception handler that was in place before
     * {@link #attachErrorContext(ClassLoader)} was called.
     * <p>
     * This should be called during shutdown to prevent {@link BootstrapMethodError}
     * when the provider's JAR file is closed.
     *
     * @since 0.13.0
     */
    void detachErrorContext();

    /**
     * Returns whether an error context is attached.
     *
     * @return whether an error context is attached
     * @since 0.13.0
     */
    boolean isContextAttached();

    /**
     * Sets the error event handler which will be called when an error is tracked automatically.
     * <p>
     * The purpose of this handler is to allow custom error handling like logging.
     *
     * @param errorEvent the error event handler
     * @since 0.11.0
     */
//    @Contract(mutates = "this")
    void setContextErrorHandler(/*@Nullable*/ BiConsumer</*@Nullable*/ ClassLoader, Throwable> errorEvent);

    /**
     * Returns the error event handler which will be called when an error is tracked automatically.
     *
     * @return the error event handler
     * @since 0.11.0
     */
//    @Contract(pure = true)
    Optional<BiConsumer</*@Nullable*/ ClassLoader, Throwable>> getContextErrorHandler();

    /**
     * Checks if the error occurred in the same class loader as the provided loader.
     *
     * @param loader the class loader
     * @param error  the error
     * @return whether the error occurred in the same class loader
     * @since 0.14.0
     */
//    @Contract(pure = true)
    static boolean isSameLoader(final ClassLoader loader, final Throwable error) {
        return ErrorHelper.isSameLoader(loader, error);
    }
}    
