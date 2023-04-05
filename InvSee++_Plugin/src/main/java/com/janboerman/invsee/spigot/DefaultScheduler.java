package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.internal.Scheduler;

import java.util.UUID;

public class DefaultScheduler implements Scheduler {

    private final InvseePlusPlus plugin;

    public DefaultScheduler(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeSyncPlayer(UUID playerId, Runnable task, Runnable retired) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public void executeSyncGlobal(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public void executeAsync(Runnable task) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

}
