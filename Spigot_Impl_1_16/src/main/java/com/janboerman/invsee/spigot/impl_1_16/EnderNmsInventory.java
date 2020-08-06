package com.janboerman.invsee.spigot.impl_1_16;

import com.janboerman.invsee.utils.ConcatList;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class EnderNmsInventory implements IInventory, ITileInventory {
    protected static final ItemStack EMPTY_STACK = ItemStack.b;

    protected final UUID spectatedPlayerUuid;
    protected final NonNullList<ItemStack> storageContents;

    protected Inventory bukkit;
    protected String title;

    private int maxStack = IInventory.MAX_STACK;
    private final List<HumanEntity> transaction = new ArrayList<>();
    protected InventoryHolder owner;

    public EnderNmsInventory(UUID spectatedPlayerUuid, NonNullList<ItemStack> storageContents) {
        this.spectatedPlayerUuid = spectatedPlayerUuid;
        this.storageContents = storageContents;
    }

    public EnderNmsInventory(UUID spectatedPlayerUuid, NonNullList<ItemStack> storageContents, String title) {
        this(spectatedPlayerUuid, storageContents);
        this.title = title;
    }

    @Override
    public int getSize() {
        return storageContents.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : storageContents) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= getSize()) return EMPTY_STACK;

        return storageContents.get(slot);
    }

    @Override
    public ItemStack splitStack(int slot, int subtractAmount) {
        if (slot < 0 || slot >= getSize()) return EMPTY_STACK;

        ItemStack stack = ContainerUtil.a(storageContents, slot, subtractAmount);
        if (!stack.isEmpty()) {
            update();
        }
        return stack;
    }

    @Override
    public ItemStack splitWithoutUpdate(int slot) {
        if (slot < 0 || slot >= getSize()) return EMPTY_STACK;

        var stack = storageContents.get(slot);
        if (stack.isEmpty()) {
            return EMPTY_STACK;
        } else {
            storageContents.set(slot, EMPTY_STACK);
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
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void update() {
        //called after an item in the inventory was removed, added or updated.
        //looking at InventorySubContainer, I don't think we need to do anything here.
    }

    @Override
    public boolean a(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public List<ItemStack> getContents() {
        return storageContents;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return owner;
    }

    @Override
    public void setMaxStackSize(int maxStack) {
        this.maxStack = maxStack;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void clear() {
        storageContents.clear();
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return CraftChatMessage.fromStringOrNull(title);
    }

    @Override
    public Container createMenu(int containerId, PlayerInventory playerInventory, EntityHuman entityHuman) {
        return new EnderNmsContainer(containerId, this, playerInventory, entityHuman);
    }
}
