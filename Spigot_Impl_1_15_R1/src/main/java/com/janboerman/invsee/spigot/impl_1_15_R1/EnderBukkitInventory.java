package com.janboerman.invsee.spigot.impl_1_15_R1;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.internal.inventory.Wrapper;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventory;

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
        return getInventory().creationOptions.getTitle().titleFor(Target.byGameProfile(getSpectatedPlayerId(), getSpectatedPlayerName()));
    }

    @Override
    public Mirror<EnderChestSlot> getMirror() {
        return getInventory().creationOptions.getMirror();
    }

    @Override
    public CreationOptions<EnderChestSlot> getCreationOptions() {
        return getInventory().creationOptions.clone();
    }

}
