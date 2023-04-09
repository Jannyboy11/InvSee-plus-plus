package com.janboerman.invsee.spigot.api.resolve;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A strategy which resolves player's Unique IDs if they are online.
 */
public class UUIDOnlinePlayerStrategy implements UUIDResolveStrategy {

    private final Server server;
    private final Executor syncExecutor;

    public UUIDOnlinePlayerStrategy(Server server, Executor syncExecutor) {
        this.server = Objects.requireNonNull(server);
        this.syncExecutor = Objects.requireNonNull(syncExecutor);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        return CompletableFuture.supplyAsync(() -> {
            Player player = server.getPlayerExact(userName);
            if (player == null) return Optional.empty();
            else return Optional.of(player.getUniqueId());
        }, syncExecutor);
    }
}
