package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import com.janboerman.invsee.utils.UUIDHelper;
import net.minecraft.server.v1_16_R3.*;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class EnderNmsInventory extends TileEntityContainer /* cannot extend AbstractNmsInventory unfortunately */ implements ShallowCopy<EnderNmsInventory> {

    private static final TileEntityTypes TileEntityTypeFakeEnderChest = new TileEntityTypes(EnderNmsInventory::new, Set.of(), new com.mojang.datafixers.types.constant.EmptyPart());

    protected final UUID spectatedPlayerUuid;
    protected final String spectatedPlayerName;
    protected NonNullList<ItemStack> storageContents;

    private EnderBukkitInventory bukkit;
    protected CreationOptions<EnderChestSlot> creationOptions;

    private int maxStack = IInventory.MAX_STACK;
    private final List<HumanEntity> transaction = new ArrayList<>();
    protected InventoryHolder owner;

    private EnderNmsInventory() {   //used for the fake tile entity type
        super(TileEntityTypeFakeEnderChest);
        spectatedPlayerUuid = null;
        spectatedPlayerName = null;
        storageContents = null;
    }

    EnderNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, NonNullList<ItemStack> storageContents, CreationOptions<EnderChestSlot> creationOptions) {
        // Possibly could've used TileEntityTypes.ENDER_CHEST, but I'm afraid that will cause troubles elsewhere.
        // So use the fake type for now.
        // All of this hadn't been necessary if craftbukkit checked whether the inventory was an instance of ITileEntityContainer instead of straight up TileEntityContainer.
        super(TileEntityTypeFakeEnderChest);
        this.spectatedPlayerUuid = UUIDHelper.copy(spectatedPlayerUuid);
        this.spectatedPlayerName = spectatedPlayerName;
        this.storageContents = storageContents;
        this.creationOptions = creationOptions;
    }

    public EnderBukkitInventory bukkit() {
        return bukkit == null ? bukkit = new EnderBukkitInventory(this) : bukkit;
    }

    @Override
    public int defaultMaxStack() {
        return IInventory.MAX_STACK;
    }

    @Override
    public void shallowCopyFrom(EnderNmsInventory from) {
        this.storageContents = from.storageContents;
        update();
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
        return CraftChatMessage.fromStringOrNull(creationOptions.getTitle().titleFor(Target.byGameProfile(spectatedPlayerUuid, spectatedPlayerName)));
    }

    @Override
    protected IChatBaseComponent getContainerName() {
        return new ChatMessage("minecraft:generic_9x" + (storageContents.size() / 9));
    }

    @Override
    protected Container createContainer(int containerId, PlayerInventory playerInventory) {
        return new EnderNmsContainer(containerId, this, playerInventory, playerInventory.player, creationOptions);
    }

    @Override
    public boolean e(EntityHuman entityhuman) {
        //there is no chestlock here
        return true;
    }

}