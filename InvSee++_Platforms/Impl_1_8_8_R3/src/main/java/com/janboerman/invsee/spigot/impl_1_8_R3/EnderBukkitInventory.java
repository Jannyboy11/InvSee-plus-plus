package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.janboerman.invsee.spigot.internal.inventory.EnderInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;

public class EnderBukkitInventory extends CraftInventory implements EnderInventory<EnderNmsInventory, EnderBukkitInventory> {

    protected EnderBukkitInventory(EnderNmsInventory inventory) {
        super(inventory);
    }

    @Override
    public EnderNmsInventory getInventory() {
        return (EnderNmsInventory) super.getInventory();
    }

}
