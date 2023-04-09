package com.janboerman.invsee.spigot.api.resolve;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A strategy for converting player usernames into their Unique IDs.
 */
public interface UUIDResolveStrategy {

    /**
     * Resolve the player's Unique ID.
     * @param userName the player's username
     * @return a future which, when completed, will supply the player's Unique ID optionally, if it could be found.
     */
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName);

}
