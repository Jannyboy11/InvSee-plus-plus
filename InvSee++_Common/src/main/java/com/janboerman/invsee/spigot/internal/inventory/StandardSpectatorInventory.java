package com.janboerman.invsee.spigot.internal.inventory;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.target.Target;
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
        return getInventory().creationOptions.getTitle().titleFor(Target.byGameProfile(getSpectatedPlayerId(), getSpectatedPlayerName()));
    }

    public default Mirror<Slot> getMirror() {
        return getInventory().creationOptions.getMirror();
    }

    public default CreationOptions<Slot> getCreationOptions() {
        return getInventory().creationOptions.clone();
    }

}
