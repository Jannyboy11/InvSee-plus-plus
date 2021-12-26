package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;

public class ImplementationFault implements NotCreatedReason {

    private final Target target;

    ImplementationFault(Target target) {
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
        if (!(obj instanceof ImplementationFault)) return false;

        ImplementationFault that = (ImplementationFault) obj;
        return Objects.equals(this.getTarget(), that.getTarget());
    }

}
