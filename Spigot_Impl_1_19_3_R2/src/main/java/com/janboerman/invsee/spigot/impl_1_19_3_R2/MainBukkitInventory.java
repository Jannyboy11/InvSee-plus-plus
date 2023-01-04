package com.janboerman.invsee.spigot.impl_1_19_3_R2;

import com.janboerman.invsee.spigot.internal.inventory.MainInventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MerchantContainer;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_19_R2.inventory.*;

import java.util.HashMap;
import java.util.Objects;

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
		
		MainNmsInventory nms = getInventory();
		var top = targetPlayerView.getTopInventory();
		if (top instanceof CraftInventoryCrafting cic) {
			CraftingContainer targetCrafting = (CraftingContainer) cic.getInventory();
			nms.personalContents = targetCrafting.getContents(); //luckily, this does not create a copy.
		} else if (top instanceof CraftResultInventory cri) {
			//anvil, grindstone, loom, smithing table, cartography table, stone cutter
			Container repairItems = cri.getInventory();
			nms.personalContents = repairItems.getContents();
		} else if (top instanceof CraftInventoryEnchanting cie) {
			Container enchantItems = cie.getInventory();
			nms.personalContents = enchantItems.getContents();
		} else if (top instanceof CraftInventoryMerchant cim) {
			MerchantContainer merchantItems = cim.getInventory();
			nms.personalContents = merchantItems.getContents();
		}
		
		//do this at the nms level so that I can save on packets? (only need to update the last 9 slots :-))
		for (HumanEntity viewer : getViewers()) {
			if (viewer instanceof org.bukkit.entity.Player) {
				((org.bukkit.entity.Player) viewer).updateInventory();
			}
		}
	}

	@Override
	public void unwatch() {
		MainNmsInventory nms = getInventory();
		nms.personalContents = nms.craftingContents;
		
		//idem
		for (HumanEntity viewer : getViewers()) {
			if (viewer instanceof org.bukkit.entity.Player) {
				((org.bukkit.entity.Player) viewer).updateInventory();
			}
		}
	}

	@Override
	public ItemStack[] getStorageContents() {
		return getInventory().storageContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
	}

	@Override
	public void setStorageContents(ItemStack[] storageContents) {
		Objects.requireNonNull(storageContents, "storageContents cannot be null");
		int storageContentsSize = getInventory().storageContents.size();
		if (storageContents.length != storageContentsSize)
			throw new IllegalArgumentException("storage contents must be of length " + storageContentsSize);

		for (int i = 0; i < storageContentsSize; i++) {
			getInventory().storageContents.set(i, CraftItemStack.asNMSCopy(storageContents[i]));
		}
	}

	@Override
	public ItemStack[] getArmourContents() {
		return getInventory().armourContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
	}

	@Override
	public void setArmourContents(ItemStack[] armourContents) {
		Objects.requireNonNull(armourContents, "armourContents cannot be null");
		int armourContentsSize = getInventory().armourContents.size();
		if (armourContents.length != armourContentsSize)
			throw new IllegalArgumentException("armour contents must be of length " + armourContentsSize);
		
		for (int i = 0; i < armourContentsSize; i++) {
			getInventory().armourContents.set(i, CraftItemStack.asNMSCopy(armourContents[i]));
		}
	}

	@Override
	public ItemStack[] getOffHandContents() {
		return getInventory().offHand.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
	}

	@Override
	public void setOffHandContents(ItemStack[] offHand) {
		Objects.requireNonNull(offHand, "offHand cannot be null");
		int offHandContentsSize = getInventory().offHand.size();
		if (offHand.length != offHandContentsSize)
			throw new IllegalArgumentException("offHand must be of length " + offHandContentsSize);
		
		for (int i = 0; i < offHandContentsSize; i++) {
			getInventory().offHand.set(i,  CraftItemStack.asNMSCopy(offHand[i]));
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

	@Override
	public int first(ItemStack stack) {
		assert stack != null;

		ItemStack[] storageContents = getStorageContents();
		for (int i = 0; i < storageContents.length; i++) {
			if (storageContents[i] != null && storageContents[i].isSimilar(stack))
				return i;
		}

		ItemStack[] armourContents = getArmourContents();
		for (int i = 0; i < armourContents.length; i++) {
			if (armourContents[i] != null && armourContents[i].isSimilar(stack))
				return i + storageContents.length;
		}

		ItemStack[] offHandContents = getOffHandContents();
		for (int i = 0; i < offHandContents.length; i++) {
			if (offHandContents[i] != null && offHandContents[i].isSimilar(stack))
				return i + storageContents.length + armourContents.length;
		}

		return -1;
	}

	@Override
	public int first(Material material) {
		assert material != null;

		ItemStack[] storageContents = getStorageContents();
		for (int i = 0; i < storageContents.length; i++) {
			if (storageContents[i] != null && storageContents[i].getType() == material)
				return i;
		}

		ItemStack[] armourContents = getArmourContents();
		for (int i = 0; i < armourContents.length; i++) {
			if (armourContents[i] != null && armourContents[i].getType() == material)
				return i + storageContents.length;
		}

		ItemStack[] offHandContents = getOffHandContents();
		for (int i = 0; i < offHandContents.length; i++) {
			if (offHandContents[i] != null && offHandContents[i].getType() == material)
				return i + storageContents.length + armourContents.length;
		}

		return -1;
	}

	@Override
	public int firstEmpty() {

		ItemStack[] storageContents = getStorageContents();
		for (int i = 0; i < storageContents.length; i++) {
			if (storageContents[i] == null || storageContents[i].getAmount() == 0)
				return i;
		}

		ItemStack[] armourContents = getArmourContents();
		for (int i = 0; i < armourContents.length; i++) {
			if (armourContents[i] == null || armourContents[i].getAmount() == 0)
				return i + storageContents.length;
		}

		ItemStack[] offHandContents = getOffHandContents();
		for (int i = 0; i < offHandContents.length; i++) {
			if (offHandContents[i] == null || offHandContents[i].getAmount() == 0)
				return i + storageContents.length + armourContents.length;
		}

		return -1;
	}

	@Override
	public HashMap<Integer, ItemStack> addItem(ItemStack[] items) {
		assert items != null;

		HashMap<Integer, ItemStack> leftOvers = new HashMap<>();
		for (int i = 0; i < items.length; i++) {
			ItemStack leftOver = addItem(items[i]);
			if (leftOver != null && leftOver.getAmount() > 0) {
				leftOvers.put(i, leftOver);
			}
		}

		return leftOvers;
	}

}
