package com.janboerman.invsee.spigot.impl_1_15_R1;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class EnderNmsInventory extends TileEntityContainer {

    private static final TileEntityTypes TileEntityTypeFakeEnderChest = new TileEntityTypes(EnderNmsInventory::new, Set.of(), new com.mojang.datafixers.types.constant.NilSave());

    protected final UUID spectatedPlayerUuid;
    protected final String spectatedPlayerName;
    protected final NonNullList<ItemStack> storageContents;

    protected Inventory bukkit;
    protected String title;
    protected Mirror<EnderChestSlot> mirror = Mirror.defaultEnderChest();

    private int maxStack = IInventory.MAX_STACK;
    private final List<HumanEntity> transaction = new ArrayList<>();
    protected InventoryHolder owner;

    private EnderNmsInventory() {   //used for the fake tile entity type
        super(TileEntityTypeFakeEnderChest);
        spectatedPlayerUuid = null;
        spectatedPlayerName = null;
        storageContents = null;
    }

    EnderNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, NonNullList<ItemStack> storageContents) {
        // Possibly could've used TileEntityTypes.ENDER_CHEST, but I'm afraid that will cause troubles elsewhere.
        // So use the fake type for now.
        // All of this hadn't been necessary if craftbukkit checked whether the inventory was an instance of ITileEntityContainer instead of straight up TileEntityContainer.
        super(TileEntityTypeFakeEnderChest);
        this.spectatedPlayerUuid = spectatedPlayerUuid;
        this.spectatedPlayerName = spectatedPlayerName;
        this.storageContents = storageContents;
    }

    EnderNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, NonNullList<ItemStack> storageContents, String title) {
        this(spectatedPlayerUuid, spectatedPlayerName, storageContents);
        this.title = title;
        this.setCustomName(CraftChatMessage.fromStringOrNull(title));
    }

    EnderNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, NonNullList<ItemStack> storageContents, String title, Mirror<EnderChestSlot> mirror) {
        this(spectatedPlayerUuid, spectatedPlayerName, storageContents, title);
        this.mirror = mirror;
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
    protected IChatBaseComponent getContainerName() {
        return new ChatMessage("minecraft:generic_9x" + (storageContents.size() / 9));
    }

    @Override
    protected Container createContainer(int containerId, PlayerInventory playerInventory) {
        return new EnderNmsContainer(containerId, this, playerInventory, playerInventory.player, mirror);
    }

    @Override
    public boolean e(EntityHuman entityhuman) {
        //there is no chestlock here
        return true;
    }

}