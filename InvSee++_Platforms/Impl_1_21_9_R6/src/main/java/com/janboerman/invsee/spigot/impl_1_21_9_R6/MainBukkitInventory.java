package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import java.util.HashMap;
import java.util.Objects;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderGroup;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.MainInventory;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R6.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MerchantContainer;

class MainBukkitInventory extends CraftInventory implements MainInventory<MainNmsInventory, MainBukkitInventory> {

	protected MainBukkitInventory(MainNmsInventory inventory) {
		super(inventory);
	}
	
	@Override
	public MainNmsInventory getInventory() {
		return (MainNmsInventory) super.getInventory();
	}

	@Override
	public void watch(InventoryView targetPlayerView) {
		Objects.requireNonNull(targetPlayerView, "targetPlayerView cannot be null");

		PlaceholderGroup placeholderGroup = null;

		MainNmsInventory nms = getInventory();
		var top = targetPlayerView.getTopInventory();
		if (top instanceof CraftInventoryCrafting cic) {
			CraftingContainer targetCrafting = (CraftingContainer) cic.getInventory();
			nms.personalContents = targetCrafting.getContents(); //luckily, this does not create a copy.
			placeholderGroup = PlaceholderGroup.CRAFTING;
		} else if (top instanceof CraftInventoryMerchant cim) {
			MerchantContainer merchantItems = cim.getInventory();
			nms.personalContents = merchantItems.getContents().subList(0, 2); //only payment slots
			placeholderGroup = PlaceholderGroup.MERCHANT;
		} else if (top instanceof CraftInventoryEnchanting cie) {
			Container enchantItems = cie.getInventory();
			nms.personalContents = enchantItems.getContents();
			placeholderGroup = PlaceholderGroup.ENCHANTING;
		} else if (top instanceof CraftResultInventory cri) {
			Container inputItems = cri.getInventory();
			nms.personalContents = inputItems.getContents();
			placeholderGroup = switch (cri.getType()) {
				case ANVIL -> PlaceholderGroup.ANVIL;
				case CARTOGRAPHY -> PlaceholderGroup.CARTOGRAPHY;
				case GRINDSTONE -> PlaceholderGroup.GRINDSTONE;
				case LOOM -> PlaceholderGroup.LOOM;
				case SMITHING, SMITHING_NEW -> PlaceholderGroup.SMITHING;
				case STONECUTTER -> PlaceholderGroup.STONECUTTER;
				case CRAFTER -> null; // Crafter is the only CraftResultInventory which is not personal.
				default -> throw new RuntimeException("CraftResultInventory with unexpected InventoryType: " + cri.getType());
			};
		}

		//send personal slots changes
		for (HumanEntity viewer : getViewers()) {
			if (viewer instanceof CraftPlayer spectator && spectator.getOpenInventory() instanceof MainBukkitInventoryView view) {
				CreationOptions<PlayerInventorySlot> creationOptions = view.nms.creationOptions;
				Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
				PlaceholderPalette palette = creationOptions.getPlaceholderPalette();

				for (int i = PlayerInventorySlot.PERSONAL_00.defaultIndex(); i <= PlayerInventorySlot.PERSONAL_08.defaultIndex(); i++) {
					Integer rawIndex = mirror.getIndex(PlayerInventorySlot.byDefaultIndex(i));
					if (rawIndex != null) { // null rawIndex does not happen if the server admin configured the template correctly.
						net.minecraft.world.item.ItemStack stack = InvseeImpl.getItemOrPlaceholder(palette, view, rawIndex, placeholderGroup);
						InvseeImpl.sendItemChange(spectator.getHandle(), rawIndex, stack);
					} else {
						InvseeImpl.sendItemChange(spectator.getHandle(), i, CraftItemStack.asNMSCopy(palette.inaccessible()));
					}
				}
			}
		}
	}

