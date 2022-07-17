package com.janboerman.invsee.spigot.addon.give;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

class ItemQueueManager {

    private static final String INVENTORY_QUEUE = "inventory-queue";
    private static final String ENDERCHEST_QUEUE = "enderchest-queue";

    private final GivePlugin plugin;
    private final Map<UUID, ItemQueue> inventoryQueues = new HashMap<>();
    private final Map<UUID, ItemQueue> enderchestQueues = new HashMap<>();
    private final File saveFolder;

    ItemQueueManager(GivePlugin plugin) {
        this.plugin = plugin;
        this.saveFolder = new File(plugin.getDataFolder(), "item queues");
    }

    void load() {
        if (!saveFolder.exists()) saveFolder.mkdirs();
        File[] saveFiles = saveFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (saveFiles != null) {
            for (File saveFile : saveFiles) {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(saveFile);
                String fileName = saveFile.getName();
                UUID uuid = UUID.fromString(fileName.substring(0, fileName.length() - 4));
                ItemQueue inventoryQueue = (ItemQueue) yamlConfiguration.get(INVENTORY_QUEUE);
                ItemQueue enderchestQueue = (ItemQueue) yamlConfiguration.get(ENDERCHEST_QUEUE);
                this.inventoryQueues.put(uuid, inventoryQueue);
                this.enderchestQueues.put(uuid, enderchestQueue);
            }
        }
    }

    ItemQueue getInventoryQueue(UUID player) {
        return inventoryQueues.computeIfAbsent(player, k -> new ItemQueue());
    }

    ItemQueue getEnderchestQueue(UUID player) {
        return enderchestQueues.computeIfAbsent(player, k -> new ItemQueue());
    }

    void enqueueInventory(UUID player, ItemStack items) {
        ItemQueue invQueue = getInventoryQueue(player);
        invQueue.addItems(items);
        save(player, invQueue, getEnderchestQueue(player));
    }

    void enqueueEnderchest(UUID player, ItemStack items) {
        ItemQueue enderQueue = getEnderchestQueue(player);
        enderQueue.addItems(items);
        save(player, getInventoryQueue(player), enderQueue);
    }

    void save(UUID player, ItemQueue inventoryQueue, ItemQueue enderchestQueue) {
        assert player != null && inventoryQueue != null && enderchestQueue != null;

        File saveFile = new File(saveFolder, player.toString() + ".yml");

        if (inventoryQueue.isEmpty() && enderchestQueue.isEmpty()) {
            if (saveFile.exists())
                saveFile.delete();
            inventoryQueues.remove(player);
            enderchestQueues.remove(player);
        }

        else {
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.set(INVENTORY_QUEUE, inventoryQueue);
            yamlConfiguration.set(ENDERCHEST_QUEUE, enderchestQueue);
            try {
                yamlConfiguration.save(saveFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save player save file: " + saveFile.getName(), e);
            }
        }
    }

}
