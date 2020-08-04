package com.janboerman.invsee.spigot.api;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OnlinePlayerStrategy implements UUIDResolveStrategy {

    private final Server server;

    public OnlinePlayerStrategy(Server server) {
        this.server = Objects.requireNonNull(server);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        return CompletableFuture.completedFuture(Optional.ofNullable(server.getPlayerExact(userName)).map(Player::getUniqueId));
    }

}
