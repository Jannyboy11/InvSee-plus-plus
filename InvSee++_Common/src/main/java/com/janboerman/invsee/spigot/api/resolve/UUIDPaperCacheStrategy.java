package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UUIDPaperCacheStrategy implements UUIDResolveStrategy {

    private final Plugin plugin;
    private final Scheduler scheduler;
    private final Server server;
    private final Method method;

    public UUIDPaperCacheStrategy(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.scheduler = scheduler;

        Method method;
        try {
            method = server.getClass().getMethod("getOfflinePlayerIfCached", String.class);
        } catch (NoSuchMethodException e) {
            method = null;
        }
        this.method = method;
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        if (method == null) return CompletedEmpty.the();
        return CompletableFuture.supplyAsync(() -> {
            try {
                OfflinePlayer offlinePlayer = (OfflinePlayer) method.invoke(server, userName);
                if (offlinePlayer == null) return Optional.empty();
                else return Optional.of(offlinePlayer.getUniqueId());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }, scheduler::executeSyncGlobal);
    }

}
