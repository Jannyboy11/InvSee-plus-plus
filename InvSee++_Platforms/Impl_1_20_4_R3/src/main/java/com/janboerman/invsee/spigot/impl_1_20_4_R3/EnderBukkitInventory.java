package com.janboerman.invsee.spigot.impl_1_20_4_R3;

import com.janboerman.invsee.spigot.internal.inventory.EnderInventory;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

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
			if (existingStack != null && existingStack.isSimilar(add)) {
				final int maxStackSizeForThisItem = Math.min(inventoryMaxStackSize, Math.max(existingStack.getMaxStackSize(), add.getAmount()));
				if (existingStack.getAmount() < maxStackSizeForThisItem) {
					//how many can we merge (at most)?
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
					}
				}
			}
		}

		//merge with empty slots
		final int maxStackSizeForThisItem = Math.min(inventoryMaxStackSize, Math.max(add.getMaxStackSize(), add.getAmount()));
		for (int i = 0; i < contents.length && add.getAmount() > 0; i++) {
			if (contents[i] == null || contents[i].getAmount() == 0 || contents[i].getType() == Material.AIR) {
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
