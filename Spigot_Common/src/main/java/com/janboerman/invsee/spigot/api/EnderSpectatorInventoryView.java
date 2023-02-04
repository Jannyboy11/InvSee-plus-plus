package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import org.bukkit.event.inventory.InventoryType;

public abstract class EnderSpectatorInventoryView extends SpectatorInventoryView<EnderChestSlot> {

    protected EnderSpectatorInventoryView(CreationOptions<EnderChestSlot> creationOptions) {
        super(creationOptions);
    }

    @Override
    public abstract EnderSpectatorInventory getTopInventory();

    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
