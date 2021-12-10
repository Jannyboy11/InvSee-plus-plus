package com.janboerman.invsee.spigot.impl_1_18_1_R1;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventory;

import java.util.UUID;

class EnderBukkitInventory extends CraftInventory implements EnderSpectatorInventory {

	protected EnderBukkitInventory(EnderNmsInventory inventory) {
		super(inventory);
	}
	
	@Override
	public EnderNmsInventory getInventory() {
		return (EnderNmsInventory) super.getInventory();
	}

	@Override
	public String getSpectatedPlayerName() {
		return getInventory().targetPlayerName;
	}

	@Override
	public UUID getSpectatedPlayerId() {
		return getInventory().targetPlayerUuid;
	}

	@Override
	public String getTitle() {
		return getInventory().title;
	}
	
}
