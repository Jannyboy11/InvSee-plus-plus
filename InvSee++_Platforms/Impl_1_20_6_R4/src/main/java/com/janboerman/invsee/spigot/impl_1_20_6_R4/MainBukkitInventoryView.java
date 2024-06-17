package com.janboerman.invsee.spigot.impl_1_20_6_R4;

import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;

class MainBukkitInventoryView extends BukkitInventoryView<PlayerInventorySlot> {

    final MainNmsContainer nms;

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
    public InventoryType getType() {
        return InventoryType.CHEST;
    }

    @Override
    public String getTitle() {
        return nms.title();
    }

    @Override
    public String getOriginalTitle() {
        return nms.originalTitle;
    }

    @Override
    public void setTitle(String title) {
        CraftInventoryView.sendInventoryTitleChange(this, title);
        nms.title = title;
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
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
            net.minecraft.world.item.ItemStack nmsStack;
            if (slot < nms.top.getContainerSize()) {
                nmsStack = nms.top.getContents().get(slot);
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
