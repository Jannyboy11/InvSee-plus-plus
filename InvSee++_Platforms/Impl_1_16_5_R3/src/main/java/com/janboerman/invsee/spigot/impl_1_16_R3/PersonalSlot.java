package com.janboerman.invsee.spigot.impl_1_16_R3;

import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.Slot;

public class PersonalSlot extends Slot {

    //a slot in the MainNmsContainer that sits on the 6th row representing the items in the player's crafting inventory
    //which could be either the 4-slot part in the player's own inventory, or a real workbench inventory.
    //this class could be used for slots other personal-inventories too such as anvils, grindstones, enchanting tables, looms, etc.
    public PersonalSlot(MainNmsInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
    }

    boolean works() {
        MainNmsInventory inv = (MainNmsInventory) super.inventory;  //because of bug in Magma's remapper, call super.inventory instead of this.inventory.
        int personalSize = inv.personalContents.size();
        boolean inRange = 45 <= HybridServerSupport.slot(this) && HybridServerSupport.slot(this) < 45 + personalSize;
        return inRange;
    }

    @Override
    public boolean isAllowed(ItemStack itemStack) {
        if (!works()) return false;
        return super.isAllowed(itemStack);
    }

    @Override
    public ItemStack getItem() {
        if (!works()) return InvseeImpl.EMPTY_STACK;
        return super.getItem();
    }

    @Override
    public boolean hasItem() {
        if (!works()) return false;
        return super.hasItem();
    }

    @Override
    public void set(ItemStack itemStack) {
        if (!works()) this.d(); //updateInventory
        super.set(itemStack);
    }

    @Override
    public int getMaxStackSize() {
        if (!works()) return 0;
        return super.getMaxStackSize();
    }

    @Override
    public ItemStack a(int subtractAmount) {
        if (!works()) {
            //return what we get after splitting the ItemStack in our slot: a stack with at most count subtractAmount.
            //since no amount can be subtracted from our inaccessible slot, we always return the empty ItemStack.
            return InvseeImpl.EMPTY_STACK;
        } else {
            return super.a(subtractAmount);
        }
    }

    @Override
    public boolean isAllowed(EntityHuman player) {
        if (!works()) return false;
        return super.isAllowed(player);
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
