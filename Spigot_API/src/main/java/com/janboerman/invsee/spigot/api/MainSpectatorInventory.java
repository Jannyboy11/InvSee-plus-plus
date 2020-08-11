package com.janboerman.invsee.spigot.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface MainSpectatorInventory extends Inventory, SpectatorInventory {

    ItemStack[] getArmourContents();

    void setArmourContents(ItemStack[] armourContents);

    ItemStack[] getOffHandContents();

    void setOffHandContents(ItemStack[] offHand);

}
