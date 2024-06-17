package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;

import org.bukkit.event.inventory.InventoryType;

/**
 * Represents an open window for an {@link EnderSpectatorInventory}.
 */
public interface EnderSpectatorInventoryView extends SpectatorInventoryView<EnderChestSlot> {

    /** {@inheritDoc} */
    @Override
    public EnderSpectatorInventory getTopInventory();

    /**
     * Get the inventory type.
     * @return the inventory type
     */
    public default InventoryType getType() {
        return InventoryType.CHEST;
    }

}
