package com.janboerman.invsee.spigot.impl_1_8_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogOutput;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import net.minecraft.server.v1_8_R3.Slot;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class MainNmsContainer extends Container {

    final EntityHuman player;
    final MainNmsInventory top;
    final PlayerInventory bottom;
    final String title;
    final CreationOptions<PlayerInventorySlot> creationOptions;

    private final boolean spectatingOwnInventory;
    private MainBukkitInventoryView bukkitView;
    final DifferenceTracker tracker;

    private static Slot makeSlot(Mirror<PlayerInventorySlot> mirror, boolean spectatingOwnInventory, MainNmsInventory top, int positionIndex, int magicX, int magicY) {
        final PlayerInventorySlot place = mirror.getSlot(positionIndex);

        if (place == null) {
            return new InaccessibleSlot(top, positionIndex, magicX, magicY);
        } else if (place.isContainer()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.CONTAINER_00.ordinal();
            return new Slot(top, referringTo, magicX, magicY); //magicX and magicY correct here? it seems to work though.
        } else if (place == PlayerInventorySlot.ARMOUR_BOOTS) {
            final int referringTo = 36;
            return new BootsSlot(top, referringTo, magicX, magicY); //idem?
        } else if (place == PlayerInventorySlot.ARMOUR_LEGGINGS) {
            final int referringTo = 37;
            return new LeggingsSlot(top, referringTo, magicX, magicY); //idem?
        } else if (place == PlayerInventorySlot.ARMOUR_CHESTPLATE) {
            final int referringTo = 38;
            return new ChestplateSlot(top, referringTo, magicX, magicY); //idem?
        } else if (place == PlayerInventorySlot.ARMOUR_HELMET) {
            final int referringTo = 39;
            return new HelmetSlot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isPersonal()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.PERSONAL_00.ordinal() + 45;
            return new PersonalSlot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isCursor() && !spectatingOwnInventory) {
            final int referringTo = 41;
            return new CursorSlot(top, referringTo, magicX, magicY); //idem?
        } else {
            return new InaccessibleSlot(top, positionIndex, magicX, magicY); //idem?
        }
    }

    //clicked
    @Override
    public ItemStack clickItem(int i, int j, int inventoryclicktype, EntityHuman entityhuman) {
        List<org.bukkit.inventory.ItemStack> contentsBefore = null, contentsAfter;
        if (tracker != null) {
            contentsBefore = Arrays.stream(top.getContents()).map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
        }

        ItemStack result = super.clickItem(i, j, inventoryclicktype, entityhuman);

        if (tracker != null) {
            contentsAfter = Arrays.stream(top.getContents()).map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
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

    MainNmsContainer(int containerId, MainNmsInventory nmsInventory, PlayerInventory playerInventory, EntityHuman player, CreationOptions<PlayerInventorySlot> creationOptions) {
        this.windowId = containerId;

        this.top = nmsInventory;
        this.bottom = playerInventory;
        this.player = player;
        this.spectatingOwnInventory = player.getUniqueID().equals(nmsInventory.targetPlayerUuid);

        nmsInventory.startOpen(player);

        //creation options
        this.creationOptions = creationOptions;
        //title
        this.title = creationOptions.getTitle().titleFor(Target.byGameProfile(nmsInventory.targetPlayerUuid, nmsInventory.targetPlayerName));
        //mirror
        Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
        //logging
        LogOptions logOptions = creationOptions.getLogOptions();
        Plugin plugin = creationOptions.getPlugin();
        if (!LogOptions.isEmpty(logOptions)) {
            this.tracker = new DifferenceTracker(
                    LogOutput.make(plugin, player.getUniqueID(), player.getName(), Target.byGameProfile(nmsInventory.targetPlayerUuid, nmsInventory.targetPlayerName), logOptions),
                    logOptions.getGranularity());
            this.tracker.onOpen();
        } else {
            this.tracker = null;
        }

        //top inventory slots
        for (int yPos = 0; yPos < 6; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 18 + yPos * 18;
                a(makeSlot(mirror, spectatingOwnInventory, top, index, magicX, magicY));
            }
        }

        //bottom inventory slots
        int magicAddY = (6 /*6 for 6 rows of the top inventory*/ - 4 /*4 for 4 rows of the bottom inventory??*/) * 18;

        //player 'storage'
        for (int yPos = 1; yPos < 4; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 103 + yPos * 18 + magicAddY;
                a(new Slot(playerInventory, index, magicX, magicY));
            }
        }

        //player 'hotbar'
        for (int xPos = 0; xPos < 9; xPos++) {
            int index = xPos;
            int magicX = 8 + xPos * 18;
            int magicY = 161 + magicAddY;
            a(new Slot(playerInventory, index, magicX, magicY));
        }
    }

    @Override
    public MainBukkitInventoryView getBukkitView() {
        if (bukkitView == null) {
            bukkitView = new MainBukkitInventoryView(this);
        }
        return bukkitView;
    }

    @Override
    public boolean a(EntityHuman entityHuman) { //canUse
        return true;
    }

    @Override
    public ItemStack b(EntityHuman entityhuman, int rawIndex) {
        //returns EMPTY_STACK when we are done transferring the itemstack on the rawIndex
        //remember that we are called inside the body of a loop!

        if (spectatingOwnInventory)
            return InvseeImpl.EMPTY_STACK;

        List<Slot> slots = this.c;

        ItemStack itemstack = InvseeImpl.EMPTY_STACK;
        Slot slot = slots.get(rawIndex);
        final int topRows = 6;

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

            if (clickedSlotItem.count == 0) {
                slot.set(InvseeImpl.EMPTY_STACK);
            } else {
                slot.f();
            }

            if (clickedSlotItem.count == itemstack.count) {
                return null;
            }

            slot.a(entityhuman, clickedSlotItem);
        }

        return itemstack;
    }

    private boolean doShiftClickTransfer(ItemStack clickedSlotItem, int targetMinIndex, int targetMaxIndex, boolean topClicked) {
        //returns true is something if part of the clickedSlotItem was transferred, otherwise false
        return super.a(clickedSlotItem, targetMinIndex, targetMaxIndex, topClicked);
    }
}
