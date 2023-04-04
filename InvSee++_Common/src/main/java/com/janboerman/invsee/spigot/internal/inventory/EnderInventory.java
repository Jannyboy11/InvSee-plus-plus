package com.janboerman.invsee.spigot.internal.inventory;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import java.util.UUID;

public interface EnderInventory<NMS extends AbstractNmsInventory<EnderChestSlot, Self, NMS>, Self extends EnderInventory<NMS, Self>> extends StandardSpectatorInventory<EnderChestSlot, NMS, Self>, EnderSpectatorInventory {

    @Override
    default String getSpectatedPlayerName() {
        return StandardSpectatorInventory.super.getSpectatedPlayerName();
    }

    @Override
    default UUID getSpectatedPlayerId() {
        return StandardSpectatorInventory.super.getSpectatedPlayerId();
    }

    @Override
    default String getTitle() {
        return StandardSpectatorInventory.super.getTitle();
    }

    @Override
    default Mirror<EnderChestSlot> getMirror() {
        return StandardSpectatorInventory.super.getMirror();
    }

    @Override
    default void setContents(EnderSpectatorInventory newContents) {
        EnderSpectatorInventory.super.setContents(newContents);
    }

}
