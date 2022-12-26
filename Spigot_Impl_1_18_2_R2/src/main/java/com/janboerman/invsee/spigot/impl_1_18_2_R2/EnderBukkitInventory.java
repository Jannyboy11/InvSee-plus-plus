package com.janboerman.invsee.spigot.impl_1_18_2_R2;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventory;

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

	@Override
	public Mirror<EnderChestSlot> getMirror() {
		return getInventory().mirror;
	}
	
}
