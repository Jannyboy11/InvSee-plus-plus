package com.janboerman.invsee.spigot.impl_1_12_R1;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;

import java.util.UUID;

public class EnderBukkitInventory extends CraftInventory implements EnderSpectatorInventory {

    protected EnderBukkitInventory(EnderNmsInventory inventory) {
        super(inventory);
    }

    @Override
    public EnderNmsInventory getInventory() {
        return (EnderNmsInventory) super.getInventory();
    }

    @Override
    public String getSpectatedPlayerName() {
        return getInventory().spectatedPlayerName;
    }

    @Override
    public UUID getSpectatedPlayerId() {
        return getInventory().spectatedPlayerUuid;
    }

    @Override
    public Mirror<EnderChestSlot> getMirror() {
        return getInventory().mirror;
    }

    // getTitle() already correctly implemented by CraftInventory!

}
