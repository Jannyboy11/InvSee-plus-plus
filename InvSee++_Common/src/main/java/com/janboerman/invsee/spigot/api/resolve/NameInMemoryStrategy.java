package com.janboerman.invsee.spigot.api.resolve;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Strategy which resolves usernames by doing a simple {@linkplain Map} lookup.
 */
public class NameInMemoryStrategy implements NameResolveStrategy {

    private final Map<UUID, String> cache;

    public NameInMemoryStrategy(Map<UUID, String> cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        return CompletableFuture.completedFuture(Optional.ofNullable(cache.get(uniqueId)));
    }

}