	@Override
	public void unwatch() {
		MainNmsInventory nms = getInventory();
		nms.personalContents = nms.craftingContents;

		//send personal slots changes
		for (HumanEntity viewer : getViewers()) {
			if (viewer instanceof CraftPlayer spectator && spectator.getOpenInventory() instanceof MainBukkitInventoryView view) {
				CreationOptions<PlayerInventorySlot> creationOptions = view.nms.creationOptions;
				Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
				PlaceholderPalette palette = creationOptions.getPlaceholderPalette();

				for (int i = PlayerInventorySlot.PERSONAL_00.defaultIndex(); i <= PlayerInventorySlot.PERSONAL_08.defaultIndex(); i++) {
					Integer rawIndex = mirror.getIndex(PlayerInventorySlot.byDefaultIndex(i));
					if (rawIndex != null) { // null rawIndex does not happen if the server admin configured the template correctly.
						net.minecraft.world.item.ItemStack stack = InvseeImpl.getItemOrPlaceholder(palette, view, rawIndex, PlaceholderGroup.CRAFTING);
						InvseeImpl.sendItemChange(spectator.getHandle(), rawIndex, stack);
					} else {
						InvseeImpl.sendItemChange(spectator.getHandle(), i, CraftItemStack.asNMSCopy(palette.inaccessible()));
					}
				}
			}
		}
	}

	@Override
	public ItemStack[] getStorageContents() {
		return getInventory().nmsPlayerInventory.getNonEquipmentItems().stream()
				.map(CraftItemStack::asCraftMirror)
				.toArray(ItemStack[]::new);
	}

	@Override
	public void setStorageContents(ItemStack[] storageContents) {
		Objects.requireNonNull(storageContents, "storageContents cannot be null");
		int storageContentsSize = getInventory().nmsPlayerInventory.getNonEquipmentItems().size();
		if (storageContents.length != storageContentsSize)
			throw new IllegalArgumentException("storage contents must be of length " + storageContentsSize);

		for (int i = 0; i < storageContentsSize; i++) {
			getInventory().nmsPlayerInventory.getNonEquipmentItems().set(i, CraftItemStack.asNMSCopy(storageContents[i]));
		}
	}

	@Override
	public ItemStack[] getArmourContents() {
		return getInventory().nmsPlayerInventory.getArmorContents().stream()
				.map(CraftItemStack::asCraftMirror)
				.toArray(ItemStack[]::new);
	}

	@Override
	public void setArmourContents(ItemStack[] armourContents) {
		Objects.requireNonNull(armourContents, "armourContents cannot be null");
		int armourContentsSize = Inventory.SLOT_OFFHAND - Inventory.INVENTORY_SIZE;
		if (armourContents.length != armourContentsSize)
			throw new IllegalArgumentException("armour contents must be of length " + armourContentsSize);

		var inv = getInventory().nmsPlayerInventory;
		int storageContents = inv.getNonEquipmentItems().size();

		for (int i = 0; i < armourContentsSize; i++) {
			inv.setItem(storageContents + i, CraftItemStack.asNMSCopy(armourContents[i]));
		}
	}

	@Override
	public ItemStack[] getOffHandContents() {
		var inv = getInventory().nmsPlayerInventory;
		// See initialisation of net.minecraft.world.entity.player.Inventory#EQUIPMENT_SLOT_MAPPING.
		return new ItemStack[] {
				CraftItemStack.asCraftMirror(inv.getItem(40)),
				CraftItemStack.asCraftMirror(inv.getItem(41)),
				CraftItemStack.asCraftMirror(inv.getItem(42))
 		};
	}

	@Override
	public void setOffHandContents(ItemStack[] offHand) {
		Objects.requireNonNull(offHand, "offHand cannot be null");
		int offHandContentsSize = 3;
		if (offHand.length != offHandContentsSize)
			throw new IllegalArgumentException("offHand must be of length " + offHandContentsSize);

		var inv = getInventory().nmsPlayerInventory;
		for (int i = 0; i < offHandContentsSize; i++) {
			inv.setItem(40 + i, CraftItemStack.asNMSCopy(offHand[i]));
		}
	}

	@Override
	public void setCursorContents(ItemStack cursor) {
		var onCursor = getInventory().onCursor;
		if (onCursor != null) {
			onCursor.set(CraftItemStack.asNMSCopy(cursor));
		}
	}

	@Override
	public ItemStack getCursorContents() {
		var onCursor = getInventory().onCursor;
		if (onCursor != null) {
			return CraftItemStack.asCraftMirror(onCursor.get());
		} else {
			return null;
		}
	}

