package com.janboerman.invsee.spigot.api.target;

import org.bukkit.entity.HumanEntity;

import java.util.UUID;

/** Abstraction for (possibly offline) target players. */
public interface Target {

    /** Create the target by their username. */
    public static Target byUsername(String username) {
        return new UsernameTarget(username);
    }

    /** Create the target by their unique ID. */
    public static Target byUniqueId(UUID uniqueId) {
        return new UniqueIdTarget(uniqueId);
    }

    /** Create the target from a player object. */
    public static Target byPlayer(HumanEntity player) {
        return new PlayerTarget(player);
    }

    /** Create the target by their unique ID and username. */
    public static Target byGameProfile(UUID uniqueId, String userName) {
        return new GameProfileTarget(uniqueId, userName);
    }

}

