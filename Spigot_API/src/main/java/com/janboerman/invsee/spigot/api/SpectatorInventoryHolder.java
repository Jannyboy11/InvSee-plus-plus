package com.janboerman.invsee.spigot.api;

import org.bukkit.inventory.InventoryHolder;

public interface SpectatorInventoryHolder extends InventoryHolder {

    @Override
    public SpectatorInventory getInventory();

}
