package com.janboerman.invsee.spigot.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.ProfileKey;

import java.util.Objects;
import java.util.UUID;

public class ProfileId {

    final ProfileKey profileKey;

    public ProfileId(ProfileKey profileKey) {
        this.profileKey = Objects.requireNonNull(profileKey);
    }

    public ProfileId(MviCommandArgs mviOptions, UUID playerId) {
        this.profileKey = ProfileKey.createProfileKey(mviOptions.containerType, mviOptions.containerName, mviOptions.profileType, playerId);
    }

    public ProfileId(MviCommandArgs mviOptions, UUID playerId, String playerName) {
        this.profileKey = ProfileKey.createProfileKey(mviOptions.containerType, mviOptions.containerName, mviOptions.profileType, playerId, playerName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ProfileId)) return false;

        ProfileId that = (ProfileId) o;
        return this.profileKey.equals(that.profileKey);
    }

    @Override
    public int hashCode() {
        return profileKey.hashCode();
    }

    @Override
    public String toString() {
        return "ProfileId(" + profileKey.toString() + ")";
    }

}
