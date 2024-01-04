package com.janboerman.invsee.spigot.impl_1_16_R3;

import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.IInventory;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.Slot;

class InaccessibleSlot extends Slot {

    InaccessibleSlot(IInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
    }

    @Override
    public boolean isAllowed(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isAllowed(EntityHuman player) {
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
    public void set(ItemStack stack) {
        super.d(); //updateInventory
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

}

class InaccessiblePlaceholderSlot extends InaccessibleSlot {

    private final ItemStack placeholder;

    InaccessiblePlaceholderSlot(ItemStack placeholder, IInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);

        this.placeholder = placeholder;
    }

    @Override
    public ItemStack getItem() {
        return placeholder;
    }

}
