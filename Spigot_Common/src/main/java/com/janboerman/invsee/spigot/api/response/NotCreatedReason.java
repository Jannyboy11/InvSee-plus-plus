package com.janboerman.invsee.spigot.api.response;

import com.janboerman.invsee.spigot.api.target.Target;

/**
 * A reason explaining why a SpectatorInventory could not be created
 */
public interface NotCreatedReason {

    /**
     * Get the target for which a SpectatorInventory could not be created
     * @return the target
     */
    public Target getTarget();

    public static TargetDoesNotExist targetDoesNotExists(Target target) {
        return new TargetDoesNotExist(target);
    }

    public static TargetHasExemptPermission targetHasExemptPermission(Target target) {
        return new TargetHasExemptPermission(target);
    }

    public static ImplementationFault implementationFault(Target target) {
        return new ImplementationFault(target);
    }
}

