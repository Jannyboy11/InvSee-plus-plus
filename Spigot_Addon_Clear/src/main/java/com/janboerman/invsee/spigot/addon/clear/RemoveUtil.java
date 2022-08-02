package com.janboerman.invsee.spigot.addon.clear;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

class RemoveUtil {

    private RemoveUtil() {}

    static int removeAtMost(Inventory inventory, Material material, int atMost) {
        int removed = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && itemStack.getType() == material) {
                final int stackAmount = itemStack.getAmount();
                final int subtractAmount = Math.min(stackAmount, atMost);

                itemStack.setAmount(stackAmount - subtractAmount);
                inventory.setItem(i, itemStack);

                atMost -= subtractAmount;
                removed += subtractAmount;

                if (atMost == 0) break;
            }
        }

        return removed;
    }

}
