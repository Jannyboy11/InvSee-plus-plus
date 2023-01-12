package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;

public class TargetDoesNotExist extends UnknownTarget {

    TargetDoesNotExist(Target target) {
        super(target);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTarget());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TargetDoesNotExist)) return false;

        TargetDoesNotExist that = (TargetDoesNotExist) obj;
        return Objects.equals(this.getTarget(), that.getTarget());
    }

    @Override
    public String toString() {
        return "Target " + getTarget() + " does not exist";
    }
}
