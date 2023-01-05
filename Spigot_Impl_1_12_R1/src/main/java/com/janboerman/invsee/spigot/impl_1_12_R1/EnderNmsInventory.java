package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;

import java.util.List;
import java.util.UUID;

class EnderNmsInventory extends AbstractNmsInventory<EnderChestSlot, EnderNmsInventory> implements IInventory, ITileEntityContainer {

    protected NonNullList<ItemStack> storageContents;

    EnderNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, NonNullList<ItemStack> storageContents, String title, Mirror<EnderChestSlot> mirror) {
        super(spectatedPlayerUuid, spectatedPlayerName, title, mirror);
        this.storageContents = storageContents;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public int defaultMaxStack() {
        return IInventory.MAX_STACK;
    }

    @Override
    public void shallowCopyFrom(EnderNmsInventory from) {
        setMaxStackSize(from.getMaxStackSize());
        this.storageContents = from.storageContents;
        update();
    }

    @Override
    public int getSize() {
        return storageContents.size();
    }

    @Override
    public boolean x_() { // isEmpty
        for (ItemStack stack : storageContents) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= getSize()) return InvseeImpl.EMPTY_STACK;

        return storageContents.get(slot);
    }

    @Override
    public ItemStack splitStack(int slot, int subtractAmount) {
        if (slot < 0 || slot >= getSize()) return InvseeImpl.EMPTY_STACK;

        ItemStack stack = ContainerUtil.a(storageContents, slot, subtractAmount);
        if (!stack.isEmpty()) {
            update();
        }
        return stack;
    }

    @Override
    public ItemStack splitWithoutUpdate(int slot) {
        if (slot < 0 || slot >= getSize()) return InvseeImpl.EMPTY_STACK;

        var stack = storageContents.get(slot);
        if (stack.isEmpty()) {
            return InvseeImpl.EMPTY_STACK;
        } else {
            storageContents.set(slot, InvseeImpl.EMPTY_STACK);
            return stack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if (slot < 0 || slot >= getSize()) return;

        storageContents.set(slot, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }

        update();
    }

    @Override
    public void update() {
        //called after an item in the inventory was removed, added or updated.
        //looking at InventorySubContainer, I don't think we need to do anything here.

        //but just to be sure, we make sure our viewers get an updated view!
//        for (HumanEntity viewer : getViewers()) {
//            if (viewer instanceof Player) {
//                ((Player) viewer).updateInventory();
//            }
//        }
    }

    @Override
    public boolean a(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public void startOpen(EntityHuman entityHuman) {
        onOpen(entityHuman.getBukkitEntity());
    }

    @Override
    public void closeContainer(EntityHuman entityHuman) {
        onClose(entityHuman.getBukkitEntity());
    }

    @Override
    public boolean b(int slot, ItemStack itemStack) { //allowHopperInput
        return true;
    }

    @Override
    public int getProperty(int idx) {
        return 0;
    }

    @Override
    public void setProperty(int idx, int prop) {
    }

    @Override
    public int h() { //property count
        return 0;
    }

    @Override
    public List<ItemStack> getContents() {
        return storageContents;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        super.onOpen(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
    }

    @Override
    public void clear() {
        storageContents.clear();
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public boolean hasCustomName() {
        return title != null;
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return CraftChatMessage.fromString(title)[0];
    }

    @Override
    public Container createContainer(PlayerInventory playerInventory, EntityHuman entityHuman) {
        EntityPlayer entityPlayer = (EntityPlayer) entityHuman;
        return new EnderNmsContainer(entityPlayer.nextContainerCounter(), this, playerInventory, playerInventory.player, mirror);
    }

    @Override
    public String getContainerName() {
        return "minecraft:container";
    }

}
