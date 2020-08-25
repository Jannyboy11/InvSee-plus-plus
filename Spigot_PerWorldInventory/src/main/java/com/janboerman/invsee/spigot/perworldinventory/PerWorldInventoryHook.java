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
import org.bukkit.GameMode;
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

    public PerWorldInventoryHook(Plugin plugin) {
        this.plugin = plugin;
    }

    /*
     * Here is what we need to do
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

    public boolean trySetup() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin pwi = pluginManager.getPlugin("PerWorldInventory");
        boolean success = pwi != null && pwi.isEnabled();
        if (success) {
            this.perWorldInventory = (PerWorldInventory) pwi;
            this.api = perWorldInventory.getApi();
        }
        return success;
    }

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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    public void saveProfile(ProfileKey profileKey, PlayerProfile profile) {
        getDataSource().savePlayer(profileKey, profile);
    }

    @Nullable
    public Group getGroupByName(String group) {
        return getPerWorldInventoryAPI().getGroup(group);
    }

    @NotNull
    public Group getGroupForWorld(String world) {
        return getPerWorldInventoryAPI().getGroupFromWorld(world);
    }

    public boolean pwiManagedInventories() {
        return getSettings().getProperty(PlayerSettings.LOAD_INVENTORY);
    }

    public boolean pwiManagedEnderChests() {
        return getSettings().getProperty(PlayerSettings.LOAD_ENDER_CHEST);
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
