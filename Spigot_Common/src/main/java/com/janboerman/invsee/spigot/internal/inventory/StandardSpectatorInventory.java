package com.janboerman.invsee.spigot.internal.inventory;

import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.template.Mirror;

import java.util.UUID;

public interface StandardSpectatorInventory<Slot, NMS extends AbstractNmsInventory<Slot, Self, NMS>, Self extends StandardSpectatorInventory<Slot, NMS, Self>> extends SpectatorInventory<Slot>, Wrapper<NMS, Self> {

    public default String getSpectatedPlayerName() {
        return getInventory().targetPlayerName;
    }

    public default UUID getSpectatedPlayerId() {
        return getInventory().targetPlayerUuid;
    }

    public default String getTitle() {
        return getInventory().title;
    }

    public default Mirror<Slot> getMirror() {
        return getInventory().mirror;
    }

}
