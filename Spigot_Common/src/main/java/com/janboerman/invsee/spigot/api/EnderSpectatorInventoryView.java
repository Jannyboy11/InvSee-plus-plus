package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.SpectatorInventoryView;
import org.bukkit.event.inventory.InventoryType;

public abstract class EnderSpectatorInventoryView extends SpectatorInventoryView<EnderChestSlot> {

    @Override
    public abstract EnderSpectatorInventory getTopInventory();

    @Override
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

}
