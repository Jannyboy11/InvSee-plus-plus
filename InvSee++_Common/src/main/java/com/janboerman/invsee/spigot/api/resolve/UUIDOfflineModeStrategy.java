package com.janboerman.invsee.spigot.api.resolve;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A strategy which synthesizes UUIDs for offline players.
 */
public class UUIDOfflineModeStrategy implements UUIDResolveStrategy {

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        return CompletableFuture.completedFuture(
                Optional.of(
                    UUID.nameUUIDFromBytes(("OfflinePlayer:" + userName).getBytes(StandardCharsets.UTF_8))));
    }

}
