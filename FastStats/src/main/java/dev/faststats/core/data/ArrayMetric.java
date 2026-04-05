package dev.faststats.core.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Optional;
import java.util.concurrent.Callable;

final class ArrayMetric<T> extends SimpleMetric<T[]> {
    public ArrayMetric(@SourceId final String id, final Callable<T /*@Nullable*/ []> callable) throws IllegalArgumentException {
        super(id, callable);
    }

    @Override
    public Optional<JsonElement> getData() throws Exception {
        return compute().map(data -> {
            final JsonArray elements = new JsonArray(data.length);
            for (final T d : data) {
                if (d instanceof Boolean) elements.add((Boolean) d);
                else if (d instanceof Number) elements.add((Number) d);
                else elements.add(String.valueOf(d));
            }
            return elements;
        });
    }
}
