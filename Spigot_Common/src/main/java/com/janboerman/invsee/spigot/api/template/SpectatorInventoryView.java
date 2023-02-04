package com.janboerman.invsee.spigot.api.template;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import org.bukkit.inventory.InventoryView;

public abstract class SpectatorInventoryView<Slot> extends InventoryView {

    @Override
    public abstract SpectatorInventory<Slot> getTopInventory();

}
