package com.janboerman.invsee.spigot.impl_1_20_2_R2;

import com.janboerman.invsee.spigot.internal.inventory.EnderInventory;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventory;

class EnderBukkitInventory extends CraftInventory implements EnderInventory<EnderNmsInventory, EnderBukkitInventory> {

	protected EnderBukkitInventory(EnderNmsInventory inventory) {
		super(inventory);
	}
	
	@Override
	public EnderNmsInventory getInventory() {
		return (EnderNmsInventory) super.getInventory();
	}

}