	@Override
	public void setPersonalContents(ItemStack[] craftingContents) {
		Objects.requireNonNull(craftingContents, "craftingContents cannot be null");
		
		MainNmsInventory nms = getInventory();
		var nmsCraftingItems = nms.personalContents;
		if (nmsCraftingItems != null) {
			int craftingContentsSize = nmsCraftingItems.size();
			if (craftingContents.length != craftingContentsSize)
				throw new IllegalArgumentException("craftingContents must be of length " + craftingContentsSize);
			
			for (int i = 0; i < craftingContentsSize; i++) {
				nmsCraftingItems.set(i,  CraftItemStack.asNMSCopy(craftingContents[i]));
			}
		}
	}

	@Override
	public ItemStack[] getPersonalContents() {
		var nmsCraftingItems = getInventory().personalContents;
		if (nmsCraftingItems != null) {
			return nmsCraftingItems.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
		} else {
			return new ItemStack[0];
		}
	}

	@Override
	public int getPersonalContentsSize() {
		var nmsCraftingItems = getInventory().personalContents;
		if (nmsCraftingItems != null) {
			return nmsCraftingItems.size();
		} else {
			return 0;
		}
	}

	// org.bukkit.inventory.Inventory overrides

	// lookups

	@Override
	public int first(ItemStack stack) {
		return first(stack, true);
	}

	public int first(ItemStack stack, boolean withAmount) {
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (item != null) {
				if (withAmount) {
					if (item.equals(stack))
						return slot;
				} else {
					if (item.isSimilar(stack))
						return slot;
				}
			} else {
				if (stack == null)
					return slot;
			}
		}

