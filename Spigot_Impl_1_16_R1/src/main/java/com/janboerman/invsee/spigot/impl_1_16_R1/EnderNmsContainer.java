package com.janboerman.invsee.spigot.impl_1_16_R1;

import net.minecraft.server.v1_16_R1.*;

import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class EnderNmsContainer extends Container {

    private final EntityHuman player;
    private final EnderNmsInventory top;
    private final IInventory bottom;
    private final int topRows;  //https://github.com/pl3xgaming/Purpur a fork of paper that has configurable rows for the enderchest inventory

    private InventoryView bukkitView;

    private static Containers determineContainerType(EnderNmsInventory inv) {
        switch (inv.getSize()) {
            case 9: return Containers.GENERIC_9X1;
            case 18: return Containers.GENERIC_9X2;
            case 27: return Containers.GENERIC_9X3;
            case 36: return Containers.GENERIC_9X4;
            case 45: return Containers.GENERIC_9X5;
            case 54: return Containers.GENERIC_9X6;
        }
        return Containers.GENERIC_9X3;
    }

    public EnderNmsContainer(int containerId, EnderNmsInventory nmsInventory, PlayerInventory playerInventory, EntityHuman player) {
        super(determineContainerType(nmsInventory), containerId);
        this.topRows = nmsInventory.getSize() / 9;
        this.player = player;
        this.top = nmsInventory;
        this.bottom = playerInventory;
        //setTitle(nmsInventory.getScoreboardDisplayName());

        //top inventory slots
        for (int yPos = 0; yPos < topRows; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 18 + yPos * 18;
                a(new Slot(top, index, magicX, magicY));
            }
        }

        //bottom inventory slots
        int magicAddY = (topRows - 4 /*4 for 4 rows of the bottom inventory??*/) * 18;

        //player 'storage'
        for (int yPos = 1; yPos < 4; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 103 + yPos * 18 + magicAddY;
                a(new Slot(playerInventory, index, magicX, magicY));
            }
        }

        //player 'hotbar'
        for (int xPos = 0; xPos < 9; xPos++) {
            int index = xPos;
            int magicX = 8 + xPos * 18;
            int magicY = 161 + magicAddY;
            a(new Slot(playerInventory, index, magicX, magicY));
        }
    }

    @Override
    public InventoryView getBukkitView() {
        if (bukkitView == null) {
            bukkitView = new CraftInventoryView(player.getBukkitEntity(), top.bukkit, this);
        }
        return bukkitView;
    }

    @Override
    public boolean canUse(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public ItemStack shiftClick(EntityHuman entityhuman, int rawIndex) {
        //returns EMPTY_STACK when we are done transferring the itemstack on the rawIndex
        //remember that we are called inside the body of a loop!

        ItemStack itemstack = InvseeImpl.EMPTY_STACK;
        Slot slot = this.slots.get(rawIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack clickedSlotItem = slot.getItem();

            itemstack = clickedSlotItem.cloneItemStack();
            if (rawIndex < topRows * 9) {
                //clicked in the top inventory
                if (!doShiftClickTransfer(clickedSlotItem, topRows * 9, this.slots.size(), true)) {
                    return InvseeImpl.EMPTY_STACK;
                }
            } else {
                //clicked in the bottom inventory
                if (!doShiftClickTransfer(clickedSlotItem, 0, topRows * 9, false)) {
                    return InvseeImpl.EMPTY_STACK;
                }
            }

            if (clickedSlotItem.isEmpty()) {
                slot.set(InvseeImpl.EMPTY_STACK);
            } else {
                slot.d();
            }
        }

        return itemstack;
    }

    private boolean doShiftClickTransfer(ItemStack clickedSlotItem, int targetMinIndex, int targetMaxIndex, boolean topClicked) {
        //returns true is something if part of the clickedSlotItem was transferred, otherwise false
        return super.a(clickedSlotItem, targetMinIndex, targetMaxIndex, topClicked);
    }
}
