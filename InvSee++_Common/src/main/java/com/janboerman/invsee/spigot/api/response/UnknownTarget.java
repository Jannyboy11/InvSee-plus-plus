package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;

public class UnknownTarget extends AbstractNotCreatedReason {

    UnknownTarget(Target target) {
        super(target);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTarget());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UnknownTarget)) return false;

        UnknownTarget that = (UnknownTarget) o;
        return Objects.equals(this.getTarget(), that.getTarget());
    }

    @Override
    public String toString() {
        return "Target " + getTarget() + " has not played on the server before";
    }

}
