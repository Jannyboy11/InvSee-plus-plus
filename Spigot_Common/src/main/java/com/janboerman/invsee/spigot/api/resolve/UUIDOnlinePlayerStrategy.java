package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UUIDOnlinePlayerStrategy implements UUIDResolveStrategy {

    private Server server;

    public UUIDOnlinePlayerStrategy(Server server) {
        this.server = Objects.requireNonNull(server);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        Player player = server.getPlayer(userName);
        if (player == null) return CompletedEmpty.the();

        return CompletableFuture.completedFuture(Optional.of(player.getUniqueId()));
    }
}
