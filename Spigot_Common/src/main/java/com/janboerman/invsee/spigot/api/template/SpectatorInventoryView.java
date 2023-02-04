package com.janboerman.invsee.spigot.api.template;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.logging.Difference;
import org.bukkit.inventory.InventoryView;

import javax.annotation.Nullable;

public abstract class SpectatorInventoryView<Slot> extends InventoryView {

    @Override
    public abstract SpectatorInventory<Slot> getTopInventory();

    public abstract @Nullable Difference getTrackedDifference();

}
