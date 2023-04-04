package com.janboerman.invsee.spigot.impl_1_19_3_R2;

import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;

class MainBukkitInventoryView extends MainSpectatorInventoryView {

    private final MainNmsContainer nms;

    MainBukkitInventoryView(MainNmsContainer nms) {
        super(nms.creationOptions);
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

    @Override
    public String getTitle() {
        return nms.title;
    }

    @Override
    public @Nullable Difference getTrackedDifference() {
        DifferenceTracker tracker = nms.tracker;
        return tracker == null ? null : tracker.getDifference();
    }
}
