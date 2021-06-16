package com.janboerman.invsee.spigot.api;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
