package com.janboerman.invsee.spigot.api.target;

import java.util.Objects;

public class UsernameTarget implements Target {

    private final String username;

    /** @deprecated Use {@link Target#byUsername(String)} instead. */
    public UsernameTarget(String username) {
        this.username = Objects.requireNonNull(username);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return getUsername();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof UsernameTarget)) return false;

        UsernameTarget that = (UsernameTarget) obj;
        return Objects.equals(this.getUsername(), that.getUsername());
    }

}
