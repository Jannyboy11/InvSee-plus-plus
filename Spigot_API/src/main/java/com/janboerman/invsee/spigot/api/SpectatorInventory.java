package com.janboerman.invsee.spigot.api;

import java.util.UUID;

public interface SpectatorInventory {

    public String getSpectatedPlayerName();

    public UUID getSpectatedPlayerId();

    public String getTitle();

}
