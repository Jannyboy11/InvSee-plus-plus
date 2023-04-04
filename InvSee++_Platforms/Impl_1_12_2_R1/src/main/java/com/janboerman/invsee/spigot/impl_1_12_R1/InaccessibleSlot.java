package com.janboerman.invsee.spigot.impl_1_12_R1;

import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.IInventory;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Slot;

public class InaccessibleSlot extends Slot {

    public InaccessibleSlot(IInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
    }

    @Override
    public boolean isAllowed(ItemStack var0) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return InvseeImpl.EMPTY_STACK;
    }

    @Override
    public boolean hasItem() {
        return false;
    }

    @Override
    public void set(ItemStack var0) {
        this.f(); //updateInventory
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public ItemStack a(int subtractAmount) {
        //return what we get after splitting the ItemStack in our slot: a stack with at most count subtractAmount.
        //since no amount can be subtracted from our inaccessible slot, we always return the empty ItemStack.
        return InvseeImpl.EMPTY_STACK;
    }

    @Override
    public boolean isAllowed(EntityHuman player) {
        return false;
    }
}
