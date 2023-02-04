package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.api.template.SpectatorInventoryView;
import org.bukkit.event.inventory.InventoryType;

public abstract class MainSpectatorInventoryView extends SpectatorInventoryView<PlayerInventorySlot> {

    @Override
    public abstract MainSpectatorInventory getTopInventory();

    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
