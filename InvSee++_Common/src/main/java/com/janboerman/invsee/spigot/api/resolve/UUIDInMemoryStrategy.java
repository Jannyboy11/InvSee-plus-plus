package com.janboerman.invsee.spigot.api.resolve;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Strategy which resolves Unique IDs by doing a simple {@linkplain Map} lookup.
 */
public class UUIDInMemoryStrategy implements UUIDResolveStrategy {

    private final Map<String, UUID> cache;

    public UUIDInMemoryStrategy(Map<String, UUID> cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        return CompletableFuture.completedFuture(Optional.ofNullable(cache.get(userName)));
    }

}
