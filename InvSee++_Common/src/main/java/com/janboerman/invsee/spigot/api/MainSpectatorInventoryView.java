package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.event.inventory.InventoryType;

/**
 * Represents an open window for a {@link MainSpectatorInventory}.
 */
public abstract class MainSpectatorInventoryView extends SpectatorInventoryView<PlayerInventorySlot> {

    protected MainSpectatorInventoryView(CreationOptions<PlayerInventorySlot> creationOptions) {
        super(creationOptions);
    }


    /** {@inheritDoc} */
    @Override
    public abstract MainSpectatorInventory getTopInventory();

    /** {@inheritDoc} */
    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
