package com.janboerman.invsee.spigot.impl_1_19_3_R2;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogOutput;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;

class MainNmsContainer extends AbstractContainerMenu {

	private final Player player;
	private final MainNmsInventory top;
	private final Inventory bottom;
	private final boolean spectatingOwnInventory;
	
	private InventoryView bukkitView;
	private final DifferenceTracker tracker;

	private static Slot makeSlot(Mirror<PlayerInventorySlot> mirror, boolean spectatingOwnInventory, MainNmsInventory top, int positionIndex, int magicX, int magicY) {
		final PlayerInventorySlot place = mirror.getSlot(positionIndex);

		if (place == null) {
			return new InaccessibleSlot(top, positionIndex, magicX, magicY);
		} else if (place.isContainer()) {
			final int referringTo = place.ordinal() - PlayerInventorySlot.CONTAINER_00.ordinal();
			return new Slot(top, referringTo, magicX, magicY); //magicX and magicY correct here? it seems to work though.
		} else if (place == PlayerInventorySlot.ARMOUR_BOOTS) {
			final int referringTo = 36;
			return new BootsSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place == PlayerInventorySlot.ARMOUR_LEGGINGS) {
			final int referringTo = 37;
			return new LeggingsSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place == PlayerInventorySlot.ARMOUR_CHESTPLATE) {
			final int referringTo = 38;
			return new ChestplateSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place == PlayerInventorySlot.ARMOUR_HELMET) {
			final int referringTo = 39;
			return new HelmetSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place.isPersonal()) {
			final int referringTo = place.ordinal() - PlayerInventorySlot.PERSONAL_00.ordinal() + 45;
			return new PersonalSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place.isOffHand()) {
			final int referringTo = 40;
			return new OffhandSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place.isCursor() && !spectatingOwnInventory) {
			final int referringTo = 41;
			return new Slot(top, referringTo, magicX, magicY); //idem?
		} else {
			return new InaccessibleSlot(top, positionIndex, magicX, magicY); //idem?
		}
	}

	// decorate clicked method for tracking/logging
	@Override
	public void clicked(int i, int j, ClickType inventoryclicktype, Player entityhuman) {
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
		if (tracker != null && Objects.equals(entityhuman, player)) {
			tracker.onClose();
		}

		super.removed(entityhuman);
	}
	
	MainNmsContainer(int id, MainNmsInventory nmsInventory, Inventory bottomInventory, Player spectator, CreationOptions<PlayerInventorySlot> creationOptions) {
		super(MenuType.GENERIC_9x6, id);
		
		this.top = nmsInventory;
		this.bottom = bottomInventory;
		this.player = spectator;
		this.spectatingOwnInventory = spectator.getUUID().equals(nmsInventory.targetPlayerUuid);

		Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
		LogOptions logOptions = creationOptions.getLogOptions();
		Plugin plugin = creationOptions.getPlugin();
		if (!LogOptions.isEmpty(logOptions)) {
			this.tracker = new DifferenceTracker(
					LogOutput.make(plugin, player.getUUID(), player.getScoreboardName(), Target.byGameProfile(nmsInventory.targetPlayerUuid, nmsInventory.targetPlayerName), logOptions),
					logOptions.getGranularity());
			this.tracker.onOpen();
		} else {
			this.tracker = null;
		}

		//top inventory slots
		for (int yPos = 0; yPos < 6; yPos++) {
			for (int xPos = 0; xPos < 9; xPos++) {
				int index = xPos + yPos * 9;
				int magicX = 8 + xPos * 18;
				int magicY = 18 + yPos * 18;

				addSlot(makeSlot(mirror, spectatingOwnInventory, top, index, magicX, magicY));
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
		
		//player 'hotbar' (yPos = 0)
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
			bukkitView = new CraftInventoryView(player.getBukkitEntity(), top.bukkit(), this);
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

	//TODO decorate clicked(int,int,ClickType,Player) and log actions (may want to accumulate them into a 'diff' object - actually will have to accumulate small diffs per spectator).

}
