package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import java.util.List;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;

import org.bukkit.craftbukkit.v1_21_R4.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftChatMessage;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;

class EnderNmsInventory extends AbstractNmsInventory<EnderChestSlot, EnderBukkitInventory, EnderNmsInventory> implements Container, MenuProvider {

	protected NonNullList<ItemStack> storageContents;

	EnderNmsInventory(Player target, CreationOptions<EnderChestSlot> creationOptions) {
		super(target.getUUID(), target.getScoreboardName(), creationOptions);
		PlayerEnderChestContainer inv = target.getEnderChestInventory();
		this.storageContents = HybridServerSupport.enderChestItems(inv);
		this.maxStack = inv.getMaxStackSize();
	}

	@Override
	protected EnderBukkitInventory createBukkit() {
		return new EnderBukkitInventory(this);
	}

	@Override
	public void setMaxStackSize(int size) {
		this.maxStack = size;
	}

	//vanilla
	@Override
	public int getMaxStackSize() {
		return maxStack;
	}

	@Override
	public int defaultMaxStack() {
		return Container.MAX_STACK;
	}

	@Override
	public void shallowCopyFrom(EnderNmsInventory from) {
		this.setMaxStackSize(from.getMaxStackSize());
		this.storageContents = from.storageContents;
		setChanged();
	}

	//vanilla
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new EnderNmsContainer(containerId, this, playerInventory, player, creationOptions);
	}

	//vanilla
	@Override
	public Component getDisplayName() {
		//return new TextComponent("minecraft:generic_9x" + (storageContents.size() / 9));
		return CraftChatMessage.fromStringOrNull(creationOptions.getTitle().titleFor(Target.byGameProfile(targetPlayerUuid, targetPlayerName)));
	}

	//vanilla
	@Override
	public void clearContent() {
		storageContents.clear();
	}

	//vanilla
	@Override
	public int getContainerSize() {
		return storageContents.size();
	}

	//craftbukkit
	@Override
	public List<ItemStack> getContents() {
		return storageContents;
	}

	//vanilla
	@Override
	public ItemStack getItem(int slot) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
		
		return storageContents.get(slot);
	}

	//vanilla
	@Override
	public boolean isEmpty() {
		for (ItemStack stack : storageContents) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	//craftbukkit
	@Override
	public void onClose(CraftHumanEntity bukkitPlayer) {
		super.onClose(bukkitPlayer);
	}

	//craftbukkit
	@Override
	public void onOpen(CraftHumanEntity bukkitPlayer) {
		super.onOpen(bukkitPlayer);
	}

	//vanilla
	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
		
		ItemStack stack = ContainerHelper.removeItem(storageContents, slot, amount);
		if (!stack.isEmpty()) {
			this.setChanged();
		}
		return stack;
	}

	//vanilla
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

	//vanilla
	@Override
	public void setChanged() {
		//probably don't need to do anything here.
	}

	//vanilla
	@Override
	public void setItem(int slot, ItemStack itemStack) {
		if (slot < 0 || slot >= getContainerSize()) return;
		
		storageContents.set(slot, itemStack);
		if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
			itemStack.setCount(getMaxStackSize());
		}
		
		setChanged();
	}

	//vanilla
	@Override
	public boolean stillValid(Player player) {
		//no chest lock
		return true;
	}

}
