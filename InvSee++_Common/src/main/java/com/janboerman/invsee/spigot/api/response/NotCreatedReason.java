package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

/**
 * A reason explaining why a SpectatorInventory could not be created.
 * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Basic-usage#a-note-on-notcreatedreason">NotCreatedReason explaination on the wiki</a>
 * @see SpectateResponse
 */
public interface NotCreatedReason {

    public static UnknownTarget unknownTarget(Target target) {
        return new UnknownTarget(target);
    }

    public static TargetDoesNotExist targetDoesNotExists(Target target) {
        return new TargetDoesNotExist(target);
    }

    public static TargetHasExemptPermission targetHasExemptPermission(Target target) {
        return new TargetHasExemptPermission(target);
    }

    @Deprecated
    public static ImplementationFault implementationFault(Target target) {
        return new ImplementationFault(target);
    }

    public static OfflineSupportDisabled offlineSupportDisabled() {
        return OfflineSupportDisabled.INSTANCE;
    }

    public static UnknownReason generic() {
        return UnknownReason.INSTANCE;
    }
}

