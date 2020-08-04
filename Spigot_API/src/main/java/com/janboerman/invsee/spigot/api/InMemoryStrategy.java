package com.janboerman.invsee.spigot.api;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InMemoryStrategy implements UUIDResolveStrategy {

    private final Map<String, UUID> cache;

    public InMemoryStrategy(Map<String, UUID> cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        return CompletableFuture.completedFuture(Optional.ofNullable(cache.get(userName)));
    }

    public static class CaseInsensitiveMap<V> extends LinkedHashMap<String, V> {

        @Override
        public V put(String key, V value) {
            if (key != null) {
                return super.put(key.toLowerCase(Locale.ROOT), value);
            } else {
                return super.put(null, value);
            }
        }

        @Override
        public V remove(Object key) {
            if (key instanceof String || key == null) {
                return remove((String) key);
            } else {
                throw new IllegalArgumentException("Called CaseInsensitiveMap#remove(Object) with a key that is not a String. Instead got: " + key);
            }
        }

        public V remove(String key) {
            if (key != null) {
                return super.remove(key.toLowerCase(Locale.ROOT));
            } else {
                return super.remove(null);
            }
        }

        @Override
        public boolean remove(Object key, Object value) {
            if (key instanceof String || key == null) {
                return remove((String) key, value);
            } else {
                throw new IllegalArgumentException("Called CaseInsensitiveMap#remove(Object,Object) with a key that is not a String. Instead got: " + key);
            }
        }

        public boolean remove(String key, Object value) {
            if (key != null) {
                return super.remove(key.toLowerCase(Locale.ROOT), value);
            } else {
                return super.remove(null, value);
            }
        }

        @Override
        public V get(Object key) {
            if (key instanceof String || key == null) {
                return get((String) key);
            } else {
                throw new IllegalArgumentException("Called CaseInsensitiveMap#get(Object) with a key that is not a String. Instead got: " + key);
            }
        }

        public V get(String key) {
            if (key != null) {
                return super.get(key.toLowerCase(Locale.ROOT));
            } else {
                return super.get(null);
            }
        }
    }

}
