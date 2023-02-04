package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Objects;

@Deprecated(forRemoval = true, since = "0.19.0")
public class ImplementationFault extends AbstractNotCreatedReason {

    ImplementationFault(Target target) {
        super(target);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTarget());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ImplementationFault)) return false;

        ImplementationFault that = (ImplementationFault) obj;
        return Objects.equals(this.getTarget(), that.getTarget());
    }

    @Override
    public String toString() {
        return "Implementation fault";
    }
}
