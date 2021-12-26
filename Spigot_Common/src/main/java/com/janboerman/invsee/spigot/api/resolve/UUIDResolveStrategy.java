package com.janboerman.invsee.spigot.api.resolve;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UUIDResolveStrategy {

    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName);

}
