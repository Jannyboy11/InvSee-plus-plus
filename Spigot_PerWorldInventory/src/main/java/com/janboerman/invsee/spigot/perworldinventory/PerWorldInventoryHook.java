package com.janboerman.invsee.spigot.perworldinventory;

import com.google.common.cache.Cache;
import me.ebonjaeger.perworldinventory.Group;
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

    private final Plugin plugin;
    private PerWorldInventory perWorldInventory;
    private PerWorldInventoryAPI api;

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
     * take into account that PerWorldInventory has settings whether to have inventories per gamemode
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



    private Settings getSettings() {
        try {
            Field field = PerWorldInventoryAPI.class.getDeclaredField("settings");
            field.setAccessible(true);
            return (Settings) field.get(api);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return Settings.Companion.create(new File(perWorldInventory.getDataFolder(), "config.yml"));
        }
    }

    @Deprecated
    private ProfileManager getProfileManager() {
        try {
            Field field = PerWorldInventoryAPI.class.getDeclaredField("profileManager");
            field.setAccessible(true);
            return (ProfileManager) field.get(api);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    private DataSource getDataSource() {
        try {
            Field field = ProfileManager.class.getDeclaredField("dataSource");
            field.setAccessible(true);
            return (DataSource) field.get(getProfileManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    private ProfileFactory getProfileFactory() {
        try {
            Field field = ProfileManager.class.getDeclaredField("profileFactor");
            field.setAccessible(true);
            return (ProfileFactory) field.get(getProfileManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    private Cache<ProfileKey, PlayerProfile> getProfileCache() {
        try {
            Field field = ProfileManager.class.getDeclaredField("profileCache");
            field.setAccessible(true);
            return (Cache<ProfileKey, PlayerProfile>) field.get(getProfileManager());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PlayerProfile getOrCreateProfile(Player player, Group group, GameMode gameMode) {
        if (!pwiInventoriesPerGameMode()) gameMode = GameMode.SURVIVAL; //fuck you Kotlin, you can't re-assign your method arguments, can you?! :D

        ProfileKey profileKey = new ProfileKey(player.getUniqueId(), group, gameMode);
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

    public void saveProfile(ProfileKey profileKey, PlayerProfile profile) {
        getDataSource().savePlayer(profileKey, profile);
    }

    @Nullable
    public Group getGroupByName(String group) {
        return api.getGroup(group);
    }

    @NotNull
    public Group getGroupForWorld(String world) {
        return api.getGroupFromWorld(world);
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

}
