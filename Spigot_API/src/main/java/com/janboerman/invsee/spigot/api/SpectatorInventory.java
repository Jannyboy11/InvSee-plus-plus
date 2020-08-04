package com.janboerman.invsee.spigot.api;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface SpectatorInventory extends Inventory {

    public UUID getSpectatedPlayer();

}
