package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class EnderNmsInventory implements Container, MenuProvider {

	protected final UUID targetPlayerUuid;
	protected final String targetPlayerName;
	protected final NonNullList<ItemStack> storageContents;

	protected org.bukkit.inventory.Inventory bukkit;
	protected String title;
	protected Mirror<EnderChestSlot> mirror = Mirror.defaultEnderChest();

	private int maxStack = Container.MAX_STACK;
	private final List<HumanEntity> transaction = new ArrayList<>();
	protected InventoryHolder owner;

	protected EnderNmsInventory(UUID targetPlayerUuid, String targetPlayerName, NonNullList<ItemStack> storageContents) {
		this.targetPlayerUuid = targetPlayerUuid;
		this.targetPlayerName = targetPlayerName;
		this.storageContents = storageContents;
	}

	protected EnderNmsInventory(UUID targetPlayerUuid, String targetPlayerName, NonNullList<ItemStack> storageContents, String title) {
		this(targetPlayerUuid, targetPlayerName, storageContents);
		this.title = title;
	}

	protected EnderNmsInventory(UUID targetPlayerUuid, String targetPlayerName, NonNullList<ItemStack> storageContents, String title, Mirror<EnderChestSlot> mirror) {
		this(targetPlayerUuid, targetPlayerName, storageContents, title);
		this.mirror = mirror;
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
	public Location getLocation() {
		return null;
	}

	@Override
	public int getMaxStackSize() {
		return maxStack;
	}

	@Override
	public InventoryHolder getOwner() {
		return owner;
	}

	@Override
	public List<HumanEntity> getViewers() {
		return transaction;
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
		transaction.remove(bukkitPlayer);
	}

	@Override
	public void onOpen(CraftHumanEntity bukkitPlayer) {
		transaction.add(bukkitPlayer);
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
	public void setMaxStackSize(int maxSize) {
		this.maxStack = maxSize;
	}

	@Override
	public boolean stillValid(Player player) {
		//no chest lock
		return true;
	}

}
