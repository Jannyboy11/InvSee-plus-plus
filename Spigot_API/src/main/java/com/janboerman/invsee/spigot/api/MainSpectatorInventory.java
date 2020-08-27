package com.janboerman.invsee.spigot.api;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public interface MainSpectatorInventory extends SpectatorInventory {

    void watch(InventoryView targetPlayerView);

    void unwatch();

    ItemStack[] getArmourContents();

    void setArmourContents(ItemStack[] armourContents);

    ItemStack[] getOffHandContents();

    void setOffHandContents(ItemStack[] offHand);

    void setCursorContents(ItemStack cursor);

    ItemStack getCursorContents();

    void setPersonalContents(ItemStack[] craftingContents);

    ItemStack[] getPersonalContents();

    int getPersonalContentsSize();

}
