package com.janboerman.invsee.spigot.perworldinventory;

import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.data.ProfileKey;
import org.bukkit.GameMode;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ProfileId {

    final ProfileKey profileKey;

    public ProfileId(ProfileKey profileKey) {
        this.profileKey = Objects.requireNonNull(profileKey);
    }

    public ProfileId(PerWorldInventoryHook hook, PwiCommandArgs commandArgs, UUID playerId) {

        Group group;
        GameMode gameMode = commandArgs.gameMode;
        if (gameMode == null || !hook.pwiInventoriesPerGameMode()) gameMode = GameMode.SURVIVAL;

        if (commandArgs.group != null) {
            group = commandArgs.group;
            if (commandArgs.world != null) {
                group.addWorld(commandArgs.world);
            }
        } else if (commandArgs.world != null) {
            //although at runtime the options.world != null check will only be performed once
            //it looks ugly to have this expression twice in the code.
            //can I un-duplicate it?
            group = hook.getGroupForWorld(commandArgs.world);
        } else {
            //unmanaged group!
            group = new Group("", Set.of(), gameMode, null);
        }

        this.profileKey = new ProfileKey(playerId, group, gameMode);
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
