package com.janboerman.invsee.spigot.impl_1_15_R1;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class MainNmsContainer extends Container {

    private final EntityHuman player;
    private final MainNmsInventory top;
    private final PlayerInventory bottom;
    private final boolean spectatingOwnInventory;

    private InventoryView bukkitView;

    public MainNmsContainer(int containerId, MainNmsInventory nmsInventory, PlayerInventory playerInventory, EntityHuman player) {
        super(Containers.GENERIC_9X6, containerId);
        this.top = nmsInventory;
        this.bottom = playerInventory;
        this.player = player;
        //setTitle(nmsInventory.getScoreboardDisplayName()); //setTitle is actually called when the thing actually opens. or something.
        this.spectatingOwnInventory = player.getUniqueID().equals(playerInventory.player.getUniqueID());

        int firstFiveRows = top.storageContents.size()
                + top.armourContents.size()
                + top.offHand.size()
                + (spectatingOwnInventory ? 0 : 1); //only include cursor when not spectating yourself

        //top inventory slots
        for (int yPos = 0; yPos < 6; yPos++) {
            for (int xPos = 0; xPos < 9; xPos++) {
                int index = xPos + yPos * 9;
                int magicX = 8 + xPos * 18;
                int magicY = 18 + yPos * 18;
                if (index < firstFiveRows) {
                    a(new Slot(top, index, magicX, magicY));
                } else if (45 <= index && index < 54) {
                    a(new PersonalSlot(top, index, magicX, magicY));
                } else {
                    a(new InAccessibleSlot(top, index, magicX, magicY));
                }
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