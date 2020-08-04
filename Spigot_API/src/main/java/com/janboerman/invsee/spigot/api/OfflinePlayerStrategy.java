package com.janboerman.invsee.spigot.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Deprecated
public class OfflinePlayerStrategy implements UUIDResolveStrategy {

    private final Server server;

    public OfflinePlayerStrategy(Server server) {
        this.server = Objects.requireNonNull(server);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        return CompletableFuture.completedFuture(Optional.of(server.getOfflinePlayer(userName)).map(OfflinePlayer::getUniqueId));
    }

}
