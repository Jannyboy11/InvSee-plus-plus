package com.janboerman.invsee.folia;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.Scheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler implementation based on {@link io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler},
 * {@link io.papermc.paper.threadedregions.scheduler.AsyncScheduler} and {@link EntityScheduler}.
 */
public class FoliaScheduler implements Scheduler {

    private InvseePlusPlus plugin;

    public FoliaScheduler(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeSyncPlayer(UUID playerId, Runnable task, Runnable retired) {
        Server server = plugin.getServer();
        Player player = server.getPlayer(playerId);
        if (player != null) {
            EntityScheduler scheduler = player.getScheduler();
            scheduler.run(plugin, scheduledTask -> task.run(), retired);
        } else {
            executeSyncGlobal(task);
        }
    }

    public void executeSyncPlayer(HumanEntity player, Runnable task, Runnable retired) {
        player.getScheduler().run(plugin, scheduledTask -> task.run(), retired);
    }

    @Override
    public void executeSyncGlobal(Runnable task) {
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
    }

    @Override
    public void executeSyncGlobalRepeatedly(Runnable task, long ticksInitialDelay, long ticksPeriod) {
        // Folia's GlobalRegionScheduler forbids an initial delay or period <= 0 (the BukkitScheduler tolerates 0),
        // so clamp to the minimum of 1 tick to keep the cross-platform Scheduler contract.
        long initialDelay = Math.max(1L, ticksInitialDelay);
        long period = Math.max(1L, ticksPeriod);
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelay, period);
    }

    @Override
    public void executeAsync(Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    @Override
    public void executeLaterGlobal(Runnable task, long delayTicks) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), Math.max(1L, delayTicks));
    }

    @Override
    public void executeLaterAsync(Runnable task, long delayTicks) {
        long delayMillis = Math.max(1L, delayTicks) * 50L;
        plugin.getServer().getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayMillis, TimeUnit.MILLISECONDS);
    }
}
