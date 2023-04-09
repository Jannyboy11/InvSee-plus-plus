package com.janboerman.invsee.spigot.api.resolve;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A strategy for converting player Unique IDs into their usernames.
 */
public interface NameResolveStrategy {

    /**
     * Resove the player's username
     * @param uniqueId the player's unique id
     * @return a future which, when completed, will supply the player's username optionally, if it could be found.
     */
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId);

}
