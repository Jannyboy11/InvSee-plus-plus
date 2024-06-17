package com.janboerman.invsee.spigot.impl_1_8_R3;

import javax.annotation.Nullable;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;

class EnderBukkitInventoryView extends BukkitInventoryView<EnderChestSlot> implements EnderSpectatorInventoryView {

    final EnderNmsContainer nms;

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
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);
        if (slot >= 0) {
            nms.getSlot(slot).set(stack);
        } else {
            nms.player.drop(stack, false);
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0) {
            return null;
        } else {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack;
            if (slot < nms.top.getSize()) {
                nmsStack = nms.top.getContents()[slot];
            } else {
                nmsStack = nms.getSlot(slot).getItem();
            }
            return CraftItemStack.asCraftMirror(nmsStack);
        }
    }

    @Override
    public @Nullable Difference getTrackedDifference() {
        DifferenceTracker tracker = nms.tracker;
        return tracker == null ? null : tracker.getDifference();
    }

}
