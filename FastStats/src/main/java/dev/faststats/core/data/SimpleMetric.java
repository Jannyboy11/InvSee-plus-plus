package dev.faststats.core.data;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

abstract class SimpleMetric<T> implements Metric<T> {
    private final @SourceId String id;
    private final Callable</*@Nullable*/ T> callable;

    public SimpleMetric(@SourceId final String id, final Callable</*@Nullable*/ T> callable) throws IllegalArgumentException {
        if (!id.matches(SourceId.PATTERN)) {
            throw new IllegalArgumentException("Invalid source id '" + id + "', must match '" + SourceId.PATTERN + "'");
        }
        this.id = id;
        this.callable = callable;
    }

    @Override
    public final @SourceId String getId() {
        return id;
    }

    public final Optional<T> compute() throws Exception {
        return Optional.ofNullable(callable.call());
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final SimpleMetric<?> that = (SimpleMetric<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SimpleMetric{" +
                "id='" + id + '\'' +
                '}';
    }
}
