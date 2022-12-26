package com.janboerman.invsee.spigot.impl_1_17_1_R1;

import java.util.UUID;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;

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