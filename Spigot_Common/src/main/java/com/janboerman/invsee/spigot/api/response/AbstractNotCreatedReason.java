package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;

public abstract class AbstractNotCreatedReason implements NotCreatedReason {

    private final Target target;

    protected AbstractNotCreatedReason(Target target) {
        this.target = Objects.requireNonNull(target, "target cannot be null");
    }

    public final Target getTarget() {
        return target;
    }

}
