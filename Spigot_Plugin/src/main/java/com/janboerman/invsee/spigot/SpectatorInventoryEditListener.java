package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class SpectatorInventoryEditListener implements Listener {

    private static final String
            INVENTORY_EDIT_PERMISSION = "invseeplusplus.invsee.edit",
            ENDERCHEST_EDIT_PERMISSION = "invseeplusplus.endersee.edit";

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory(); //this is the top inventory from the view, not the clicked inventory!
        HumanEntity player = event.getWhoClicked();
        if ((inventory instanceof MainSpectatorInventory && !player.hasPermission(INVENTORY_EDIT_PERMISSION))
                || (inventory instanceof EnderSpectatorInventory && !player.hasPermission(ENDERCHEST_EDIT_PERMISSION))) {
            event.setCancelled(true);
        }
    }

}
