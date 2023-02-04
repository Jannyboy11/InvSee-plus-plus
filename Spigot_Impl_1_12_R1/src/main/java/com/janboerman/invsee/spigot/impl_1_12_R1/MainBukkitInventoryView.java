package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.PlayerInventory;

class MainBukkitInventoryView extends MainSpectatorInventoryView {

    private final MainNmsContainer nms;

    MainBukkitInventoryView(MainNmsContainer nms) {
        this.nms = nms;
    }

    @Override
    public MainSpectatorInventory getTopInventory() {
        return nms.top.bukkit();
    }

    @Override
    public PlayerInventory getBottomInventory() {
        return nms.player.getBukkitEntity().getInventory();
    }

    @Override
    public HumanEntity getPlayer() {
        return nms.player.getBukkitEntity();
    }

}