		return -1;
	}

	@Override
	public int first(Material material) {
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if ((item == null && material == null) || (item != null && item.getType() == material))
				return slot;
		}

		return -1;
	}

	@Override
	public int firstEmpty() {
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (ItemUtils.isEmpty(item))
				return slot;
		}

		return -1;
	}

	public int firstPartial(Material material) {
		if (material == null)
			return -1;

		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (item != null && item.getType() == material && item.getAmount() < ItemUtils.getMaxStackSize(item))
				return slot;
		}

		return -1;
	}

	public int firstPartial(ItemStack item) {
		if (item == null)
			return -1;

		item = CraftItemStack.asCraftCopy(item);
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack cItem = getItem(slot);
			if (cItem != null && cItem.getAmount() < ItemUtils.getMaxStackSize(cItem) && cItem.isSimilar(item))
				return slot;
		}

		return -1;
	}

	@Override
	public boolean contains(Material material) {
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if ((item == null && material == null) || (item != null && item.getType() == material))
				return true;
		}
		return false;
	}

	@Override
	public boolean contains(ItemStack stack) {
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (Objects.equals(item, stack))
				return true;
		}
		return false;
	}

	@Override
	public boolean contains(Material material, int amount) {
		if (amount <= 0)
			return true;

		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (item != null && item.getType() == material && (amount -= item.getAmount()) <= 0)
				return true;
		}

		return false;
	}

	@Override
	public boolean contains(ItemStack stack, int count) {
		if (count <= 0)
			return true;

		int encountered = 0;
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (Objects.equals(item, stack)) {
				encountered += 1;
				if (encountered >= count)
					return true;
			}
		}

		return false;
	}

	@Override
	public boolean containsAtLeast(ItemStack stack, int amount) {
		if (amount <= 0)
			return true;
		if (stack == null)
			return false; //this is a bit weird, but this is what CraftInventory does.

		int encountered = 0;
		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (item != null && item.isSimilar(stack)) {
				encountered += item.getAmount();
				if (encountered >= amount)
					return true;
			}
		}

		return false;
	}

	@Override
	public HashMap<Integer, ItemStack> all(Material material) {
		HashMap<Integer, ItemStack> slots = new HashMap<>();

		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if ((item == null && material == null) || (item != null && item.getType() == material))
				slots.put(slot, item);
		}

		return slots;
	}

	@Override
	public HashMap<Integer, ItemStack> all(ItemStack stack) {
		HashMap<Integer, ItemStack> slots = new HashMap<>();

		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (Objects.equals(item, stack))
				slots.put(slot, item);
		}

		return slots;
	}

	// adding

	@Override
	public HashMap<Integer, ItemStack> addItem(ItemStack[] items) {
		HashMap<Integer, ItemStack> leftOvers = new HashMap<>();

		// TODO can optimise this by keeping track of empty slots.

		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				ItemStack leftOver = addItem(items[i]);
				if (leftOver != null && leftOver.getAmount() > 0) {
					leftOvers.put(i, leftOver);
				}
			}
		}

		return leftOvers;
	}

	private ItemStack addItem(ItemStack itemStack) {
		if (itemStack == null || itemStack.getAmount() == 0) return null;

		ItemStack[] storageContents = getStorageContents();
		addItem(storageContents, itemStack, getMaxStackSize());
		setStorageContents(storageContents);

		if (itemStack.getAmount() == 0) return null;

		ItemStack[] armourContents = getArmourContents();
		addItem(armourContents, itemStack, getMaxStackSize());
		setArmourContents(armourContents);

		if (itemStack.getAmount() == 0) return null;

		ItemStack[] offHand = getOffHandContents();
		addItem(offHand, itemStack, getMaxStackSize());
		setOffHandContents(offHand);

		return itemStack; //leftover (couldn't be added)
	}

	static void addItem(final ItemStack[] contents, final ItemStack add, final int inventoryMaxStackSize) {
		assert contents != null && add != null;

		//merge with existing similar item stacks
		for (int i = 0; i < contents.length && add.getAmount() > 0; i++) {
			final ItemStack existingStack = contents[i];
			if (add.isSimilar(existingStack)) {
				final int maxStackSizeForThisItem = Math.min(inventoryMaxStackSize, ItemUtils.getMaxStackSize(existingStack));
				if (existingStack.getAmount() < maxStackSizeForThisItem) {
					//how many can we merge (at most)? it's the minimum of what fits and what we have
					final int maxMergeAmount = Math.min(maxStackSizeForThisItem - existingStack.getAmount(), add.getAmount());
					assert maxMergeAmount > 0 : "Trying to merge a non-positive number of items: " + maxMergeAmount;

					if (add.getAmount() <= maxMergeAmount) {
						//full merge
						existingStack.setAmount(existingStack.getAmount() + add.getAmount());
						add.setAmount(0);
					} else {
						//partial merge (item stack to be added couldn't merge completely into the existing stack)
						assert maxStackSizeForThisItem == existingStack.getAmount() + maxMergeAmount;
						existingStack.setAmount(maxStackSizeForThisItem);
						add.setAmount(add.getAmount() - maxMergeAmount);
					}
				} // else: we cannot merge anything
			}
		}

		//merge with empty slots
		final int maxStackSizeForThisItem = Math.min(inventoryMaxStackSize, Math.min(ItemUtils.getMaxStackSize(add), add.getAmount()));
		for (int i = 0; i < contents.length && add.getAmount() > 0; i++) {
			if (ItemUtils.isEmpty(contents[i])) {
				if (add.getAmount() <= maxStackSizeForThisItem) {
					//full merge
					contents[i] = add.clone();
					add.setAmount(0);
				} else {
					//partial merge (item stack to be added exceeds the inventory's max stack size)
					ItemStack clone = add.clone(); clone.setAmount(maxStackSizeForThisItem);
					contents[i] = clone;
					add.setAmount(add.getAmount() - maxStackSizeForThisItem);
				}
			}
		}
	}

	// removing

	@Override
	public void remove(Material material) {
		if (material == null) return;

		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (item != null && item.getType() == material)
				clear(slot);
		}
	}

	@Override
	public void remove(ItemStack stack) {
		if (stack == null) return;

		for (int slot = 0; slot < getSize(); slot++) {
			ItemStack item = getItem(slot);
			if (item != null && item.equals(stack))
				clear(slot);
		}
	}

	@Override
	public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
		HashMap<Integer, ItemStack> leftOvers = new HashMap<>();

		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				ItemStack leftOver = removeItem(items[i]);
				if (leftOver != null && leftOver.getAmount() > 0) {
					leftOvers.put(i, leftOver);
				}
			}
		}

		return leftOvers;
	}

	private ItemStack removeItem(ItemStack remove) {
		if (remove == null || remove.getAmount() == 0) return null;

		for (int slot = 0; slot < getSize() && remove.getAmount() > 0; slot++) {
			final ItemStack existingStack = getItem(slot);
			if (remove.isSimilar(existingStack)) {
				//how many can we remove (at most)?
				final int maxRemoveAmount = Math.min(existingStack.getAmount(), remove.getAmount());
				//subtract the amount from both item stacks
				existingStack.setAmount(existingStack.getAmount() - maxRemoveAmount);
				remove.setAmount(remove.getAmount() - maxRemoveAmount);
			}
		}

		return remove; //leftover (couldn't be removed)
	}

}
