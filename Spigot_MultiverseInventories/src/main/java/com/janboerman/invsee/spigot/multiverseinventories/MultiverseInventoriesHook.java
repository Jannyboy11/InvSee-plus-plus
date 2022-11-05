package com.janboerman.invsee.spigot.multiverseinventories;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.InventoriesConfig;

import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.profile.ProfileDataSource;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.UUID;

public class MultiverseInventoriesHook {

    final Plugin plugin;
    private MultiverseInventories multiverseInventories;
    private InventoriesConfig mviConfig;
    private ProfileDataSource profileDataSource;
    private WorldGroupManager worldGroupManager;

    public MultiverseInventoriesHook(Plugin plugin) {
        this.plugin = plugin;
    }

    /* TO-DO list:
     *
     * for offline players - save and load their inventories from MVI storage
     * take into account the save_load_on_log_in_out setting.
     *
     * listen on GameModeChangeShareHandlingEvent
     * listen on WorldchangeShareHandlingEvent
     * (these are events that get called by MVI if a player changes to a new world group)
     * the "read profile" is the new profile that will be applied
     * the "write profile" is the old profile that is no longer needed and thus saved to disk
     * the "always write profile" is
     *
     * take into account the user_game_mode_profiles setting (different inventories/stats per game-mode)
     *
     * take into account that MVI has bypass permissions (and a setting that disables bypass permissions)
     *
     * figure out what to do with ungrouped worlds (may need to inspect the default_ungrouped_worlds) setting
     *
     * figure out what to do with optional shares
     *
     * Why does MVI have two profile container stores? (one for groups, one for worlds)
     * Each WeakProfileContainer has a Map<String=playerName, Map<ProfileType=gameMode, PlayerProfile>>
     *
     * design philosopy: https://www.spigotmc.org/threads/invsee.456148/page-2#post-3928841
     *
     * handy documentation: https://github.com/Multiverse/Multiverse-Core/wiki/Sharing-Details-(Inventories)
     */

    //with Multiverse-Inventories, the following attributes can be shared per group:
    /* all -> all the properties listed below
     * inventory -> inventory_contents + armour_contents + off_hand + ender_chest
     * And there are more aggregate sharing properties. See Sharables.java for all combinations!
     *
     * ender_chest
     * inventory_contents
     * armor_contents
     * off_hand
     * hit_points
     * remaining_air
     * fall_distance
     * fire_ticks
     * xp
     * lvl
     * total_xp
     * food_level
     * exhaustion
     * saturation
     * bed_spawn
     * last_location
     * economy
     * portion_effects
     */

    public boolean trySetup() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin mvi = pluginManager.getPlugin("Multiverse-Inventories");
        boolean success = mvi != null && mvi.isEnabled();
        if (success) {
            this.multiverseInventories = (MultiverseInventories) mvi;
            this.mviConfig = multiverseInventories.getMVIConfig();
            this.profileDataSource = multiverseInventories.getData();
            this.worldGroupManager = multiverseInventories.getGroupManager();

            //TODO load instances of important classes into fields

        }
        return success;
    }

    public boolean mviChecksByPassPermissions() {
        //DO I even need this at all?

        //bypass permissions:
        /* BYPASS_GROUP
         * BYPASS_GROUP_ALL
         * BYPASS_WORLD
         * BYPASS_WORLD_ALL
         * BYPASS_GAME_MODE
         * BYPASS_GAME_MODE_ALL
         * BYPASS_ALL
         */
        return mviConfig.isUsingBypass();
    }

    /* GlobalProfile:
     * private final UUID uuid;
     * private String lastWorld = null;
     */
    public GlobalProfile getGlobalProfile(UUID playerId, String playerName) {
        return profileDataSource.getGlobalProfile(playerName, playerId);
    }

    public WorldGroup getWorldGroupByName(String groupName) {
        return worldGroupManager.getGroup(groupName);
    }

    public Collection<WorldGroup> getWorldGroups() {
        return worldGroupManager.getGroups();
    }

    public boolean gameModeSpecificProfiles() {
        return mviConfig.isUsingGameModeProfiles();
    }

    public boolean saveAndLoadOnLoginAndLogout() {
        return mviConfig.usingLoggingSaveLoad();
    }

}
