package com.janboerman.invsee.spigot.api;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface EnderSpectatorInventory extends Inventory {

    public UUID getSpectatedPlayer();

    public String getTitle();

}
