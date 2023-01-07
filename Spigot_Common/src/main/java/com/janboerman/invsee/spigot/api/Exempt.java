package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.target.PlayerTarget;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.target.UniqueIdTarget;
import com.janboerman.invsee.spigot.api.target.UsernameTarget;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import java.util.Objects;
import java.util.UUID;

public class Exempt {

    private static final String EXEMPT_INVENTORY = "invseeplusplus.exempt.invsee";
    private static final String EXEMPT_ENDERCHEST = "invseeplusplus.exempt.endersee";

    private Server server;

    private Permission permission;

    Exempt(Server server) {
        this.server = Objects.requireNonNull(server);

        if (server.getPluginManager().isPluginEnabled("Vault")) {
            ServicesManager servicesManager = server.getServicesManager();
            RegisteredServiceProvider<Permission> provider = servicesManager.getRegistration(Permission.class);
            if (provider != null) {
                permission = provider.getProvider();
            }
        }
    }

    private boolean vaultPermissionEnabled() {
        return permission != null && permission.isEnabled();
    }

    public boolean isExemptedFromHavingMainInventorySpectated(Target target) {
        return isExempted(target, EXEMPT_INVENTORY);
    }

    public boolean isExemptedFromHavingEnderchestSpectated(Target target) {
        return isExempted(target, EXEMPT_ENDERCHEST);
    }

    private boolean isExempted(Target target, String permission) {
        if (target instanceof PlayerTarget) {
            return ((PlayerTarget) target).getPlayer().hasPermission(permission);
        }

        if (vaultPermissionEnabled()) {
            if (target instanceof UniqueIdTarget) {
                return vaultHasPermission(((UniqueIdTarget) target).getUniqueId(), permission);
            } else if (target instanceof UsernameTarget) {
                return vaultHasPermission(((UsernameTarget) target).getUsername(), permission);
            }
        }

        return false;
    }

    private boolean vaultHasPermission(UUID uniqueId, String permission) {
        return this.permission.playerHas(server.getWorlds().get(0).getName(), server.getOfflinePlayer(uniqueId), permission);
    }

    @Deprecated
    private boolean vaultHasPermission(String username, String permission) {
        return this.permission.playerHas(server.getWorlds().get(0).getName(), username, permission);
    }

    private boolean vaultHasPermission(UUID uniqueId, String world, String permission) {
        return this.permission.playerHas(world, server.getOfflinePlayer(uniqueId), permission);
    }

    @Deprecated
    private boolean vaultHasPermission(String username, String world, String permission) {
        return this.permission.playerHas(world, username, permission);
    }

    public boolean isExemptedFromHavingMainInventorySpectated(Target target, String world) {
        return isExempted(target, world, EXEMPT_INVENTORY);
    }

    public boolean isExemptedFromHavingEnderchestSpectated(Target target, String world) {
        return isExempted(target, world, EXEMPT_ENDERCHEST);
    }

    private boolean isExempted(Target target, String world, String permission) {
        if (target instanceof PlayerTarget) {
            return ((PlayerTarget) target).getPlayer().hasPermission(permission);
        }

        if (vaultPermissionEnabled()) {
            if (target instanceof UniqueIdTarget) {
                return vaultHasPermission(((UniqueIdTarget) target).getUniqueId(), world, permission);
            } else if (target instanceof UsernameTarget) {
                return vaultHasPermission(((UsernameTarget) target).getUsername(), world, permission);
            }
        }

        return false;
    }

}
