package com.janboerman.invsee.spigot.impl_1_16;

import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.IInventory;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.Slot;

public class InAccessibleSlot extends Slot {
    public InAccessibleSlot(IInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
    }

    public boolean isAllowed(ItemStack var0) {
        return false;
    }

    public ItemStack getItem() {
        return MainNmsInventory.EMPTY_STACK;
    }

    public boolean hasItem() {
        return false;
    }

    public void set(ItemStack var0) {
        this.d(); //updateInventory
    }

    public int getMaxStackSize() {
        return 0;
    }

    public ItemStack a(int subtractAmount) {
        //return what we get after splitting the ItemStack in our slot: a stack with at most count subtractAmount.
        //since no amount can be subtracted from our inaccessible slot, we always return the empty ItemStack.
        return MainNmsInventory.EMPTY_STACK;
    }

    public boolean isAllowed(EntityHuman player) {
        return false;
    }
}
