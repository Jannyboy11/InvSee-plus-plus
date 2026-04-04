package dev.faststats.core.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.Callable;

final class ArrayMetric<T> extends SimpleMetric<T[]> {
    public ArrayMetric(@SourceId final String id, final Callable<T @Nullable []> callable) throws IllegalArgumentException {
        super(id, callable);
    }

    @Override
    public Optional<JsonElement> getData() throws Exception {
        return compute().map(data -> {
            final var elements = new JsonArray(data.length);
            for (final var d : data) {
                if (d instanceof final Boolean b) elements.add(b);
                else if (d instanceof final Number n) elements.add(n);
                else elements.add(d.toString());
            }
            return elements;
        });
    }
}
