package com.janboerman.invsee.spigot.perworldinventory;

import com.google.common.cache.Cache;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.GroupManager;
import me.ebonjaeger.perworldinventory.PerWorldInventory;
import me.ebonjaeger.perworldinventory.api.PerWorldInventoryAPI;
import me.ebonjaeger.perworldinventory.configuration.PlayerSettings;
import me.ebonjaeger.perworldinventory.configuration.PluginSettings;
import me.ebonjaeger.perworldinventory.configuration.Settings;
import me.ebonjaeger.perworldinventory.data.*;
import me.ebonjaeger.perworldinventory.permission.PlayerPermission;
import me.ebonjaeger.perworldinventory.service.EconomyService;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PerWorldInventoryHook {

    final Plugin plugin;
    private PerWorldInventory perWorldInventory;
    private PerWorldInventoryAPI api;

    private Settings settings;
    private ProfileManager profileManager;
    private DataSource dataSource;
    private ProfileFactory profileFactory;
    private Cache<ProfileKey, PlayerProfile> profileCache;
    private GroupManager groupManager;
    private EconomyService economyService;

    public PerWorldInventoryHook(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean trySetup() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin pwi = pluginManager.getPlugin("PerWorldInventory");
        boolean success = pwi != null && pwi.isEnabled();
        if (success) {
            this.perWorldInventory = (PerWorldInventory) pwi;
            this.api = perWorldInventory.getApi();

            getGroupManager().loadGroups();
        }
        return success;
    }

    public boolean managesEitherInventory() {
        return pwiManagedInventories() || pwiManagedEnderChests();
    }

    /*
     * Here is what we need to do:
     *
     * for offline players - save and load their inventories from PWI storage
     *
     * listen on InventoryDataLoadEvent? don't think that is necessary.
     *
     * adjust /invsee and /endersee to take take extra parameters (this is going to be annoying for paper's async tabcompleter urghh)
     * I'm thinking that I'm going to do it json-like, e.g.: PerWorldInventory{group=default} or PWI{world=world_nether,gamemode=survival}
     *
     * take into account that PerWorldInventory has settings whether to manage inventories and/or enderchests
     *
     * take into account that PerWorldInventory has a feature that groups unmanaged world into one group (or not, if it's disabled in its settings)
     *
     * take into account that PerWorldInventory has settings whether to have inventories per gamemode (and that there is a bypass permission for this too!)
     */

    protected PerWorldInventoryAPI getPerWorldInventoryAPI() {
        return api;
    }

    protected Settings getSettings() {
        if (settings != null) return settings;

        try {
            Field field = PerWorldInventoryAPI.class.getDeclaredField("settings");
            field.setAccessible(true);
            return settings = (Settings) field.get(getPerWorldInventoryAPI());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return settings = Settings.Companion.create(new File(perWorldInventory.getDataFolder(), "config.yml"));
        }
    }

    protected GroupManager getGroupManager() {
        if (groupManager != null) return groupManager;

        try {
            Field field = PerWorldInventoryAPI.class.getDeclaredField("groupManager");
            field.setAccessible(true);
            return groupManager = (GroupManager) field.get(getPerWorldInventoryAPI());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ProfileManager getProfileManager() {
        if (profileManager != null) return profileManager;

        try {
            Field field = PerWorldInventoryAPI.class.getDeclaredField("profileManager");
            field.setAccessible(true);
            return profileManager = (ProfileManager) field.get(api);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected DataSource getDataSource() {
        if (dataSource != null) return dataSource;

        try {
            Field field = ProfileManager.class.getDeclaredField("dataSource");
            field.setAccessible(true);
            return dataSource = (DataSource) field.get(getProfileManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected EconomyService getEconomyService() {
        if (economyService != null) return economyService;

        try {
            Field field = ProfileManager.class.getDeclaredField("economyService");
            field.setAccessible(true);
            return economyService = (EconomyService) field.get(getProfileManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ProfileFactory getProfileFactory() {
        if (profileFactory != null) return profileFactory;

        try {
            Field field = ProfileManager.class.getDeclaredField("profileFactory");
            field.setAccessible(true);
            return profileFactory = (ProfileFactory) field.get(getProfileManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Cache<ProfileKey, PlayerProfile> getProfileCache() {
        if (profileCache != null) return profileCache;
        
        try {
            Field field = ProfileManager.class.getDeclaredField("profileCache");
            field.setAccessible(true);
            return profileCache = (Cache<ProfileKey, PlayerProfile>) field.get(getProfileManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PlayerProfile getOrCreateProfile(Player player, ProfileKey profileKey) {
        Cache<ProfileKey, PlayerProfile> cache = getProfileCache();

        try {
            return cache.get(profileKey, () -> {
                PlayerProfile loaded = getDataSource().getPlayer(profileKey, player);
                if (loaded != null) {
                    //loaded from flatfile (or mysql in the future?)
                    return loaded;
                } else {
                    //create fresh!
                    return getProfileFactory().create(player);
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PlayerProfile getOrCreateProfile(Player player, Group group, GameMode gameMode) {
        if (!pwiInventoriesPerGameMode()) gameMode = GameMode.SURVIVAL; //fuck you Kotlin, you can't re-assign your method arguments, can you?! :D

        ProfileKey profileKey = new ProfileKey(player.getUniqueId(), group, gameMode);
        return getOrCreateProfile(player, profileKey);
    }

    @Nullable
    public Group getGroupByName(String group) {
        return getPerWorldInventoryAPI().getGroup(group);
    }

    @NotNull
    public Group getGroupForWorld(String world) {
        return getPerWorldInventoryAPI().getGroupFromWorld(world);
    }

    public boolean worldsShareInventory(String world1, String world2) {
        return getPerWorldInventoryAPI().canWorldsShare(world1, world2);
    }

    public ProfileKey getActiveProfileKey(HumanEntity player) {
        GameMode gameMode = pwiInventoriesPerGameMode() ? player.getGameMode() : GameMode.SURVIVAL;
        Group group = getGroupForWorld(player.getWorld().getName());
        UUID uuid = player.getUniqueId();
        return new ProfileKey(uuid, group, gameMode);
    }

    public boolean isMatchedByProfile(HumanEntity player, ProfileKey profileKey) {
        return isMatchedByProfile(player.getUniqueId(), player.getWorld().getName(), player.getGameMode(), profileKey);
    }

    public boolean isMatchedByProfile(UUID playerId, Group group, GameMode gameMode, ProfileKey profileKey) {
        return profileKey.getUuid().equals(playerId)
                && profileKey.getGroup().getWorlds().equals(group.getWorlds())
                && (!pwiInventoriesPerGameMode() || profileKey.getGameMode() == gameMode);
    }

    public boolean isMatchedByProfile(UUID playerId, String world, GameMode gameMode, ProfileKey profileKey) {
        return profileKey.getUuid().equals(playerId)
                && profileKey.getGroup().getWorlds().contains(world)
                && (!pwiInventoriesPerGameMode() || profileKey.getGameMode() == gameMode);
    }

    public boolean isWorldManagedByPWI(String world) {
        return pwiUnmanagedWorldsSameGroup()
                || getGroupManager().getGroups().values().stream().anyMatch(g -> g.getWorlds().contains(world));
    }

    public boolean isGroupManagedByPWI(String group) {
        Group g = getGroupByName(group);
        if (g == null) return false;
        return isGroupManagedByPWI(g);
    }

    public boolean isGroupManagedByPWI(Group group) {
        return group.getConfigured();
    }

    public boolean bypassesGameModeChange(Player player) {
        return !pwiBypassDisabled() && player.hasPermission(PlayerPermission.BYPASS_GAMEMODE.getNode());
    }

    public boolean bypassesWorldChange(Player player) {
        return !pwiBypassDisabled() && player.hasPermission(PlayerPermission.BYPASS_WORLDS.getNode());
    }

    public boolean pwiManagedInventories() {
        return getSettings().getProperty(PlayerSettings.LOAD_INVENTORY);
    }

    public boolean pwiManagedEnderChests() {
        return getSettings().getProperty(PlayerSettings.LOAD_ENDER_CHEST);
    }

    public boolean pwiBypassDisabled() {
        return getSettings().getProperty(PluginSettings.DISABLE_BYPASS);
    }

    public boolean pwiInventoriesPerGameMode() {
        return getSettings().getProperty(PluginSettings.SEPARATE_GM_INVENTORIES);
    }

    public boolean pwiUnmanagedWorldsSameGroup() {
        return getSettings().getProperty(PluginSettings.SHARE_IF_UNCONFIGURED);
    }

    public boolean pwiLoadDataOnJoin() {
        return getSettings().getProperty(PluginSettings.LOAD_DATA_ON_JOIN);
    }

}
