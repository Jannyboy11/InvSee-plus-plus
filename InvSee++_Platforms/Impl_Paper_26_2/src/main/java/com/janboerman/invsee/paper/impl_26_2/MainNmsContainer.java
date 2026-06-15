package com.janboerman.invsee.paper.impl_26_2;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.logging.DifferenceTracker;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.logging.LogOutput;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.plugin.Plugin;

import com.janboerman.invsee.spigot.api.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

class MainNmsContainer extends AbstractContainerMenu {

	final Player player;
	final MainNmsInventory top;
	final Inventory bottom;
	final String originalTitle;
	String title;

	final CreationOptions<PlayerInventorySlot> creationOptions;
	private final boolean spectatingOwnInventory;
	private MainBukkitInventoryView bukkitView;
	final DifferenceTracker tracker;

	private static Slot makeSlot(Mirror<PlayerInventorySlot> mirror, boolean spectatingOwnInventory, MainNmsInventory top, int positionIndex, int magicX, int magicY,
								 ItemStack inaccessiblePlaceholder) {
		final PlayerInventorySlot place = mirror.getSlot(positionIndex);

		if (top.isStmDetached() && place != null && (place.isCursor() || place.isPersonal())) {
			return new InaccessibleSlot(inaccessiblePlaceholder, top, positionIndex, magicX, magicY);
		}

		if (place == null) {
			return new InaccessibleSlot(inaccessiblePlaceholder, top, positionIndex, magicX, magicY);
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
		} else if (place.isOffHand()) {
			final int referringTo = 40;
			return new OffhandSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place.isBody()) {
			final int referringTo = 41;
			return new BodySlot(top, referringTo, magicX, magicY); //idem?
		} else if (place.isSaddle()) {
			final int referringTo = 42;
			return new SaddleSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place.isCursor() && !spectatingOwnInventory) {
			final int referringTo = 43;
			return new CursorSlot(top, referringTo, magicX, magicY); //idem?
		} else if (place.isPersonal()) {
			final int referringTo = place.ordinal() - PlayerInventorySlot.PERSONAL_00.ordinal() + 45;
			return new PersonalSlot(inaccessiblePlaceholder, top, referringTo, magicX, magicY); //idem?
		} else {
			return new InaccessibleSlot(inaccessiblePlaceholder, top, positionIndex, magicX, magicY); //idem?
		}
	}

