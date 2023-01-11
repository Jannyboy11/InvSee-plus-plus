package com.janboerman.invsee.spigot.api.target;

import java.util.Objects;
import java.util.UUID;

public class GameProfileTarget extends UniqueIdTarget {

    private final String userName;

    public GameProfileTarget(UUID uniqueId, String username) {
        super(uniqueId);
        this.userName = Objects.requireNonNull(username);
    }

    @Override
    public String toString() {
        return userName;
    }

}
