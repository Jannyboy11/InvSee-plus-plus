package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;

import org.bukkit.event.inventory.InventoryType;

public interface MainSpectatorInventoryView extends SpectatorInventoryView<PlayerInventorySlot> {

    /** {@inheritDoc} */
    @Override
    public MainSpectatorInventory getTopInventory();

    /**
     * Get the inventory type.
     * @return the inventory type
     */
    public default InventoryType getType() {
        return InventoryType.CHEST;
    }

}
