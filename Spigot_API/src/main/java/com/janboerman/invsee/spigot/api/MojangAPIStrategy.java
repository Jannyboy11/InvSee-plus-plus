package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.mojangapi.MojangAPI;
import org.bukkit.plugin.Plugin;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
        return mojangApi.lookupUniqueId(userName).thenApplyAsync(
                Function.identity(),
                runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

}
