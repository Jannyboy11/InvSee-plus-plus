package com.janboerman.invsee.spigot.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.ProfileKey;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;

import java.util.Objects;
import java.util.UUID;

public class ProfileId {

    private final UUID playerId;
    private final String playerName;
    private final ContainerType containerType;  //World or Group.
    private final String containerName; //corresponds to 'dataName' in MVI. Allowed values are same as save folder names: "worlds", "groups" and "players".
    private final ProfileType profileType; //null for gamemode-unspecific profiles

    ProfileId(MultiverseInventoriesHook hook, ProfileKey profileKey) {
        this.playerId = profileKey.getPlayerUUID();
        this.playerName = profileKey.getPlayerName();
        this.containerType = profileKey.getContainerType();
        this.containerName = profileKey.getDataName();
        this.profileType = hook.gameModeSpecificProfiles() ? profileKey.getProfileType() : null;
    }

    public ProfileId(MultiverseInventoriesHook hook, MviCommandArgs args, UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.containerType = args.containerType;
        this.containerName = args.containerName;
        this.profileType = hook.gameModeSpecificProfiles() ? args.profileType : null;
    }

    public boolean isMatchedByProfileKey(MultiverseInventoriesHook hook, ProfileKey profileKey) {
        boolean result = playerId.equals(profileKey.getPlayerUUID());
        if (containerType != null && profileKey.getContainerType() != null) result &= containerType.equals(profileKey.getContainerType());
        if (containerName != null && profileKey.getDataName() != null) result &= containerType.equals(profileKey.getDataName());
        if (hook.gameModeSpecificProfiles() && profileType != null && profileKey.getProfileType() != null) result &= profileType.equals(profileKey.getProfileType());
        return result;
    }

    public ProfileKey toProfileKey() {
        ProfileType profileType = this.profileType;
        if (profileType == null) profileType = ProfileTypes.SURVIVAL;
        if (playerName == null || playerName.equals("InvSee++ Player")) {
            return ProfileKey.createProfileKey(containerType, containerName, profileType, playerId, playerName);
        } else {
            return ProfileKey.createProfileKey(containerType, containerName, profileType, playerId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ProfileId)) return false;

        ProfileId that = (ProfileId) o;
        return Objects.equals(this.playerId, that.playerId)
                && Objects.equals(this.containerType, that.containerType)
                && Objects.equals(this.containerName, that.containerName)
                && Objects.equals(this.profileType, that.profileType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, containerType, containerName, profileType);
    }

    @Override
    public String toString() {
        return "ProfileId(" + playerId + "," + playerName + "," + containerType + "," + containerName + "," + profileType + ")";
    }
}
