package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import org.bukkit.inventory.ItemStack;

/**
 * The spectator inventory that contains all enderchest items
 */
public interface EnderSpectatorInventory extends SpectatorInventory<EnderChestSlot> {

    /** Get the mirror this inventory is viewed through. */
    @Override
    public default Mirror<EnderChestSlot> getMirror() {
        return getCreationOptions().getMirror();
    }

    /** Set the contents of this inventory based on the contents from the provided inventory. */
    public default void setContents(EnderSpectatorInventory newContents) {
        setContents(newContents.getContents());
    }

    @Override
    public default void setStorageContents(ItemStack[] newContents) {
        setContents(newContents);
    }

    @Override
    public default ItemStack[] getStorageContents() {
        return getContents();
    }

}
