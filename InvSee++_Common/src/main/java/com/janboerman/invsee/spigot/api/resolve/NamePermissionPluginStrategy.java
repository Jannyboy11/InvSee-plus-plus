package com.janboerman.invsee.spigot.api.resolve;

import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NamePermissionPluginStrategy implements NameResolveStrategy {
    private final Plugin plugin;
    private final Server server;
    private final Scheduler scheduler;

    public NamePermissionPluginStrategy(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.scheduler = scheduler;
    }

    private static Optional<String> firstPresentOptional(Optional<String> one, Optional<String> two) {
        if (one.isPresent()) return one;
        return two;
    }


    @Override
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        return resolveUsingLuckPerms(uniqueId)
                .thenCombine(resolveUsingGroupManager(uniqueId), NamePermissionPluginStrategy::firstPresentOptional)
                .thenCombine(resolveUsingBungeePerms(uniqueId), NamePermissionPluginStrategy::firstPresentOptional)
                .thenCombine(resolveUsingUltraPermissions(uniqueId), NamePermissionPluginStrategy::firstPresentOptional);
    }

    private CompletableFuture<Optional<String>> resolveUsingLuckPerms(UUID uniqueId) {
        final CompletableFuture<Optional<String>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("LuckPerms")) {
                net.luckperms.api.LuckPerms api = net.luckperms.api.LuckPermsProvider.get();
                net.luckperms.api.model.user.UserManager userManager = api.getUserManager();
                net.luckperms.api.model.user.User user = userManager.getUser(uniqueId);

                if (user != null) {
                    resultFuture.complete(Optional.ofNullable(user.getUsername()));
                } else {
                    CompletableFuture<String> luckPermsFuture = userManager.lookupUsername(uniqueId);
                    luckPermsFuture.whenComplete((userName, error) -> {
                        if (error != null) resultFuture.complete(Optional.ofNullable(userName));
                        else resultFuture.complete(Optional.empty());
                    });
                }
            } else {
                resultFuture.complete(Optional.empty());
            }
        });

        return resultFuture;
    }

    private CompletableFuture<Optional<String>> resolveUsingGroupManager(UUID uniqueId) {
        final CompletableFuture<Optional<String>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("GroupManager")) {
                if (server.getPluginManager().isPluginEnabled("GroupManager")) {
                    org.anjocaido.groupmanager.GroupManager groupManager = (org.anjocaido.groupmanager.GroupManager) server.getPluginManager().getPlugin("GroupManager");

                    org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder worldsHolder = groupManager.getWorldsHolder();
                    org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder overloadedWorldHolder = worldsHolder.getDefaultWorld();
                    org.anjocaido.groupmanager.data.User user = overloadedWorldHolder.getUser(uniqueId.toString());
                    //funnily enough, GroupManager will just create a User without a UUID if it doesn't know about one.
                    //hence 'user' is always non-null!

                    resultFuture.complete(Optional.ofNullable(user.getLastName()));
                } else {
                    resultFuture.complete(Optional.empty());
                }
            }
        });

        return resultFuture;
    }

    private CompletableFuture<Optional<String>> resolveUsingBungeePerms(UUID uniqueId) {
        final CompletableFuture<Optional<String>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("BungeePerms")) {
                net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin bukkitPlugin = (net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin) server.getPluginManager().getPlugin("BungeePerms");
                net.alpenblock.bungeeperms.BungeePerms bungeePerms = bukkitPlugin.getBungeeperms();
                net.alpenblock.bungeeperms.PermissionsManager permissionsManager = bungeePerms.getPermissionsManager();

                scheduler.executeAsync(() -> {
                    try {
                        net.alpenblock.bungeeperms.User user = permissionsManager.getUser(uniqueId, true);
                        if (user == null) {
                            resultFuture.complete(Optional.empty());
                        } else {
                            resultFuture.complete(Optional.ofNullable(user.getName()));
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

    private CompletableFuture<Optional<String>> resolveUsingUltraPermissions(UUID uniqueId) {
        final CompletableFuture<Optional<String>> resultFuture = new CompletableFuture<>();

        scheduler.executeSyncGlobal(() -> {
            if (server.getPluginManager().isPluginEnabled("UltraPermissions")) {
                me.TechsCode.UltraPermissions.UltraPermissionsAPI api = me.TechsCode.UltraPermissions.UltraPermissions.getAPI();

                scheduler.executeAsync(() -> {
                    try {
                        me.TechsCode.UltraPermissions.storage.collection.UserList userList = api.getUsers();
                        Optional<me.TechsCode.UltraPermissions.storage.objects.User> optUser = userList.uuid(uniqueId);
                        resultFuture.complete(optUser.map(me.TechsCode.UltraPermissions.storage.objects.User::getName));
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
