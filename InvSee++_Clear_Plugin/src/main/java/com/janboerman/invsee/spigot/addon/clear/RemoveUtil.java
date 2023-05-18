package com.janboerman.invsee.spigot.addon.clear;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

class RemoveUtil {

    private RemoveUtil() {}

    static int removeAtMost(Inventory inventory, Material material, int atMost) {
        int removed = 0;

        for (int i = 0; i < inventory.getSize() && atMost > 0; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && itemStack.getType() == material) {
                final int stackAmount = itemStack.getAmount();
                final int subtractAmount = Math.min(stackAmount, atMost);

                itemStack.setAmount(stackAmount - subtractAmount);
                inventory.setItem(i, itemStack);

                atMost -= subtractAmount;
                removed += subtractAmount;
            }
        }

        return removed;
    }

    static int removeIfAtMost(Inventory inventory, Predicate<? super ItemStack> predicate, int atMost) {
        int removed = 0;

        for (int i = 0; i < inventory.getSize() && atMost > 0; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && predicate.test(itemStack)) {
                final int stackAmount = itemStack.getAmount();
                final int subtractAmount = Math.min(stackAmount, atMost);

                itemStack.setAmount(stackAmount - subtractAmount);
                inventory.setItem(i, itemStack);

                atMost -= subtractAmount;
                removed += subtractAmount;
            }
        }

        return removed;
    }

    static void removeIf(Inventory inventory, Predicate<? super ItemStack> predicate) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final ItemStack existingStack = inventory.getItem(slot);
            if (existingStack != null && predicate.test(existingStack)) {
                inventory.clear(slot);
            }
        }
    }

}
