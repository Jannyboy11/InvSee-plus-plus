package com.janboerman.invsee.spigot.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

//TODO rename this thing?
//TODO create a similar thing for enderchests?
public interface SpectatorInventory extends Inventory {

    public UUID getSpectatedPlayer();

    ItemStack[] getArmourContents();

    void setArmourContents(ItemStack[] armourContents);

    ItemStack[] getOffHandContents();

    void setOffHandContents(ItemStack[] offHand);
}
