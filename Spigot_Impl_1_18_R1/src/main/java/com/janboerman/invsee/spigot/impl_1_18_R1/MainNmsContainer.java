package com.janboerman.invsee.spigot.impl_1_18_R1;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

class MainNmsContainer extends AbstractContainerMenu {

	private final Player player;
	private final MainNmsInventory top;
	private final Inventory bottom;
	
	private InventoryView bukkitView;
	
	MainNmsContainer(int id, MainNmsInventory nmsInventory, Inventory playerInventory, Player player) {
		super(MenuType.GENERIC_9x6, id);
		
		this.top = nmsInventory;
		this.bottom = playerInventory;
		this.player = player;
		
		int firstFiveRows = top.storageContents.size()
				+ top.armourContents.size()
				+ top.offHand.size()
				+ 1 /*cursor*/;
		
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
				addSlot(new Slot(playerInventory, index, magicX, magicY));
			}
		}
		
		//player 'hotbar'
		for (int xPos = 0; xPos < 9; xPos++) {
			int index = xPos;
			int magicX = 8 + xPos * 18;
			int magicY = 161 + magicAddY;
			addSlot(new Slot(playerInventory, index, magicX, magicY));
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
        //returns EMPTY_STACK when we are done transferring the itemstack on the rawIndex
        //remember that we are called inside the body of a loop!
		
		ItemStack itemStack = InvseeImpl.EMPTY_STACK;
		Slot slot = getSlot(rawIndex);
		final int topRows = 6;
		
		if (slot != null && slot.hasItem()) {
			ItemStack clickedSlotItem = slot.getItem();
			
			itemStack = clickedSlotItem.copy();
			if (rawIndex < topRows * 9) {
				//clicked in the top inventory
				if (!moveItemStackTo(clickedSlotItem, topRows * 9, this.slots.size(), true)) {
					return InvseeImpl.EMPTY_STACK;
				}
			} else {
				//clicked in the bottom inventory
				if (!moveItemStackTo(clickedSlotItem, 0, topRows * 9, false)) {
					return InvseeImpl.EMPTY_STACK;
				}
			}
			
			if (clickedSlotItem.isEmpty()) {
				slot.set(InvseeImpl.EMPTY_STACK);
			} else {
				slot.setChanged();
			}
		}
		
		return itemStack;
	}

}
