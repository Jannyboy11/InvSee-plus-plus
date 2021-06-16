package com.janboerman.invsee.spigot.api;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface NameResolveStrategy {

    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId);

}
