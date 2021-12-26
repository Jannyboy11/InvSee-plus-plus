package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NameOnlinePlayerStrategy implements NameResolveStrategy {

    private final Server server;

    public NameOnlinePlayerStrategy(Server server) {
        this.server = Objects.requireNonNull(server);
    }

    @Override
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        Player player = server.getPlayer(uniqueId);
        if (player == null) return CompletedEmpty.the();

        return CompletableFuture.completedFuture(Optional.of(player.getName()));
    }
}
