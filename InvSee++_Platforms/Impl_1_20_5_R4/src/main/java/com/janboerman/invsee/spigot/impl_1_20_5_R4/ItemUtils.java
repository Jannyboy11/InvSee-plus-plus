package com.janboerman.invsee.spigot.impl_1_20_5_R4;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class ItemUtils {

    static int getMaxStackSize(ItemStack stack) {
        ItemMeta meta;
        if (stack.hasItemMeta() && (meta = stack.getItemMeta()) != null && meta.hasMaxStackSize()) {
            return meta.getMaxStackSize();
        } else {
            return stack.getMaxStackSize();
        }
    }

    static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType().isAir() || stack.getAmount() <= 0;
    }
}
