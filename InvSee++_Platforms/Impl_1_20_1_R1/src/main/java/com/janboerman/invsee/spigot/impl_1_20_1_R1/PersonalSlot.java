package com.janboerman.invsee.spigot.impl_1_20_1_R1;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class PersonalSlot extends Slot {
	
	PersonalSlot(MainNmsInventory inventory, int index, int xPos, int yPos) {
		super(inventory, index, xPos, yPos);
	}
	
	private boolean works() {
		MainNmsInventory inv = (MainNmsInventory) super.container;		// Mohist compat: use super.container instead of this.container
		int personalSize = inv.personalContents.size();
		boolean inRange = 45 <= super.getContainerSlot() && super.getContainerSlot() < 45 + personalSize;
		return inRange;
	}
	
	@Override
	public boolean mayPlace(ItemStack itemStack) {
		if (!works()) return false;
		return super.mayPlace(itemStack);
	}
	
	@Override
	public boolean hasItem() {
		if (!works()) return false;
		return super.hasItem();
	}
	
	@Override
	public void set(ItemStack itemStack) {
		if (!works()) this.setChanged();
		super.set(itemStack);
	}
	
	@Override
	public int getMaxStackSize() {
		if (!works()) return 0;
		return super.getMaxStackSize();
	}
	
	@Override
	public ItemStack remove(int subtractAmount) {
		if (!works()) {
			return ItemStack.EMPTY;
		} else {
			return super.remove(subtractAmount);
		}
	}
	
	@Override
	public boolean allowModification(Player player) {
		if (!works()) return false;
		return super.allowModification(player);
	}
	
	@Override
	public boolean mayPickup(Player player) {
		if (!works()) return false;
		return super.mayPickup(player);
	}

	@Override
	public ItemStack getItem() {
		if (!works()) return ItemStack.EMPTY;
		return super.getItem();
	}

	public boolean isActive() {
		return works();
	}

	// === Mohist workarounds ===
	//TODO are SRG names stable?

//	public boolean m_5857_(ItemStack itemStack) {
//		return mayPlace(itemStack);
//	}
//
//	public boolean m_6657_() {
//		return hasItem();
//	}
//
//	public void m_219996_(ItemStack itemStack) {
//		set(itemStack);
//	}
//
//	public int m_6641_() {
//		return getMaxStackSize();
//	}
//
//	public ItemStack m_6201_(int subtractAmount) {
//		return remove(subtractAmount);
//	}
//
//	public boolean m_150651_(Player player) {
//		return allowModification(player);
//	}
//
//	public boolean m_8010_(Player player) {
//		return mayPickup(player);
//	}
//
//	public ItemStack m_7993_() {
//		return getItem();
//	}
//
//	public boolean m_6659_() {
//		return isActive();
//	}

}
