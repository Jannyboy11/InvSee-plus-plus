package com.janboerman.invsee.spigot.impl_1_21_5_R4;

import java.util.List;
import java.util.Objects;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogOutput;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;

import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack;
import org.bukkit.plugin.Plugin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class EnderNmsContainer extends AbstractContainerMenu {
	
	final Player player;
	final EnderNmsInventory top;
	final Inventory bottom;
	final String originalTitle;
	String title;

	final CreationOptions<EnderChestSlot> creationOptions;
	private final int topRows;	//in Purpur, this is not always 3.
	private EnderBukkitInventoryView bukkitView;
	final DifferenceTracker tracker;
	
	private static MenuType<?> determineMenuType(EnderNmsInventory inv) {
		return switch (inv.getContainerSize()) {
			case 9 -> MenuType.GENERIC_9x1;
			case 18 -> MenuType.GENERIC_9x2;
			case 27 -> MenuType.GENERIC_9x3;
			case 36 -> MenuType.GENERIC_9x4;
			case 45 -> MenuType.GENERIC_9x5;
			case 54 -> MenuType.GENERIC_9x6;
			default -> MenuType.GENERIC_9x3;
		};
	}

	private static Slot makeSlot(Mirror<EnderChestSlot> mirror, EnderNmsInventory top, int positionIndex, int magicX, int magicY, ItemStack inaccessiblePlaceholder) {
		final EnderChestSlot place = mirror.getSlot(positionIndex);

		if (place == null) {
			return new InaccessibleSlot(inaccessiblePlaceholder, top, positionIndex, magicX, magicY);
		} else {
			final int referringTo = place.ordinal();
			return new Slot(top, referringTo, magicX, magicY);
		}
	}

	// decorate clicked method for tracking/logging
	@Override
	public void clicked(int i, int j, ClickType inventoryclicktype, Player entityhuman) {
		//TODO Folia: schedule task that is synchronised across both the target player's EntityScheduler as well as the spectator player's EntityScheduler.
		//TODO because now we have a data race.
		//TODO can I use java.util.concurrent.{Exchanger, CyclicBarrier or Phaser} perhaps??
		//TODO when we arrive here, we are in the tick thread of the Spectator player.

		List<org.bukkit.inventory.ItemStack> contentsBefore = null, contentsAfter;
		if (tracker != null) {
			contentsBefore = top.getContents().stream().map(CraftItemStack::asBukkitCopy).toList();
		}

		super.clicked(i, j, inventoryclicktype, entityhuman);

		if (tracker != null) {
			contentsAfter = top.getContents().stream().map(CraftItemStack::asBukkitCopy).toList();
			tracker.onClick(contentsBefore, contentsAfter);
		}
	}

	// decorate removed method for tracking/logging
	@Override
	public void removed(Player entityhuman) {
		super.removed(entityhuman);

		if (tracker != null && Objects.equals(entityhuman, player)) {
			tracker.onClose();
		}
	}

	EnderNmsContainer(int containerId, EnderNmsInventory nmsInventory, Inventory playerInventory, Player player, CreationOptions<EnderChestSlot> creationOptions) {
		super(determineMenuType(nmsInventory), containerId);
		
		this.topRows = nmsInventory.getContainerSize() / 9;
		this.player = player;
		this.top = nmsInventory;
		this.bottom = playerInventory;

		this.creationOptions = creationOptions;
		Target target = Target.byGameProfile(nmsInventory.targetPlayerUuid, nmsInventory.targetPlayerName);
		this.originalTitle = creationOptions.getTitle().titleFor(target);
		Mirror<EnderChestSlot> mirror = creationOptions.getMirror();
		LogOptions logOptions = creationOptions.getLogOptions();
		Plugin plugin = creationOptions.getPlugin();
		if (!LogOptions.isEmpty(logOptions)) {
			this.tracker = new DifferenceTracker(
					LogOutput.make(plugin, player.getUUID(), player.getScoreboardName(), target, logOptions),
					logOptions.getGranularity());
			this.tracker.onOpen();
		} else {
			this.tracker = null;
		}

		//top inventory slots
		for (int yPos = 0; yPos < topRows; yPos++) {
			for (int xPos = 0; xPos < 9; xPos++) {
				int index = xPos + yPos * 9;
				int magicX = 8 + xPos * 18;
				int magicY = 18 + yPos * 18;

				addSlot(makeSlot(mirror, top, index, magicX, magicY, CraftItemStack.asNMSCopy(creationOptions.getPlaceholderPalette().inaccessible())));
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
				addSlot(new Slot(bottom, index, magicX, magicY));
			}
		}
		
		//player 'hotbar' (yPos = 0)
		for (int xPos = 0; xPos < 9; xPos++) {
			int index = xPos;
			int magicX = 8 + xPos * 18;
			int magicY = 161 + magicAddY;
			addSlot(new Slot(bottom, index, magicX, magicY));
		}
	}

	@Override
	public EnderBukkitInventoryView getBukkitView() {
		if (bukkitView == null) {
			bukkitView = new EnderBukkitInventoryView(this);
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

		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = getSlot(rawIndex);
		
		if (slot != null && slot.hasItem()) {
			ItemStack clickedSlotItem = slot.getItem();
			
			itemStack = clickedSlotItem.copy();
			if (rawIndex < topRows * 9) {
				//clicked in the top inventory
				if (!moveItemStackTo(clickedSlotItem, topRows * 9, slots.size(), true)) {
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

	public String title() {
		return title != null ? title : originalTitle;
	}

}
