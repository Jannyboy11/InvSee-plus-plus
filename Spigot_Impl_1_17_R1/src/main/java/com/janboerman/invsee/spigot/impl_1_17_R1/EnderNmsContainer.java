package com.janboerman.invsee.spigot.impl_1_17_R1;

import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class EnderNmsContainer extends AbstractContainerMenu {
	
	private final Player player;
	private final EnderNmsInventory top;
	private final Inventory bottom;
	private final int topRows;	//in Purpur, this is not always 3.
	
	private InventoryView bukkitView;
	
	private static MenuType<?> determineMenuType(EnderNmsInventory inv) {
		return switch(inv.getContainerSize()) {
			case 9 -> MenuType.GENERIC_9x1;
			case 18 -> MenuType.GENERIC_9x2;
			case 27 -> MenuType.GENERIC_9x3;
			case 36 -> MenuType.GENERIC_9x4;
			case 45 -> MenuType.GENERIC_9x5;
			case 54 -> MenuType.GENERIC_9x6;
			default -> MenuType.GENERIC_9x3;
		};
	}

	public EnderNmsContainer(int containerId, EnderNmsInventory nmsInventory, Inventory playerInventory, Player player) {
		super(determineMenuType(nmsInventory), containerId);
		
		this.topRows = nmsInventory.getContainerSize() / 9;
		this.player = player;
		this.top = nmsInventory;
		this.bottom = playerInventory;
		//nmsInventory.startOpen(player);

		//top inventory slots
		for (int yPos = 0; yPos < topRows; yPos++) {
			for (int xPos = 0; xPos < 9; xPos++) {
				int index = xPos + yPos * 9;
				int magicX = 8 + xPos * 18;
				int magicY = 18 + yPos * 18;
				this.addSlot(new Slot(top, index, magicX, magicY));
			}
		}
		
		//bottom inventory slots
		int magicAddY = (topRows - 4 /*4 because the bottom inventory has 4 rows*/) * 18;
		
		//player 'storage'
		for (int yPos = 1; yPos < 4; yPos++) {
			for (int xPos = 0; xPos < 9; xPos++) {
				int index = xPos + yPos * 9;
				int magicX = 8 + xPos * 18;
				int magicY = 103 + yPos * 18 + magicAddY;
				this.addSlot(new Slot(bottom, index, magicX, magicY));
			}
		}
		
		//player 'hotbar'
		for (int xPos = 0; xPos < 9; xPos++) {
			int index = xPos;
			int magicX = 8 + xPos * 18;
			int magicY = 161 + magicAddY;
			this.addSlot(new Slot(bottom, index, magicX, magicY));
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
	public ItemStack quickMoveStack(Player player, int rawIndex) {
		//returns EMPTY_STACK when we are done transferring the itemstack on the rawIndex
        //remember that we are called from inside the body of a loop!

		ItemStack itemStack = InvseeImpl.EMPTY_STACK;
		Slot slot = this.getSlot(rawIndex);
		
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
