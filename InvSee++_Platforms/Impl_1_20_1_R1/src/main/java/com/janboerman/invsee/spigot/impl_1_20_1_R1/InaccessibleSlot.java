package com.janboerman.invsee.spigot.impl_1_20_1_R1;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class InaccessibleSlot extends Slot {
	
	InaccessibleSlot(Container inventory, int index, int xPos, int yPos) {
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
		super.setChanged();		//Mohist
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

	@Override
	public boolean isActive() {
		return false;
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
