package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.entity.HumanEntity;

import java.util.UUID;

/**
 * Scheduler implementation based on the {@link org.bukkit.scheduler.BukkitScheduler}.
 */
public class DefaultScheduler implements Scheduler {

    private final InvseePlusPlus plugin;

    public DefaultScheduler(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeSyncPlayer(UUID playerId, Runnable task, Runnable retired) {
        executeSync(task);
    }

    public void executeSyncPlayer(HumanEntity player, Runnable task, Runnable retired) {
        executeSync(task);
    }

    @Override
    public void executeSyncGlobal(Runnable task) {
        executeSync(task);
    }

    @Override
    public void executeSyncGlobalRepeatedly(Runnable task, long ticksInitialDelay, long ticksPeriod) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, task, ticksInitialDelay, ticksPeriod);
    }

    private void executeSync(Runnable task) {
        if (plugin.getServer().isPrimaryThread()) {
            task.run();
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    @Override
    public void executeAsync(Runnable task) {
        if (!plugin.getServer().isPrimaryThread()) {
            task.run();
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    @Override
    public void executeLaterGlobal(Runnable task, long delayTicks) {
        plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void executeLaterAsync(Runnable task, long delayTicks) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }
}
