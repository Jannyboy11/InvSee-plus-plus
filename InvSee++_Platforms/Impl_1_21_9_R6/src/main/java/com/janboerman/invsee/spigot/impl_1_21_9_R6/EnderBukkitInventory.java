package com.janboerman.invsee.spigot.impl_1_21_9_R6;

import java.util.HashMap;

import com.janboerman.invsee.spigot.internal.inventory.EnderInventory;

import org.bukkit.craftbukkit.v1_21_R6.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;

class EnderBukkitInventory extends CraftInventory implements EnderInventory<EnderNmsInventory, EnderBukkitInventory> {

	protected EnderBukkitInventory(EnderNmsInventory inventory) {
		super(inventory);
	}
	
	@Override
	public EnderNmsInventory getInventory() {
		return (EnderNmsInventory) super.getInventory();
	}


	// custom 'add item' algorithm that respects item stack sizes and the inventory max stack size

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

		return itemStack; //leftover (couldn't be added)
	}

	private static void addItem(final ItemStack[] contents, final ItemStack add, final int inventoryMaxStackSize) {
		assert contents != null && add != null;

		//merge with existing similar item stacks
		for (int i = 0; i < contents.length && add.getAmount() > 0; i++) {
			final ItemStack existingStack = contents[i];
			if (add.isSimilar(existingStack)) {
				final int maxStackSizeForThisItem = Math.min(inventoryMaxStackSize, ItemUtils.getMaxStackSize(existingStack));
				if (existingStack.getAmount() < maxStackSizeForThisItem) {
					//how many can we merge (at most)? it's the minimum of what fits and what we have
					final int maxMergeAmount = Math.min(maxStackSizeForThisItem - existingStack.getAmount(), add.getAmount());
					if (maxMergeAmount > 0) {
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

}
