package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.MainSpectatorInventoryView;
import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;

import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

class MainBukkitInventoryView extends BukkitInventoryView<PlayerInventorySlot> implements MainSpectatorInventoryView {

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
    public void setCursor(ItemStack itemStack) {
        getPlayer().setItemOnCursor(itemStack);
    }

    @Override
    public ItemStack getCursor() {
        return getPlayer().getItemOnCursor();
    }

    @Override
    public Inventory getInventory(int rawSlot) {
        if (rawSlot == OUTSIDE || rawSlot == -1) {
            return null;
        } else {
            Preconditions.checkArgument(rawSlot >= 0, "Negative, non outside slot %s", rawSlot);
            Preconditions.checkArgument(rawSlot < this.countSlots(), "Slot %s greater than inventory slot count", rawSlot);
            return rawSlot < this.getTopInventory().getSize() ? this.getTopInventory() : this.getBottomInventory();
        }
    }

    @Override
    public int convertSlot(int rawSlot) {
        int topSize = getTopInventory().getSize();
        if (rawSlot < topSize) {
            return rawSlot;
        } else {
            int slot = rawSlot - topSize;
            if (slot >= 27) {
                slot -= 27;
            } else {
                slot += 9;
            }
            return slot;
        }
    }

    @Override
    public InventoryType.SlotType getSlotType(int slot) {
        if (slot < 0) {
            return InventoryType.SlotType.OUTSIDE;
        } else {
            int slotCount = countSlots();
            if (slotCount - 9 <= slot && slot < slotCount) {
                return InventoryType.SlotType.QUICKBAR;
            } else {
                return InventoryType.SlotType.CONTAINER;
            }
        }
    }

    @Override
    public void close() {
        getPlayer().closeInventory();
    }

    @Override
    public int countSlots() {
        return getTopInventory().getSize() + getBottomInventory().getStorageContents().length;
    }

    @Override
    public boolean setProperty(Property property, int value) {
        return getPlayer().setWindowProperty(property, value);
    }

    @Override
    public @Nullable Difference getTrackedDifference() {
        DifferenceTracker tracker = nms.tracker;
        return tracker == null ? null : tracker.getDifference();
    }
}
