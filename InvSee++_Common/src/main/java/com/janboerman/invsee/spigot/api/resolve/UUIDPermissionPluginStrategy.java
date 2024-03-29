package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A strategy which tries to resolve players' Unique IDs using a handful of popular permission plugins.
 */
public class UUIDPermissionPluginStrategy implements UUIDResolveStrategy {

    private final Plugin plugin;
    private final Server server;
    private final Scheduler scheduler;

    public UUIDPermissionPluginStrategy(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.scheduler = scheduler;
    }

    private static Optional<UUID> firstPresentOptional(Optional<UUID> one, Optional<UUID> two) {
        if (one.isPresent()) return one;
        return two;
    }

    @Override
    public CompletableFuture<Optional<UUID>> resolveUniqueId(String userName) {
        return resolveUsingLuckPerms(userName)
                .thenCombine(resolveUsingGroupManager(userName), UUIDPermissionPluginStrategy::firstPresentOptional)
                .thenCombine(resolveUsingBungeePerms(userName), UUIDPermissionPluginStrategy::firstPresentOptional)
                .thenCombine(resolveUsingUltraPermissions(userName), UUIDPermissionPluginStrategy::firstPresentOptional);
        //PermissionsEx? version 1.x seems to be deprecated, but 2.0's development seems to be halted.
        //Any other permission plugins we're forgetting?
    }

    private CompletableFuture<Optional<UUID>> resolveUsingLuckPerms(String userName) {
        final CompletableFuture<Optional<UUID>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("LuckPerms")) {
                net.luckperms.api.LuckPerms api = net.luckperms.api.LuckPermsProvider.get();
                net.luckperms.api.model.user.UserManager userManager = api.getUserManager();
                net.luckperms.api.model.user.User user = userManager.getUser(userName);

                if (user != null) {
                    resultFuture.complete(Optional.ofNullable(user.getUniqueId()));
                } else {
                    CompletableFuture<UUID> luckPermsFuture = userManager.lookupUniqueId(userName);
                    luckPermsFuture.whenComplete((uuid, error) -> {
                        if (error != null) resultFuture.complete(Optional.ofNullable(uuid));
                        else resultFuture.complete(Optional.empty());
                    });
                }
            } else {
                resultFuture.complete(Optional.empty());
            }
        });

        return resultFuture;
    }

    private CompletableFuture<Optional<UUID>> resolveUsingGroupManager(String userName) {
        final CompletableFuture<Optional<UUID>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("GroupManager")) {
                org.anjocaido.groupmanager.GroupManager groupManager = (org.anjocaido.groupmanager.GroupManager) server.getPluginManager().getPlugin("GroupManager");

                org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder worldsHolder = groupManager.getWorldsHolder();
                org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder overloadedWorldHolder = worldsHolder.getWorldDataByPlayerName(userName);
                if (overloadedWorldHolder == null) {
                    overloadedWorldHolder = worldsHolder.getDefaultWorld();
                }
                org.anjocaido.groupmanager.data.User user = overloadedWorldHolder.getUser(userName);
                //funnily enough, GroupManager will just create a User without a UUID if it doesn't know about one.
                //hence 'user' is always non-null!

                String userId = user.getUUID();
                try {
                    resultFuture.complete(userId == null ? Optional.empty() : Optional.of(UUID.fromString(userId)));
                } catch (IllegalArgumentException e) {
                    resultFuture.complete(Optional.empty());
                }
            } else {
                resultFuture.complete(Optional.empty());
            }
        });

        return resultFuture;
    }

    private CompletableFuture<Optional<UUID>> resolveUsingBungeePerms(String userName) {
        final CompletableFuture<Optional<UUID>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("BungeePerms")) {
                net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin bukkitPlugin = (net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin) server.getPluginManager().getPlugin("BungeePerms");
                net.alpenblock.bungeeperms.BungeePerms bungeePerms = bukkitPlugin.getBungeeperms();
                net.alpenblock.bungeeperms.PermissionsManager permissionsManager = bungeePerms.getPermissionsManager();

                scheduler.executeAsync(() -> {
                    try {
                        net.alpenblock.bungeeperms.User user = permissionsManager.getUser(userName, true);
                        if (user == null) {
                            resultFuture.complete(Optional.empty());
                        } else {
                            resultFuture.complete(Optional.ofNullable(user.getUUID()));
                        }
                    } catch (Throwable t) {
                        resultFuture.complete(Optional.empty());
                    }
                });
            } else {
                resultFuture.complete(Optional.empty());
            }
        });

        return resultFuture;
    }

    private CompletableFuture<Optional<UUID>> resolveUsingUltraPermissions(String userName) {
        final CompletableFuture<Optional<UUID>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("UltraPermissions")) {
                me.TechsCode.UltraPermissions.UltraPermissionsAPI api = me.TechsCode.UltraPermissions.UltraPermissions.getAPI();

                scheduler.executeAsync(() -> {
                    try {
                        me.TechsCode.UltraPermissions.storage.collection.UserList userList = api.getUsers();
                        Optional<me.TechsCode.UltraPermissions.storage.objects.User> optUser = userList.name(userName);
                        resultFuture.complete(optUser.map(me.TechsCode.UltraPermissions.storage.objects.User::getUuid));
                    } catch (Throwable t) {
                        resultFuture.complete(Optional.empty());
                    }
                });
            } else {
                resultFuture.complete(Optional.empty());
            }
        });

        return resultFuture;
    }

}
