package com.janboerman.invsee.spigot.api;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InMemoryStrategy implements UUIDResolveStrategy {

    private final CaseInsensitiveMap<UUID> cache;

    public InMemoryStrategy(CaseInsensitiveMap<UUID> cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        return CompletableFuture.completedFuture(Optional.ofNullable(cache.get(userName)));
    }

    public static class CaseInsensitiveMap<V> extends LinkedHashMap<String, V> {

        @Override
        public V put(String key, V value) {
            return super.put(key.toLowerCase(Locale.ROOT), value);
        }

        public V get(String key) {
            return super.get(key.toLowerCase(Locale.ROOT));
        }

        @Override
        public V get(Object object) {
            if (object instanceof String || object == null) {
                return get((String) object);
            } else {
                throw new IllegalArgumentException("Called CaseInsensitiveMap#get(Object) with an argument that is not a String. Instead got: " + object);
            }
        }
    }

}
