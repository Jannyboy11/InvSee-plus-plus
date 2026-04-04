package dev.faststats.core.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Optional;
import java.util.concurrent.Callable;

final class SingleValueMetric<T> extends SimpleMetric<T> {
    public SingleValueMetric(@SourceId final String id, final Callable</*@Nullable*/ T> callable) throws IllegalArgumentException {
        super(id, callable);
    }

    @Override
    public Optional<JsonElement> getData() throws Exception {
        return compute().map(data -> {
            if (data instanceof final Boolean bool) return new JsonPrimitive(bool);
            if (data instanceof final Number number) return new JsonPrimitive(number);
            return new JsonPrimitive(data.toString());
        });
    }
}
