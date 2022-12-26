package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import net.minecraft.server.v1_16_R3.*;

import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class MainNmsContainer extends Container {

    private final EntityHuman player;
    private final MainNmsInventory top;
    private final PlayerInventory bottom;
    private final boolean spectatingOwnInventory;

    private InventoryView bukkitView;

    private static Slot makeSlot(Mirror<PlayerInventorySlot> mirror, boolean spectatingOwnInventory, MainNmsInventory top, int positionIndex, int magicX, int magicY) {
        final PlayerInventorySlot place = mirror.getSlot(positionIndex);

        if (place == null) {
            return new InaccessibleSlot(top, positionIndex, magicX, magicY);
        } else if (place.isContainer()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.CONTAINER_00.ordinal();
            return new Slot(top, referringTo, magicX, magicY); //magicX and magicY correct here? it seems to work though.
        } else if (place.isArmour()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.ARMOUR_BOOTS.ordinal() + 36;
            return new Slot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isPersonal()) {
            final int referringTo = place.ordinal() - PlayerInventorySlot.PERSONAL_00.ordinal() + 45;
            return new PersonalSlot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isOffHand()) {
            final int referringTo = 40;
            return new Slot(top, referringTo, magicX, magicY); //idem?
        } else if (place.isCursor() && !spectatingOwnInventory) {
            final int referringTo = 41;
            return new Slot(top, referringTo, magicX, magicY); //idem?
        } else {
            return new InaccessibleSlot(top, positionIndex, magicX, magicY); //idem?
        }
    }

    MainNmsContainer(int containerId, MainNmsInventory nmsInventory, PlayerInventory playerInventory, EntityHuman player, Mirror<PlayerInventorySlot> mirror) {
        super(Containers.GENERIC_9X6, containerId);
        this.top = nmsInventory;
        this.bottom = playerInventory;
        this.player = player;
        //setTitle(nmsInventory.getScoreboardDisplayName()); //setTitle is actually called when the thing actually opens. or something.
        this.spectatingOwnInventory = player.getUniqueID().equals(playerInventory.player.getUniqueID());

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

        //player 'hotbar' (yPos = 0)
        for (int xPos = 0; xPos < 9; xPos++) {
            int index = xPos;
            int magicX = 8 + xPos * 18;
            int magicY = 161 + magicAddY;
            a(new Slot(playerInventory, index, magicX, magicY));
        }
    }

    @Override
    public InventoryView getBukkitView() {
        if (bukkitView == null) {
            bukkitView = new CraftInventoryView(player.getBukkitEntity(), top.bukkit, this);
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

        if (spectatingOwnInventory)
            return InvseeImpl.EMPTY_STACK;

        ItemStack itemstack = InvseeImpl.EMPTY_STACK;
        Slot slot = this.slots.get(rawIndex);
        final int topRows = 6;

        if (slot != null && slot.hasItem()) {
            ItemStack clickedSlotItem = slot.getItem();

            itemstack = clickedSlotItem.cloneItemStack();
            if (rawIndex < topRows * 9) {
                //clicked in the top inventory
                if (!doShiftClickTransfer(clickedSlotItem, topRows * 9, this.slots.size(), true)) {
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
