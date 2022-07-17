package com.janboerman.invsee.spigot.addon.give;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

@SerializableAs("ItemQueue")
class ItemQueue implements ConfigurationSerializable {

    private Deque<ItemStack> queue;

    ItemQueue() {
        this.queue = new ArrayDeque<>();
    }

    private ItemQueue(Collection<? extends ItemStack> queue) {
        this();
        this.queue.addAll(queue);
    }

    void addItems(ItemStack items) {
        assert items != null;
        queue.add(items);
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Processes the queue. Tries to insert all the items in the queue into the given inventory.
     * @param inventory the inventory to fill
     * @param targetUsername the username of the target player
     * @param console the console to log to
     * @return true if the queue was emptied, false otherwise.
     */
    boolean process(Inventory inventory, ConsoleCommandSender console, String targetUsername, String inventoryType) {
        boolean success = true;
        ItemStack last = null;
        while (!queue.isEmpty() && success) {
            ItemStack item = queue.poll();
            ItemStack clone = item.clone();
            Map<Integer, ItemStack> map = inventory.addItem(item);
            success = map.isEmpty();
            if (!success) {
                last = map.get(0);
            } else {
                console.sendMessage(ChatColor.GREEN + "[Queue] Added " + clone + " to " + targetUsername + "'s " + inventoryType + "!");
            }
        }

        if (!success) {
            assert last != null;
            queue.addFirst(last);
        }

        return success;
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of("queue", queue);
    }

    public static ItemQueue deserialize(Map<String, Object> map) {
        List<ItemStack> queue = (List<ItemStack>) map.get("queue");
        return new ItemQueue(queue);
    }

}