	// decorate clicked method for tracking/logging
	@Override
	public void clicked(int i, int j, ContainerInput inventoryclicktype, Player entityhuman) {
		if (top.isStmDetached()) {
			stmClicked(i, j, inventoryclicktype, entityhuman);
			return;
		}

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

	private void stmClicked(int i, int j, ContainerInput inventoryclicktype, Player entityhuman) {
		final int size = top.nmsPlayerInventory.getContainerSize();

		// snapshot the player-storage slots (incl. armour/offhand/body/saddle) before the click - safe, this
		// reads the detached copy owned by the spectator's region thread.
		final ItemStack[] before = new ItemStack[size];
		for (int s = 0; s < size; s++) before[s] = top.nmsPlayerInventory.getItem(s).copy();

		List<org.bukkit.inventory.ItemStack> trackBefore = null;
		if (tracker != null) trackBefore = top.getContents().stream().map(CraftItemStack::asBukkitCopy).toList();

		// apply the click to the detached snapshot (top) and the spectator's own live bottom inventory.
		super.clicked(i, j, inventoryclicktype, entityhuman);

		if (tracker != null) {
			List<org.bukkit.inventory.ItemStack> trackAfter = top.getContents().stream().map(CraftItemStack::asBukkitCopy).toList();
			tracker.onClick(trackBefore, trackAfter);
		}

		final ItemStack[] after = new ItemStack[size];
		boolean anyChange = false;
		for (int s = 0; s < size; s++) {
			after[s] = top.nmsPlayerInventory.getItem(s).copy();
			if (!ItemStack.matches(before[s], after[s])) anyChange = true;
		}
		if (!anyChange) return;

		final UUID targetId = top.stmLiveTargetId;
		final Scheduler scheduler = top.stmScheduler;
		// commit on the target's region thread; if the target logged off the edits simply stay in the snapshot.
		scheduler.executeSyncPlayer(targetId, () -> commitToLiveTarget(targetId, before, after, size), () -> {});
	}

	private void commitToLiveTarget(UUID targetId, ItemStack[] before, ItemStack[] after, int size) {
		Plugin plugin = creationOptions.getPlugin();
		org.bukkit.entity.Player bukkitTarget = plugin.getServer().getPlayer(targetId);
		if (bukkitTarget == null) return; // target went offline between scheduling and running
		Inventory liveInv = ((CraftPlayer) bukkitTarget).getHandle().getInventory();

		List<Integer> conflictSlots = null;
		List<ItemStack> conflictValues = null;
		for (int s = 0; s < size; s++) {
			if (ItemStack.matches(before[s], after[s])) continue; // spectator did not change this slot
			ItemStack real = liveInv.getItem(s);
			if (ItemStack.matches(real, before[s])) {
				// the live slot still matches what the spectator saw -> safe to apply the edit.
				liveInv.setItem(s, after[s].copy());
			} else {
				// the target changed this slot concurrently -> keep the live value and re-sync the spectator.
				if (conflictSlots == null) { conflictSlots = new ArrayList<>(); conflictValues = new ArrayList<>(); }
				conflictSlots.add(s);
				conflictValues.add(real.copy());
			}
		}

		if (conflictSlots != null) {
			final List<Integer> fConflictSlots = conflictSlots;
			final List<ItemStack> fConflictValues = conflictValues;
			top.stmScheduler.executeSyncPlayer(player.getUUID(), () -> resyncSpectator(fConflictSlots, fConflictValues), () -> {});
		}
	}

	private void resyncSpectator(List<Integer> conflictSlots, List<ItemStack> conflictValues) {
		Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
		for (int k = 0; k < conflictSlots.size(); k++) {
			int s = conflictSlots.get(k);
			ItemStack real = conflictValues.get(k);
			top.nmsPlayerInventory.setItem(s, real.copy()); // bring the snapshot back in line with reality
			Integer rawIndex = mirror.getIndex(PlayerInventorySlot.byDefaultIndex(s));
			if (rawIndex != null) {
				InvseeImpl.sendItemChange((net.minecraft.server.level.ServerPlayer) player, rawIndex.intValue(), real);
			}
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

	MainNmsContainer(int id, MainNmsInventory nmsInventory, Inventory bottomInventory, Player spectator, CreationOptions<PlayerInventorySlot> creationOptions) {
		super(MenuType.GENERIC_9x6, id);

		this.top = nmsInventory;
		this.bottom = bottomInventory;
		this.player = spectator;
		this.spectatingOwnInventory = spectator.getUUID().equals(nmsInventory.targetPlayerUuid);

		this.creationOptions = creationOptions;
		Target target = Target.byGameProfile(nmsInventory.targetPlayerUuid, nmsInventory.targetPlayerName);
		this.originalTitle = creationOptions.getTitle().titleFor(target);
		Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
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
		ItemStack inaccessibleSlotPlaceholder = CraftItemStack.asNMSCopy(creationOptions.getPlaceholderPalette().inaccessible());

		//top inventory slots
		for (int yPos = 0; yPos < 6; yPos++) {
			for (int xPos = 0; xPos < 9; xPos++) {
				int index = xPos + yPos * 9;
				int magicX = 8 + xPos * 18;
				int magicY = 18 + yPos * 18;

				addSlot(makeSlot(mirror, spectatingOwnInventory, top, index, magicX, magicY, inaccessibleSlotPlaceholder));
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
	public MainBukkitInventoryView getBukkitView() {
		if (bukkitView == null) {
			bukkitView = new MainBukkitInventoryView(this);
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
