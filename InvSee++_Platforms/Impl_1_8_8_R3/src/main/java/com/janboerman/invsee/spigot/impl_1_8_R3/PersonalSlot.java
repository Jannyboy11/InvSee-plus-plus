package com.janboerman.invsee.spigot.impl_1_8_R3;

import static com.janboerman.invsee.spigot.impl_1_8_R3.HybridServerSupport.slot;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Slot;

class PersonalSlot extends Slot {

    //a slot in the MainNmsContainer that sits on the 6th row representing the items in the player's crafting inventory
    //which could be either the 4-slot part in the player's own inventory, or a real workbench inventory.
    //this class could be used for slots other personal-inventories too such as anvils, enchanting tables, etc.
    PersonalSlot(MainNmsInventory inventory, int index, int xPos, int yPos) {
        super(inventory, index, xPos, yPos);
    }

    boolean works() {
        MainNmsInventory inv = (MainNmsInventory) inventory;
        int personalSize = inv.personalContents.length;
        boolean inRange = 45 <= slot(this) && slot(this) < 45 + personalSize;
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
        if (!works()) this.f(); //updateInventory
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
}
