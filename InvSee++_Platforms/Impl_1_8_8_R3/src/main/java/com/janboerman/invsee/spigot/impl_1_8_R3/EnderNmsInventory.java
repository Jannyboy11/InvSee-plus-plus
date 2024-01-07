package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;

import java.util.UUID;

class EnderNmsInventory extends AbstractNmsInventory<EnderChestSlot, EnderBukkitInventory, EnderNmsInventory> implements IInventory, ITileEntityContainer {

    protected ItemStack[] storageContents;

    EnderNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, ItemStack[] storageContents, CreationOptions<EnderChestSlot> creationOptions) {
        super(spectatedPlayerUuid, spectatedPlayerName, creationOptions);
        this.storageContents = storageContents;
    }

    @Override
    protected EnderBukkitInventory createBukkit() {
        return new EnderBukkitInventory(this);
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
        return storageContents.length;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= getSize()) return InvseeImpl.EMPTY_STACK;

        return storageContents[slot];
    }

    @Override
    public ItemStack splitStack(int slot, int subtractAmount) {
        ItemStack stack;
        if (slot < 0 || slot >= getSize() || (stack = storageContents[slot]) == null) return InvseeImpl.EMPTY_STACK;

        if (stack.count <= subtractAmount) {
            this.storageContents[slot] = InvseeImpl.EMPTY_STACK;
        } else {
            stack = stack.cloneAndSubtract(subtractAmount);
            if (storageContents[slot].count == 0) {
                storageContents[slot] = InvseeImpl.EMPTY_STACK;
            }
        }

        update();
        return stack;
    }

    @Override
    public ItemStack splitWithoutUpdate(int slot) {
        if (slot < 0 || slot >= getSize()) return InvseeImpl.EMPTY_STACK;

        net.minecraft.server.v1_8_R3.ItemStack stack = storageContents[slot];
        if (stack.count == 0) {
            return InvseeImpl.EMPTY_STACK;
        } else {
            storageContents[slot] = InvseeImpl.EMPTY_STACK;
            return stack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if (slot < 0 || slot >= getSize()) return;

        storageContents[slot] = itemStack;
        if (!InvseeImpl.isEmptyStack(itemStack) && itemStack.count > getMaxStackSize()) {
            itemStack.count = getMaxStackSize();
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
    public void b(int idx, int prop) { //setProperty
    }

    @Override
    public int g() { //property count
        return 0;
    }

    @Override
    public ItemStack[] getContents() {
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
    public void l() { //clear
        for(int i = 0; i < this.storageContents.length; ++i) {
            this.storageContents[i] = InvseeImpl.EMPTY_STACK;
        }
    }

    @Override
    public String getName() {
        return creationOptions.getTitle().titleFor(Target.byGameProfile(targetPlayerUuid, targetPlayerName));
    }

    @Override
    public boolean hasCustomName() {
        return creationOptions.getTitle() != null;
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return CraftChatMessage.fromString(creationOptions.getTitle().titleFor(Target.byGameProfile(targetPlayerUuid, targetPlayerName)))[0];
    }

    @Override
    public Container createContainer(PlayerInventory playerInventory, EntityHuman entityHuman) {
        EntityPlayer entityPlayer = (EntityPlayer) entityHuman;
        return new EnderNmsContainer(entityPlayer.nextContainerCounter(), this, playerInventory, playerInventory.player, creationOptions);
    }

    @Override
    public String getContainerName() {
        return "minecraft:container";
    }

}
