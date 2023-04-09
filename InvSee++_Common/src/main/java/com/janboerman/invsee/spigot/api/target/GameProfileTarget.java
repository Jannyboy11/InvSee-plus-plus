package com.janboerman.invsee.spigot.api.target;

import java.util.Objects;
import java.util.UUID;

public class GameProfileTarget extends UniqueIdTarget {

    private final String userName;

    /** @deprecated Use {@link Target#byGameProfile(UUID, String)} instead.*/
    public GameProfileTarget(UUID uniqueId, String username) {
        super(uniqueId);
        this.userName = Objects.requireNonNull(username);
    }

    @Override
    public String toString() {
        return userName;
    }

}
