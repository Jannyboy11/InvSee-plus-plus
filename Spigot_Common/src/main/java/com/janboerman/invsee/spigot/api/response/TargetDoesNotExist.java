package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;

public class TargetDoesNotExist implements NotCreatedReason {

    private final Target target;

    TargetDoesNotExist(Target target) {
        this.target = Objects.requireNonNull(target);
    }

    public Target getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(target);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TargetDoesNotExist)) return false;

        TargetDoesNotExist that = (TargetDoesNotExist) obj;
        return Objects.equals(this.getTarget(), that.getTarget());
    }
}
