package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.mojangapi.MojangAPI;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class UUIDMojangAPIStrategy implements UUIDResolveStrategy {

    private final Plugin plugin;
    private final MojangAPI mojangApi;

    public UUIDMojangAPIStrategy(Plugin plugin, MojangAPI mojangApi) {
        this.plugin = Objects.requireNonNull(plugin);
        this.mojangApi = Objects.requireNonNull(mojangApi);
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        return mojangApi.lookupUniqueId(userName).handle((Optional<UUID> success, Throwable error) -> {
            if (error == null) return success;
            plugin.getLogger().log(Level.WARNING, "Could not request profile for name " + userName + " from Mojang's REST API", error);
            return Optional.empty();
        });
    }

}
