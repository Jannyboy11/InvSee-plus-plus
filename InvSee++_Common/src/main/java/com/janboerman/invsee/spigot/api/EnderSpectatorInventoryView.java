package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import org.bukkit.event.inventory.InventoryType;

/**
 * Represents an open window for an {@link EnderSpectatorInventory}.
 */
public abstract class EnderSpectatorInventoryView extends SpectatorInventoryView<EnderChestSlot> {

    protected EnderSpectatorInventoryView(CreationOptions<EnderChestSlot> creationOptions) {
        super(creationOptions);
    }

    /** {@inheritDoc} */
    @Override
    public abstract EnderSpectatorInventory getTopInventory();

    /** {@inheritDoc} */
    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
