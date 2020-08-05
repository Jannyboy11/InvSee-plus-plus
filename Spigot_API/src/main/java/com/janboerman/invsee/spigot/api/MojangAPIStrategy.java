package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.mojangapi.MojangAPI;
import org.bukkit.plugin.Plugin;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MojangAPIStrategy implements UUIDResolveStrategy {

    private final Plugin plugin;
    private final MojangAPI mojangApi;

    public MojangAPIStrategy(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.mojangApi = new MojangAPI(HttpClient.newBuilder()
                .executor(runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable))
                .build());
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        return mojangApi.lookupUniqueId(userName).handle((Optional<UUID> success, Throwable error) -> {
            if (error == null) return success;
            plugin.getLogger().log(Level.WARNING, "Could not request " + userName + " from Mojang's REST API", error);
            return Optional.empty();
        });
    }

}
