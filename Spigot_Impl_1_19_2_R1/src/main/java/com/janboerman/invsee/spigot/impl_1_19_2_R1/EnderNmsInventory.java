package com.janboerman.invsee.spigot.impl_1_19_2_R1;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;

import java.util.List;
import java.util.UUID;

class EnderNmsInventory extends AbstractNmsInventory<EnderChestSlot, EnderNmsInventory> implements Container, MenuProvider {

	protected NonNullList<ItemStack> storageContents;

	EnderNmsInventory(UUID targetPlayerUuid, String targetPlayerName, NonNullList<ItemStack> storageContents, String title, Mirror<EnderChestSlot> mirror) {
		super(targetPlayerUuid, targetPlayerName, title, mirror);
		this.storageContents = storageContents;
	}

	@Override
	public void setMaxStackSize(int size) {
		this.maxStack = size;
	}

	@Override
	public int getMaxStackSize() {
		return maxStack;
	}

	@Override
	public int defaultMaxStack() {
		return Container.LARGE_MAX_STACK_SIZE;
	}

	@Override
	public void shallowCopyFrom(EnderNmsInventory from) {
		setMaxStackSize(from.getMaxStackSize());
		storageContents = from.storageContents;
		setChanged();
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new EnderNmsContainer(containerId, this, playerInventory, player, mirror);
	}

	@Override
	public Component getDisplayName() {
		//return new TextComponent("minecraft:generic_9x" + (storageContents.size() / 9));
		return CraftChatMessage.fromStringOrNull(title);
	}

	@Override
	public void clearContent() {
		storageContents.clear();
	}

	@Override
	public int getContainerSize() {
		return storageContents.size();
	}

	@Override
	public List<ItemStack> getContents() {
		return storageContents;
	}

	@Override
	public ItemStack getItem(int slot) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;

		return storageContents.get(slot);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : storageContents) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public void onClose(CraftHumanEntity bukkitPlayer) {
		super.onClose(bukkitPlayer);
	}

	@Override
	public void onOpen(CraftHumanEntity bukkitPlayer) {
		super.onOpen(bukkitPlayer);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;

		ItemStack stack = ContainerHelper.removeItem(storageContents, slot, amount);
		if (!stack.isEmpty()) {
			this.setChanged();
		}
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;

		ItemStack stack = storageContents.get(slot);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			storageContents.set(slot, ItemStack.EMPTY);
			return stack;
		}
	}

	@Override
	public void setChanged() {
		//probably don't need to do anything here.
	}

	@Override
	public void setItem(int slot, ItemStack itemStack) {
		if (slot < 0 || slot >= getContainerSize()) return;

		storageContents.set(slot, itemStack);
		if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
			itemStack.setCount(getMaxStackSize());
		}

		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		//no chest lock
		return true;
	}

}
