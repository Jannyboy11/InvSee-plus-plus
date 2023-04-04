package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.event.inventory.InventoryType;

public abstract class MainSpectatorInventoryView extends SpectatorInventoryView<PlayerInventorySlot> {

    protected MainSpectatorInventoryView(CreationOptions<PlayerInventorySlot> creationOptions) {
        super(creationOptions);
    }

    @Override
    public abstract MainSpectatorInventory getTopInventory();

    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
