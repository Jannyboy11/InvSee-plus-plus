package com.janboerman.invsee.spigot.impl_1_19_2_R1;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class InaccessibleSlot extends Slot {
	
	InaccessibleSlot(MainNmsInventory inventory, int index, int xPos, int yPos) {
		super(inventory, index, xPos, yPos);
	}
	
	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return false;
	}
	
	@Override
	public boolean hasItem() {
		return false;
	}
	
	@Override
	public void set(ItemStack itemStack) {
		setChanged();
	}
	
	@Override
	public int getMaxStackSize() {
		return 0;
	}
	
	@Override
	public ItemStack remove(int subtractAmount) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean allowModification(Player player) {
		return false;
	}
	
	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	public ItemStack getItem() {
		return ItemStack.EMPTY;
	}

}
