package com.janboerman.invsee.spigot.api.target;

import java.util.Objects;
import java.util.UUID;

public class UniqueIdTarget implements Target {

    private final UUID uniqueId;

    public UniqueIdTarget(UUID uniqueId) {
        this.uniqueId = Objects.requireNonNull(uniqueId);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String toString() {
        return getUniqueId().toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uniqueId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof UniqueIdTarget)) return false;

        UniqueIdTarget that = (UniqueIdTarget) obj;
        return Objects.equals(this.getUniqueId(), that.getUniqueId());
    }

}
