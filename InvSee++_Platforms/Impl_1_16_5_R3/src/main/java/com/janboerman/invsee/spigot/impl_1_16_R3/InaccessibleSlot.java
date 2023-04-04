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


    // ===== Magma compatibility =====
    // https://github.com/Jannyboy11/InvSee-plus-plus/issues/43#issuecomment-1493377971

    public boolean func_75214_a(ItemStack stack) {
        return isAllowed(stack);
    }

    public boolean func_82869_a(EntityHuman playerEntity) {
        return isAllowed(playerEntity);
    }

    public ItemStack func_75211_c() {
        return getItem();
    }

    public boolean func_75216_d() {
        return hasItem();
    }

    public void func_75215_d(ItemStack stack) {
        set(stack);
    }

    public int func_75219_a() {
        return getMaxStackSize();
    }

    public ItemStack func_75209_a(int subtractAmount) {
        //receiveSplit
        return a(subtractAmount);
    }

}
