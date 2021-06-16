package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.mojangapi.MojangAPI;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class NameMojangAPIStrategy implements NameResolveStrategy {

    private final Plugin plugin;
    private final MojangAPI mojangApi;

    public NameMojangAPIStrategy(Plugin plugin, MojangAPI mojangApi) {
        this.plugin = Objects.requireNonNull(plugin);
        this.mojangApi = Objects.requireNonNull(mojangApi);
    }

    @Override
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        return mojangApi.lookupUserName(uniqueId).handle((Optional<String> success, Throwable error) -> {
            if (error == null) return success;
            plugin.getLogger().log(Level.WARNING, "Could not request profile for id " + uniqueId + " from Mojang's REST API", error);
            return Optional.empty();
        });
    }
}
