package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.internal.inventory.EnderInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;

public class EnderBukkitInventory extends CraftInventory implements EnderInventory<EnderNmsInventory, EnderBukkitInventory> {

    protected EnderBukkitInventory(EnderNmsInventory inventory) {
        super(inventory);
    }

    @Override
    public EnderNmsInventory getInventory() {
        return (EnderNmsInventory) super.getInventory();
    }

    @Override
    public void setContents(EnderSpectatorInventory newContents) {
        super.setContents(newContents.getContents());
    }

    @Override
    public ItemStack[] getStorageContents() {
        return super.getContents();
    }

    @Override
    public void setStorageContents(ItemStack[] storageContents) {
        super.setContents(storageContents);
    }

}
