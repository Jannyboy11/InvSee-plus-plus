package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;

class EnderBukkitInventoryView extends EnderSpectatorInventoryView {

    private final EnderNmsContainer nms;

    EnderBukkitInventoryView(EnderNmsContainer nms) {
        super(nms.creationOptions);
        this.nms = nms;
    }

    @Override
    public EnderSpectatorInventory getTopInventory() {
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

    @Override
    public @Nullable Difference getTrackedDifference() {
        DifferenceTracker tracker = nms.tracker;
        return tracker == null ? null : tracker.getDifference();
    }

}
