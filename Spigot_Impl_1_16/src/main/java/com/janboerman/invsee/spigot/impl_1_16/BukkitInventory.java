package com.janboerman.invsee.spigot.impl_1_16;

import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventory;

public class BukkitInventory extends CraftInventory {

    public BukkitInventory(NmsInventory nmsInventory) {
        super(nmsInventory);
    }

}
