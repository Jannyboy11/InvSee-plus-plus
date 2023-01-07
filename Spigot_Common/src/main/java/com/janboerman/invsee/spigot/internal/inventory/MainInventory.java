package com.janboerman.invsee.spigot.internal.inventory;

import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;

import java.util.UUID;

public interface MainInventory<NMS extends AbstractNmsInventory<PlayerInventorySlot, Self, NMS>, Self extends MainInventory<NMS, Self>> extends StandardSpectatorInventory<PlayerInventorySlot, NMS, Self>, MainSpectatorInventory, Personal {

    @Override
    public default String getSpectatedPlayerName() {
        return StandardSpectatorInventory.super.getSpectatedPlayerName();
    }

    @Override
    public default UUID getSpectatedPlayerId() {
        return StandardSpectatorInventory.super.getSpectatedPlayerId();
    }

    @Override
    public default String getTitle() {
        return StandardSpectatorInventory.super.getTitle();
    }

    @Override
    public default Mirror<PlayerInventorySlot> getMirror() {
        return StandardSpectatorInventory.super.getMirror();
    }

    @Override
    public default void setContents(MainSpectatorInventory newContents) {
        MainSpectatorInventory.super.setContents(newContents);
    }

}
