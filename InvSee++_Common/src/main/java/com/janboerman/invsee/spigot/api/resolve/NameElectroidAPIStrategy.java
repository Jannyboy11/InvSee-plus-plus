package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.mojangapi.ElectroidAPI;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class NameElectroidAPIStrategy implements NameResolveStrategy {

    private final Plugin plugin;
    private final ElectroidAPI electroidApi;

    public NameElectroidAPIStrategy(Plugin plugin, ElectroidAPI electroidApi) {
        this.plugin = Objects.requireNonNull(plugin);
        this.electroidApi = Objects.requireNonNull(electroidApi);
    }

    @Override
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        return electroidApi.lookupUserName(uniqueId).handle((Optional<String> success, Throwable error) -> {
            if (error == null) return success;
            plugin.getLogger().log(Level.WARNING, "Could not request profile for uuid " + uniqueId + " from Electroid's Mojang API", error);
            return Optional.empty();
        });
    }
}