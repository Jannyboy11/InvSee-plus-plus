package com.janboerman.invsee.spigot.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface MainSpectatorInventory extends Inventory {

    public UUID getSpectatedPlayer();

    ItemStack[] getArmourContents();

    void setArmourContents(ItemStack[] armourContents);

    ItemStack[] getOffHandContents();

    void setOffHandContents(ItemStack[] offHand);

    String getTitle();

}
