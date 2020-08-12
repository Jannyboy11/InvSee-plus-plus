package com.janboerman.invsee.spigot.impl_1_16_R1;

import com.janboerman.invsee.utils.ConcatList;
import com.janboerman.invsee.utils.Pair;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class MainNmsInventory extends TileEntityContainer {

    private static final TileEntityTypes TileEntityTypeFakePlayerInventory = new TileEntityTypes(MainNmsInventory::new, Set.of(), new com.mojang.datafixers.types.constant.EmptyPart());

    protected final UUID spectatedPlayerUuid;
    protected final NonNullList<ItemStack> storageContents;
    protected final NonNullList<ItemStack> armourContents;
    protected final NonNullList<ItemStack> offHand;
    protected final String spectatedPlayerName;

    protected Inventory bukkit;
    protected String title;

    private int maxStack = IInventory.MAX_STACK;
    private final List<HumanEntity> transaction = new ArrayList<>();
    protected InventoryHolder owner;

    private MainNmsInventory() { //used for the fake tile entity type
        super(TileEntityTypeFakePlayerInventory);
        spectatedPlayerUuid = null;
        spectatedPlayerName = null;
        storageContents = null;
        armourContents = null;
        offHand = null;
    }

    protected MainNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, NonNullList<ItemStack> storageContents, NonNullList<ItemStack> armourContents, NonNullList<ItemStack> offHand) {
        // Possibly could've used TileEntityTypes.CHEST, but I'm afraid that will cause troubles elsewhere.
        // So use the fake type for now.
        // All of this hadn't been necessary if craftbukkit checked whether the inventory was an instance of ITileEntityContainer instead of straight up TileEntityContainer.
        super(TileEntityTypeFakePlayerInventory);
        this.spectatedPlayerUuid = spectatedPlayerUuid;
        this.spectatedPlayerName = spectatedPlayerName;
        this.storageContents = storageContents;
        this.armourContents = armourContents;
        this.offHand = offHand;
    }

    protected MainNmsInventory(UUID spectatedPlayerUuid, String spectatedPlayerName, NonNullList<ItemStack> storageContents, NonNullList<ItemStack> armourContents, NonNullList<ItemStack> offHand, String title) {
        this(spectatedPlayerUuid, spectatedPlayerName, storageContents, armourContents, offHand);
        this.title = title;
        this.setCustomName(CraftChatMessage.fromStringOrNull(title));
    }

    private Pair<Integer, NonNullList<ItemStack>> decideWhichInv(int slot) {
        if (0 <= slot && slot < storageContents.size()) {
            return new Pair<>(slot, storageContents);
        } else if (slot < storageContents.size() + armourContents.size()) {
            return new Pair<>(slot - storageContents.size(), armourContents);
        } else if (slot < storageContents.size() + armourContents.size() + offHand.size()) {
            return new Pair<>(slot - storageContents.size() - armourContents.size(), offHand);
        } else {
            return null;
        }
    }

    @Override
    public int getSize() {
        int size = storageContents.size() + armourContents.size() + offHand.size();
        int remainder = size % 9;
        if (remainder != 0) {
            size += (9 - remainder);
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : armourContents) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : storageContents) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : offHand) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        var pair = decideWhichInv(slot);
        if (pair == null) return InvseeImpl.EMPTY_STACK;

        return pair.getSecond().get(pair.getFirst());
    }

    @Override
    public ItemStack splitStack(int slot, int subtractAmount) {
        var pair = decideWhichInv(slot);
        if (pair == null) return InvseeImpl.EMPTY_STACK;

        ItemStack stack = ContainerUtil.a(pair.getSecond(), pair.getFirst(), subtractAmount);
        if (!stack.isEmpty()) {
            update();
        }
        return stack;
    }

    @Override
    public ItemStack splitWithoutUpdate(int slot) {
        var pair = decideWhichInv(slot);
        if (pair == null) return InvseeImpl.EMPTY_STACK;

        slot = pair.getFirst();
        var items = pair.getSecond();

        var stack = items.get(slot);
        if (stack.isEmpty()) {
            return InvseeImpl.EMPTY_STACK;
        } else {
            items.set(slot, InvseeImpl.EMPTY_STACK);
            return stack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        var pair = decideWhichInv(slot);
        if (pair == null) return;

        slot = pair.getFirst();
        var items = pair.getSecond();

        items.set(slot, itemStack);
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
        ConcatList<ItemStack> contents = new ConcatList<>(storageContents, new ConcatList<>(armourContents, offHand));
        ConcatList<ItemStack> padded = new ConcatList<>(contents, NonNullList.a(getSize() - contents.size(), InvseeImpl.EMPTY_STACK));
        return padded;
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
        armourContents.clear();
        offHand.clear();
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return CraftChatMessage.fromStringOrNull(title);
    }

    @Override
    protected IChatBaseComponent getContainerName() {
        return new ChatMessage("minecraft:generic_9x5");
    }

    @Override
    protected Container createContainer(int containerId, PlayerInventory playerInventory) {
        return new MainNmsContainer(containerId, this, playerInventory, playerInventory.player);
    }

    @Override
    public boolean e(EntityHuman entityhuman) {
        //there is no chestlock here
        return true;
    }

}
