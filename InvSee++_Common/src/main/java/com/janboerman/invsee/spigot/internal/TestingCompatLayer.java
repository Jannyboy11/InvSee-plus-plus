package com.janboerman.invsee.spigot.internal;

import java.util.UUID;

public interface TestingCompatLayer {

    /** Get the player's NBT tag compound */
    Object loadPlayerSaveCompound(UUID playerId, String playerName);
}
