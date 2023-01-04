package com.janboerman.invsee.spigot.internal.inventory;

import org.bukkit.inventory.InventoryView;

public interface Watchable {

    /** Sets the personal contents according to the InventoryView of the target player. */
    public void watch(InventoryView targetPlayerView);

    /** Sets the personal contents back to the target player's own crafting contents. */
    public void unwatch();

}
