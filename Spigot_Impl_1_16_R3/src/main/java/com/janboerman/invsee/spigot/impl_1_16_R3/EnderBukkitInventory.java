package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.internal.inventory.Wrapper;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventory;

import java.util.UUID;

public class EnderBukkitInventory extends CraftInventory implements EnderSpectatorInventory, Wrapper<EnderNmsInventory, EnderBukkitInventory> {

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
    public String getTitle() {
        return getInventory().title;
    }

    @Override
    public Mirror<EnderChestSlot> getMirror() {
        return getInventory().mirror;
    }

}
