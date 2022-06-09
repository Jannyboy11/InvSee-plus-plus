package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

/**
 * A reason explaining why a SpectatorInventory could not be created
 */
public interface NotCreatedReason {

    /**
     * Get the target for which a SpectatorInventory could not be created
     * @return the target
     * @throws UnsupportedOperationException if the reason has no target player
     */
    @Deprecated(forRemoval = true)
    public default Target getTarget() {
        throw new UnsupportedOperationException("This NotCreatedReason has no target player.");
    }

    public static TargetDoesNotExist targetDoesNotExists(Target target) {
        return new TargetDoesNotExist(target);
    }

    public static TargetHasExemptPermission targetHasExemptPermission(Target target) {
        return new TargetHasExemptPermission(target);
    }

    public static ImplementationFault implementationFault(Target target) {
        return new ImplementationFault(target);
    }

    public static OfflineSupportDisabled offlineSupportDisabled() {
        return OfflineSupportDisabled.INSTANCE;
    }
}

