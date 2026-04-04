package dev.faststats.core.data;

import com.google.gson.JsonElement;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * A metric.
 *
 * @param <T> the metric data type
 * @since 0.16.0
 */
public interface Metric<T> {
    /**
     * Get the source id.
     *
     * @return the source id
     * @since 0.16.0
     */
    @SourceId
//    @Contract(pure = true)
    String getId();

    /**
     * Compute the metric data.
     *
     * @return an optional containing the metric data
     * @throws Exception if unable to compute the metric data
     * @implSpec The implementation must be thread-safe and pure (i.e. not modify any shared state).
     * @since 0.16.0
     */
//    @Contract(pure = true)
    Optional<T> compute() throws Exception;

    /**
     * Get the metric data as a JSON element.
     *
     * @return an optional containing the metric data as {@link JsonElement}
     * @throws Exception if unable to get the metric data
     * @implSpec The implementation must call {@link #compute()} to get the metric data
     * and follow the same thread-safety and pureness requirements.
     * @see #compute()
     * @since 0.16.0
     */
//    @Contract(pure = true)
    Optional<JsonElement> getData() throws Exception;

    /**
     * Create a string array metric.
     *
     * @param id       the source id
     * @param callable the metric data callable
     * @return the string array metric
     * @throws IllegalArgumentException if the source id is invalid
     * @apiNote The callable must be thread-safe and pure (i.e. not modify any shared state).
     * @see #compute()
     * @since 0.16.0
     */
//    @Contract(value = "_, _ -> new", pure = true)
    static Metric<String[]> stringArray(@SourceId final String id, final Callable<String /*@Nullable*/ []> callable) throws IllegalArgumentException {
        return new ArrayMetric<>(id, callable);
    }

    /**
     * Create a boolean array metric.
     *
     * @param id       the source id
     * @param callable the metric data callable
     * @return the boolean array metric
     * @throws IllegalArgumentException if the source id is invalid
     * @apiNote The callable must be thread-safe and pure (i.e. not modify any shared state).
     * @see #compute()
     * @since 0.16.0
     */
//    @Contract(value = "_, _ -> new", pure = true)
    static Metric<Boolean[]> booleanArray(@SourceId final String id, final Callable<Boolean /*@Nullable*/ []> callable) throws IllegalArgumentException {
        return new ArrayMetric<>(id, callable);
    }

    /**
     * Create a number array metric.
     *
     * @param id       the source id
     * @param callable the metric data callable
     * @return the number array metric
     * @throws IllegalArgumentException if the source id is invalid
     * @apiNote The callable must be thread-safe and pure (i.e. not modify any shared state).
     * @see #compute()
     * @since 0.16.0
     */
//    @Contract(value = "_, _ -> new", pure = true)
    static Metric<Number[]> numberArray(@SourceId final String id, final Callable<Number /*@Nullable*/ []> callable) throws IllegalArgumentException {
        return new ArrayMetric<>(id, callable);
    }

    /**
     * Create a metric for a boolean value.
     *
     * @param id       the source id
     * @param callable the metric data callable
     * @return the boolean metric
     * @throws IllegalArgumentException if the source id is invalid
     * @apiNote The callable must be thread-safe and pure (i.e. not modify any shared state).
     * @see #compute()
     * @since 0.16.0
     */
//    @Contract(value = "_, _ -> new", pure = true)
    static Metric<Boolean> bool(@SourceId final String id, final Callable</*@Nullable*/ Boolean> callable) throws IllegalArgumentException {
        return new SingleValueMetric<>(id, callable);
    }

    /**
     * Create a metric for a string value.
     *
     * @param id       the source id
     * @param callable the metric data callable
     * @return the string metric
     * @throws IllegalArgumentException if the source id is invalid
     * @apiNote The callable must be thread-safe and pure (i.e. not modify any shared state).
     * @see #compute()
     * @since 0.16.0
     */
//    @Contract(value = "_, _ -> new", pure = true)
    static Metric<String> string(@SourceId final String id, final Callable</*@Nullable*/ String> callable) throws IllegalArgumentException {
        return new SingleValueMetric<>(id, callable);
    }

    /**
     * Create a metric for a number value.
     *
     * @param id       the source id
     * @param callable the metric data callable
     * @return the number metric
     * @throws IllegalArgumentException if the source id is invalid
     * @apiNote The callable must be thread-safe and pure (i.e. not modify any shared state).
     * @see #compute()
     * @since 0.16.0
     */
//    @Contract(value = "_, _ -> new", pure = true)
    static Metric<Number> number(@SourceId final String id, final Callable</*@Nullable*/ Number> callable) throws IllegalArgumentException {
        return new SingleValueMetric<>(id, callable);
    }
}
