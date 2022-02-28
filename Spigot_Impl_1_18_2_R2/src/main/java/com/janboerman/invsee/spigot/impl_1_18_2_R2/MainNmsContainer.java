package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

class MainNmsContainer extends AbstractContainerMenu {

	private final Player player;
	private final MainNmsInventory top;
	private final Inventory bottom;
	private final boolean spectatingOwnInventory;
	
	private InventoryView bukkitView;
	
	MainNmsContainer(int id, MainNmsInventory nmsInventory, Inventory bottomInventory, Player spectator) {
		super(MenuType.GENERIC_9x6, id);
		
		this.top = nmsInventory;
		this.bottom = bottomInventory;
		this.player = spectator;
		this.spectatingOwnInventory = spectator.getUUID().equals(nmsInventory.targetPlayerUuid);
		
		final int firstFiveRows = top.storageContents.size()
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
					addSlot(new Slot(top, index, magicX, magicY));
				} else if (45 <= index && index < 54) {
					addSlot(new PersonalSlot(top, index, magicX, magicY));
				} else {
					addSlot(new InaccessibleSlot(top, index, magicX, magicY));
				}
			}
		}
		
		//bottom inventory slots
		int magicAddY = (6 /*6 for 6 rows of the top inventory*/ - 4 /*4 for 4 rows of the bottom inventory*/) * 18;
		
		//player 'storage'
		for (int yPos = 1; yPos < 4; yPos++) {
			for (int xPos = 0; xPos < 9; xPos++) {
				int index = xPos + yPos * 9;
				int magicX = 8 + xPos * 18;
				int magicY = 103 + yPos * 18 + magicAddY;
				addSlot(new Slot(bottomInventory, index, magicX, magicY));
			}
		}
		
		//player 'hotbar'
		for (int xPos = 0; xPos < 9; xPos++) {
			int index = xPos;
			int magicX = 8 + xPos * 18;
			int magicY = 161 + magicAddY;
			addSlot(new Slot(bottomInventory, index, magicX, magicY));
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
	public boolean stillValid(Player player) {
		return true;
	}
	
	@Override
	public ItemStack quickMoveStack(Player entityHuman, int rawIndex) {
        //returns ItemStack.EMPTY when we are done transferring the itemstack on the rawIndex
        //remember that we are called inside the body of a loop!

		//is entityHuman ever not equal to the viewer that we got instantiated with?
		//in any case, let's just do this first: prevent shift-clicking when spectating your own inventory
		if (spectatingOwnInventory)
			return ItemStack.EMPTY;
		
		ItemStack itemStack = ItemStack.EMPTY;
		final Slot slot = getSlot(rawIndex);
		final int topRows = 6;
		
		if (slot != null && slot.hasItem()) {
			ItemStack clickedSlotItem = slot.getItem();
			
			itemStack = clickedSlotItem.copy();
			if (rawIndex < topRows * 9) {
				//clicked in the top inventory
				if (!moveItemStackTo(clickedSlotItem, topRows * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				//clicked in the bottom inventory
				if (!moveItemStackTo(clickedSlotItem, 0, topRows * 9, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			if (clickedSlotItem.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		
		return itemStack;
	}

}
