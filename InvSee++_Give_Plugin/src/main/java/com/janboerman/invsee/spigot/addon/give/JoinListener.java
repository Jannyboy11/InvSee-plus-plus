package com.janboerman.invsee.spigot.addon.give;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {

    private final GivePlugin plugin;
    private final ItemQueueManager queueManager;

    JoinListener(GivePlugin plugin, ItemQueueManager queueManager) {
        this.plugin = plugin;
        this.queueManager = queueManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        ItemQueue inventoryQueue = queueManager.getInventoryQueue(uuid);
        ItemQueue enderchestQueue = queueManager.getEnderchestQueue(uuid);
        inventoryQueue.process(player.getInventory(), plugin.getServer().getConsoleSender(), name, "inventory");
        enderchestQueue.process(player.getEnderChest(), plugin.getServer().getConsoleSender(), name, "enderchest");
        queueManager.save(uuid, inventoryQueue, enderchestQueue);
    }

}
