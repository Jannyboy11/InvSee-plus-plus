package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogOutput;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import net.minecraft.server.v1_16_R3.*;

import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class EnderNmsContainer extends Container {

    final EntityHuman player;
    final EnderNmsInventory top;
    final IInventory bottom;
    final String title;
    final CreationOptions<EnderChestSlot> creationOptions;

    private final int topRows;  //https://github.com/pl3xgaming/Purpur a fork of paper that has configurable rows for the enderchest inventory
    private EnderBukkitInventoryView bukkitView;
    final DifferenceTracker tracker;

    private static Containers determineContainerType(EnderNmsInventory inv) {
        switch (inv.getSize()) {
            case 9: return Containers.GENERIC_9X1;
            case 18: return Containers.GENERIC_9X2;
            case 27: return Containers.GENERIC_9X3;
            case 36: return Containers.GENERIC_9X4;
            case 45: return Containers.GENERIC_9X5;
            case 54: return Containers.GENERIC_9X6;
        }
        return Containers.GENERIC_9X3;
    }

    private static Slot makeSlot(Mirror<EnderChestSlot> mirror, EnderNmsInventory top, int positionIndex, int magicX, int magicY, ItemStack inaccessiblePlaceholder) {
        final EnderChestSlot place = mirror.getSlot(positionIndex);

        if (place == null) {
            return new InaccessiblePlaceholderSlot(inaccessiblePlaceholder, top, positionIndex, magicX, magicY);
        } else {
            final int referringTo = place.ordinal();
            return new Slot(top, referringTo, magicX, magicY);
        }
    }

    //clicked
    @Override
    public ItemStack a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
        List<org.bukkit.inventory.ItemStack> contentsBefore = null, contentsAfter;
        if (tracker != null) {
            contentsBefore = top.getContents().stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
        }

        ItemStack result = super.a(i, j, inventoryclicktype, entityhuman);

        if (tracker != null) {
            contentsAfter = top.getContents().stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
            tracker.onClick(contentsBefore, contentsAfter);
        }

        return result;
    }

    //removed
    @Override
    public void b(EntityHuman entityhuman) {
        super.b(entityhuman);

        if (tracker != null && Objects.equals(entityhuman, player)) {
            tracker.onClose();
        }
    }

    EnderNmsContainer(int containerId, EnderNmsInventory nmsInventory, PlayerInventory playerInventory, EntityHuman player, CreationOptions<EnderChestSlot> creationOptions) {
        super(determineContainerType(nmsInventory), containerId);
        this.topRows = nmsInventory.getSize() / 9;
        this.player = player;
        this.top = nmsInventory;
        this.bottom = playerInventory;

        //creation options
        this.creationOptions = creationOptions;
        //title
        this.title = creationOptions.getTitle().titleFor(Target.byGameProfile(nmsInventory.spectatedPlayerUuid, nmsInventory.spectatedPlayerName));
        //mirror
        Mirror<EnderChestSlot> mirror = creationOptions.getMirror();
        //logging
        LogOptions logOptions = creationOptions.getLogOptions();
        Plugin plugin = creationOptions.getPlugin();
        if (!LogOptions.isEmpty(logOptions)) {
            this.tracker = new DifferenceTracker(
                    LogOutput.make(plugin, player.getUniqueID(), player.getName(), Target.byGameProfile(nmsInventory.spectatedPlayerUuid, nmsInventory.spectatedPlayerName), logOptions),
                    logOptions.getGranularity());
            this.tracker.onOpen();
        } else {
            this.tracker = null;
        }

        // Magma compatability:
        // IT IS IMPORTANT THAT WE CALL super.a(slot) AND NOT this.a(slot). THIS IS BECAUSE OF A BUG IN THE MAGMA REMAPPER!
        // https://github.com/Jannyboy11/InvSee-plus-plus/issues/43#issuecomment-1493377971

        //top inventory slots
        for (int yPos = 0; yPos < topRows; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 18 + yPos * 18;
                super.a(makeSlot(mirror, top, index, magicX, magicY, CraftItemStack.asNMSCopy(creationOptions.getPlaceholderPalette().inaccessible())));
            }
        }

        //bottom inventory slots
        int magicAddY = (topRows - 4 /*4 for 4 rows of the bottom inventory??*/) * 18;

        //player 'storage'
        for (int yPos = 1; yPos < 4; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 103 + yPos * 18 + magicAddY;
                super.a(new Slot(playerInventory, index, magicX, magicY));
            }
        }

        //player 'hotbar'
        for (int xPos = 0; xPos < 9; xPos++) {
            int index = xPos;
            int magicX = 8 + xPos * 18;
            int magicY = 161 + magicAddY;
            super.a(new Slot(playerInventory, index, magicX, magicY));
        }
    }

    @Override
    public EnderBukkitInventoryView getBukkitView() {
        if (bukkitView == null) {
            bukkitView = new EnderBukkitInventoryView(this);
        }
        return bukkitView;
    }

    @Override
    public boolean canUse(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public ItemStack shiftClick(EntityHuman entityhuman, int rawIndex) {
        //returns EMPTY_STACK when we are done transferring the itemstack on the rawIndex
        //remember that we are called inside the body of a loop!

        List<Slot> slots = super.slots;

        ItemStack itemstack = InvseeImpl.EMPTY_STACK;
        Slot slot = slots.get(rawIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack clickedSlotItem = slot.getItem();

            itemstack = clickedSlotItem.cloneItemStack();
            if (rawIndex < topRows * 9) {
                //clicked in the top inventory
                if (!doShiftClickTransfer(clickedSlotItem, topRows * 9, slots.size(), true)) {
                    return InvseeImpl.EMPTY_STACK;
                }
            } else {
                //clicked in the bottom inventory
                if (!doShiftClickTransfer(clickedSlotItem, 0, topRows * 9, false)) {
                    return InvseeImpl.EMPTY_STACK;
                }
            }

            if (clickedSlotItem.isEmpty()) {
                slot.set(InvseeImpl.EMPTY_STACK);
            } else {
                slot.d();
            }
        }

        return itemstack;
    }

    private boolean doShiftClickTransfer(ItemStack clickedSlotItem, int targetMinIndex, int targetMaxIndex, boolean topClicked) {
        //returns true is something if part of the clickedSlotItem was transferred, otherwise false
        return super.a(clickedSlotItem, targetMinIndex, targetMaxIndex, topClicked);
    }

}

