package com.janboerman.invsee.spigot.perworldinventory;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import com.janboerman.invsee.spigot.internal.inventory.Personal;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.data.PlayerProfile;
import me.ebonjaeger.perworldinventory.data.ProfileKey;
import me.ebonjaeger.perworldinventory.event.InventoryLoadCompleteEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class PerWorldInventorySeeApi extends InvseeAPI {

    private final InvseeAPI wrapped;
    private final PerWorldInventoryHook pwiHook;

    //there can be more than one open spectator inventories per target player.
    //use the superclass openInventory-mechanic only for profile-unspecific spectator inventories
    private final Map<ProfileKey, MainSpectatorInventory> inventories = new HashMap<>();
    private final Map<MainSpectatorInventory, ProfileKey> inventoryKeys = new HashMap<>();
    private final Map<ProfileKey, EnderSpectatorInventory> enderchests = new HashMap<>();
    private final Map<EnderSpectatorInventory, ProfileKey> enderchestKeys = new HashMap<>();

    private PwiEventListener pwiEventListener;
    private TiedInventoryListener tiedInventoryListener;
    private TiedPlayerListener tiedPlayerListener;

    public PerWorldInventorySeeApi(Plugin plugin, InvseeAPI wrapped, PerWorldInventoryHook pwiHook) {
        super(plugin);
        this.wrapped = Objects.requireNonNull(wrapped);
        this.pwiHook = Objects.requireNonNull(pwiHook);

        wrapped.unregisterListeners();
        setOpenInventories(getOpenInventories(wrapped));
        setOpenEnderChests(getOpenEnderChests(wrapped));

        //these influence the PlayerListener
        setMainInventoryTransferPredicate((spectatorInventory, player) -> {
            if (!pwiHook.pwiManagedInventories()) return true;

            // a player logs in and his inventory was being edited by somebody.
            // do we transfer the contents from the spectator to the live player?
            // only if the inventories share the same group!

            ProfileKey profileKey = inventoryKeys.get(spectatorInventory);
            if (profileKey == null) return true; //not tied to a profile, so just transfer

            //check whether world and gamemode match
            return pwiHook.isMatchedByProfile(player, profileKey);
        });
        setEnderChestTransferPredicate((spectatorInventory, player) -> {
            if (!pwiHook.pwiManagedEnderChests()) return true;

            // a player logs in and his enderchest was being edited by somebody.
            // do we transfer the contents from the spectator to the live player?
            // only if the enderchests share the same group!

            ProfileKey profileKey = enderchestKeys.get(spectatorInventory);
            if (profileKey == null) return true; //not tied to a profile, so just transfer

            //check whether world and gamemode match
            return pwiHook.isMatchedByProfile(player, profileKey);
        });
    }

    public void registerListeners() {
        super.registerListeners();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(pwiEventListener = new PwiEventListener(), plugin);
        pluginManager.registerEvents(tiedInventoryListener = new TiedInventoryListener(), plugin);
        pluginManager.registerEvents(tiedPlayerListener = new TiedPlayerListener(), plugin);
    }

    public void unregisterListeners() {
        super.unregisterListeners();
        HandlerList.unregisterAll(pwiEventListener);
        HandlerList.unregisterAll(tiedInventoryListener);
        HandlerList.unregisterAll(tiedPlayerListener);
    }

    public PerWorldInventoryHook getHook() {
        return pwiHook;
    }

    private final class TiedPlayerListener implements Listener {

        @EventHandler
        public void onTargetQuit(PlayerQuitEvent event) {
            //remove from maps if nobody is watching.

            Player player = event.getPlayer();
            ProfileKey key = pwiHook.getActiveProfileKey(player);

            MainSpectatorInventory mainSpectator = inventories.get(key);
            if (mainSpectator != null && mainSpectator.getViewers().isEmpty()) {
                inventories.remove(key);
                inventoryKeys.remove(mainSpectator, key);
            }

            EnderSpectatorInventory enderSpectator = enderchests.get(key);
            if (enderSpectator != null && enderSpectator.getViewers().isEmpty()) {
                enderchests.remove(key);
                enderchestKeys.remove(enderSpectator, key);
            }
        }
    }

    private final class TiedInventoryListener implements Listener {

        @EventHandler
        public void onSpectatorClose(InventoryCloseEvent event) {
            Inventory inventory = event.getInventory();
            if (inventory instanceof MainSpectatorInventory) {
                MainSpectatorInventory main = (MainSpectatorInventory) inventory;

                ProfileKey key = inventoryKeys.get(main);
                if (key != null) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        //remove from maps if nobody is watching
                        if (main.getViewers().isEmpty()) {
                            inventories.remove(key, main);
                            inventoryKeys.remove(main, key);
                        }
                    }, 20L * 5);
                }
            } else if (inventory instanceof EnderSpectatorInventory) {
                EnderSpectatorInventory ender = (EnderSpectatorInventory) inventory;

                ProfileKey key = enderchestKeys.get(ender);
                if (key != null) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        //remove from maps if nobody is watching
                        if (ender.getViewers().isEmpty()) {
                            enderchests.remove(key, ender);
                            enderchestKeys.remove(ender, key);
                        }
                    }, 20L * 5);
                }
            }
        }

        @EventHandler
        public void onTargetInventoryOpen(InventoryOpenEvent event) {
            HumanEntity player = event.getPlayer();

            ProfileKey activeProfileKey = pwiHook.getActiveProfileKey(player);
            MainSpectatorInventory mainSpectator = inventories.get(activeProfileKey);
            if (mainSpectator instanceof Personal) {
                ((Personal) mainSpectator).watch(event.getView());
            }

            wrapped.getOpenMainSpectatorInventory(player.getUniqueId()).ifPresent(spectator -> {
                if (spectator instanceof Personal) {
                    ((Personal) spectator).watch(event.getView());
                }
            });
        }

        @EventHandler
        public void onTargetInventoryClose(InventoryCloseEvent event) {
            HumanEntity player = event.getPlayer();

            ProfileKey activeProfileKey = pwiHook.getActiveProfileKey(player);
            MainSpectatorInventory mainSpectator = inventories.get(activeProfileKey);
            if (mainSpectator instanceof Personal) {
                ((Personal) mainSpectator).unwatch();
            }

            wrapped.getOpenMainSpectatorInventory(player.getUniqueId()).ifPresent(spectator -> {
                if (spectator instanceof Personal) {
                    ((Personal) spectator).unwatch();
                }
            });
        }
    }

    private final class PwiEventListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onTeleport(PlayerTeleportEvent event) {
            if (pwiHook.bypassesWorldChange(event.getPlayer())) return;

            World from = event.getFrom().getWorld(); assert from != null;
            World to = event.getTo().getWorld(); assert to != null;

            if (!from.equals(to) && !pwiHook.worldsShareInventory(from.toString(), to.toString())) {
                giveSnapshotInventoryToSpectators(pwiHook.getActiveProfileKey(event.getPlayer()));
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onGameModeChange(PlayerGameModeChangeEvent event) {
            if (pwiHook.bypassesGameModeChange(event.getPlayer())) return;

            if (pwiHook.pwiInventoriesPerGameMode()) {
                giveSnapshotInventoryToSpectators(pwiHook.getActiveProfileKey(event.getPlayer()));
            }
        }

        //can't use InventoryLoadEvent because I can't get the old profile key from it!

        private void giveSnapshotInventoryToSpectators(ProfileKey oldProfileKey) {
            //new data is about to be loaded onto the player

            //  if there are 'live' spectator inventories for the player, then
            //      take a snapshot of the inventory, and 're-open' for all viewers

            MainSpectatorInventory mainSpectator = inventories.get(oldProfileKey);
            EnderSpectatorInventory enderSpectator = enderchests.get(oldProfileKey);

            if (mainSpectator != null) {
                List<HumanEntity> viewers = new ArrayList<>(mainSpectator.getViewers());   //copy
                ItemStack[] contents = mainSpectator.getContents();                        //already is a copy

                viewers.forEach(HumanEntity::closeInventory);

                CompletableFuture<Optional<MainSpectatorInventory>> snapshotFuture = asSnapShotInventory(mainSpectator);
                snapshotFuture.thenAccept(optional -> optional.ifPresentOrElse(newSpectatorInventory -> {
                    inventories.put(oldProfileKey, newSpectatorInventory);
                    inventoryKeys.put(newSpectatorInventory, oldProfileKey);
                    newSpectatorInventory.setContents(contents);
                    viewers.forEach(v -> v.openInventory(newSpectatorInventory));
                }, /*orElse part*/ () -> inventories.remove(oldProfileKey)));
            }

            if (enderSpectator != null) {
                List<HumanEntity> viewers = new ArrayList<>(enderSpectator.getViewers());   //copy
                ItemStack[] contents = enderSpectator.getContents();                        //already is a copy

                CompletableFuture<Optional<EnderSpectatorInventory>> snapshotFuture = asSnapShotInventory(enderSpectator);
                snapshotFuture.thenAccept(optional -> optional.ifPresentOrElse(newSpectatorInventory -> {
                    enderchests.put(oldProfileKey, newSpectatorInventory);
                    enderchestKeys.put(newSpectatorInventory, oldProfileKey);
                    newSpectatorInventory.setContents(contents);
                    viewers.forEach(v -> v.openInventory(newSpectatorInventory));
                }, /*orElse part*/ () -> enderchests.remove(oldProfileKey)));
            }
        }


        @EventHandler
        public void onPwiLoadComplete(InventoryLoadCompleteEvent event) {
            ProfileKey newProfileKey = new ProfileKey(event.getPlayer().getUniqueId(), event.getGroup(), event.getGameMode());
            giveLiveInventoryToSpectators(newProfileKey);
        }

        private void giveLiveInventoryToSpectators(ProfileKey newProfileKey) {
            //new inventory contents was loaded onto the player.

            //  if there is an open spectator inventory for the new profile, then
            //      close the spectator inventory for all viewers
            //      re-open a live spectator inventory, tied to the same profile key

            MainSpectatorInventory mainSpectator = inventories.get(newProfileKey);
            EnderSpectatorInventory enderSpectator = enderchests.get(newProfileKey);

            if (mainSpectator != null) {

                List<HumanEntity> viewers = new ArrayList<>(mainSpectator.getViewers());    //copy
                ItemStack[] contents = mainSpectator.getContents();                         //already is a copy
                viewers.forEach(HumanEntity::closeInventory);

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    //run in the next tick to ensure that the player has changed worlds and the live inventory is actually really live
                    Optional<MainSpectatorInventory> liveFuture = asLiveInventory(mainSpectator, false);
                    liveFuture.ifPresentOrElse(liveSpectator -> {
                        inventories.put(newProfileKey, liveSpectator);
                        inventoryKeys.put(liveSpectator, newProfileKey);
                        liveSpectator.setContents(contents);    //updates the player's inventory!
                        viewers.forEach(v -> v.openInventory(liveSpectator));
                    }, /*orElse part*/ () -> inventories.remove(newProfileKey));
                });
            }

            if (enderSpectator != null) {
                List<HumanEntity> viewers = new ArrayList<>(enderSpectator.getViewers());    //copy
                ItemStack[] contents = enderSpectator.getContents();                         //already is a copy
                viewers.forEach(HumanEntity::closeInventory);

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    //run in the next tick to ensure that the player has changed worlds and the live inventory is actually really live
                    Optional<EnderSpectatorInventory> liveFuture = asLiveInventory(enderSpectator, false);
                    liveFuture.ifPresentOrElse(liveSpectator -> {
                        enderchests.put(newProfileKey, liveSpectator);
                        enderchestKeys.put(liveSpectator, newProfileKey);
                        liveSpectator.setContents(contents);
                        viewers.forEach(v -> v.openInventory(liveSpectator));
                    }, /*orElse part*/ () -> inventories.remove(newProfileKey));
                });
            }
        }
    }

    @Override
    public void openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, String title, Mirror<PlayerInventorySlot> mirror) {
        wrapped.openMainSpectatorInventory(spectator, spectatorInventory, title, mirror);
    } //TODO overload with ProfileKey?

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title, Mirror<PlayerInventorySlot> mirror) {
        return wrapped.spectateInventory(player, title, mirror);
    }

    public MainSpectatorInventory spectateInventory(HumanEntity player, String title, Mirror<PlayerInventorySlot> mirror, ProfileKey profileKey) {
        //return from cache? but that does not guarantee it's live, so for now, don't use the cache.

        MainSpectatorInventory spectatorInv = spectateInventory(player, title, mirror);
        inventories.put(profileKey, spectatorInv);
        inventoryKeys.put(spectatorInv, profileKey);
        return spectatorInv;
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> spectateInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror, ProfileId profileId) {
        Player player = plugin.getServer().getPlayer(playerId);
        ProfileKey profileKey = profileId.profileKey;
        if (player != null && getHook().isMatchedByProfile(player, profileKey))
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectateInventory(player, title, mirror, profileKey)));

        return createOfflineInventory(playerId, playerName, title, mirror, profileKey);
    }

    @Override
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror) {
        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(playerId, playerName, plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        Group group = pwiHook.getGroupForWorld(world.getName());
        ProfileKey profileKey = new ProfileKey(playerId, group, GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        return createOfflineInventory(playerId, playerName, title, mirror, profileKey, false).thenApply(SpectateResponse::toOptional);
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory) {
        ProfileKey profileKey = inventoryKeys.get(inventory);
        boolean saveVanilla = false;

        Location location = null;
        Player target = plugin.getServer().getPlayer(inventory.getSpectatedPlayerId());
        if (target != null) location = target.getLocation();
        if (location == null) location = pwiHook.getDataSource().getLogout(new FakePlayer(inventory.getSpectatedPlayerId(), inventory.getSpectatedPlayerName(), plugin.getServer()));
        World logoutWorld = location != null ? location.getWorld() : plugin.getServer().getWorlds().get(0); assert logoutWorld != null;

        if (profileKey == null) {
            saveVanilla = true;
            profileKey = new ProfileKey(inventory.getSpectatedPlayerId(), pwiHook.getGroupForWorld(logoutWorld.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        } else if (!pwiHook.pwiLoadDataOnJoin() && profileKey.getGroup().containsWorld(logoutWorld.getName())) {
            //the implementation of Group#containsWorld seems bugged - PWI keeps reporting that it creates groups on the fly!
            saveVanilla = true;
        }

        return saveInventory(inventory, profileKey, saveVanilla);
    }

    @Override
    public void openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, String title, Mirror<EnderChestSlot> mirror) {
        wrapped.openEnderSpectatorInventory(spectator, spectatorInventory, title, mirror);
    } //TODO overload with ProfileKey?

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title, Mirror<EnderChestSlot> mirror) {
        return wrapped.spectateEnderChest(player, title, mirror);
    }

    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title, Mirror<EnderChestSlot> mirror, ProfileKey profileKey) {
        EnderSpectatorInventory spectatorInv = spectateEnderChest(player, title, mirror);
        enderchests.put(profileKey, spectatorInv);
        enderchestKeys.put(spectatorInv, profileKey);
        return spectatorInv;
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> spectateEnderChest(UUID playerId, String playerName, String title, Mirror<EnderChestSlot> mirror, ProfileId profileId) {
        Player player = plugin.getServer().getPlayer(playerId);
        ProfileKey profileKey = profileId.profileKey;
        if (player != null && pwiHook.isMatchedByProfile(player, profileKey))
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectateEnderChest(player, title, mirror, profileKey)));

        return createOfflineEnderChest(playerId, playerName, title, mirror, profileKey);
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, Mirror<EnderChestSlot> mirror) {
        Optional<EnderSpectatorInventory> cached = wrapped.getOpenEnderSpectatorInventory(playerId);
        if (cached.isPresent()) return CompletableFuture.completedFuture(cached);

        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(playerId, playerName, plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        Group group = pwiHook.getGroupForWorld(world.getName());
        ProfileKey profileKey = new ProfileKey(playerId, group, GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        return createOfflineEnderChest(playerId, playerName, title, mirror, profileKey, false).thenApply(SpectateResponse::toOptional);
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest) {
        ProfileKey profileKey = inventoryKeys.get(enderChest);

        boolean saveVanilla = false;

        Location location = null;
        Player player = plugin.getServer().getPlayer(enderChest.getSpectatedPlayerId());
        if (player != null) location = player.getLocation();
        if (location == null) location = pwiHook.getDataSource().getLogout(new FakePlayer(enderChest.getSpectatedPlayerId(), enderChest.getSpectatedPlayerName(), plugin.getServer()));
        World logoutWorld = location != null ? location.getWorld() : plugin.getServer().getWorlds().get(0);

        if (profileKey == null) {
            saveVanilla = true;
            profileKey = new ProfileKey(enderChest.getSpectatedPlayerId(), pwiHook.getGroupForWorld(logoutWorld.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        } else if (!pwiHook.pwiLoadDataOnJoin() && profileKey.getGroup().containsWorld(logoutWorld.getName())) {
            //the implementation of Group#containsWorld seems bugged - PWI keeps reporting that it creates groups on the fly!
            saveVanilla = true;
        }

        return saveEnderChest(enderChest, profileKey, saveVanilla);
    }

    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror, ProfileKey profileKey) {
        return createOfflineInventory(playerId, playerName, title, mirror, profileKey, pwiHook.isGroupManagedByPWI(profileKey.getGroup()));
    }

    private CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror, ProfileKey profileKey, boolean tieToProfile) {
        //don't ask the cache because it may contain a live inventory! (and we could get called by asSnapshotInventory!)

        //check whether the player is not exempted.
        final Target target = Target.byUniqueId(playerId);
        if (profileKey.getGroup().getWorlds().stream().anyMatch(world -> exempt.isExemptedFromHavingMainInventorySpectated(target, world)))
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

        //try non-managed
        CompletableFuture<Optional<MainSpectatorInventory>> fromVanillaStorageOfflineInv = wrapped.createOfflineInventory(playerId, playerName, title, mirror);
        if (!pwiHook.pwiManagedInventories()) return fromVanillaStorageOfflineInv.thenApply(opt -> SpectateResponse.fromOptional(opt, NotCreatedReason.implementationFault(target)));

        //create a fake player for PWI so that we can load data onto it!
        FakePlayer player = new FakePlayer(playerId, playerName, plugin.getServer());
        PlayerInventory playerInv = player.getInventory();

        AtomicBoolean loadFromPWI = new AtomicBoolean(true);
        Location location = pwiHook.getDataSource().getLogout(player);
        World logoutWorld = location != null ? location.getWorld() : plugin.getServer().getWorlds().get(0);
        if (!pwiHook.pwiLoadDataOnJoin() && profileKey.getGroup().containsWorld(logoutWorld.getName())) {
            loadFromPWI.set(false); //just load from vanilla.
        }

        return fromVanillaStorageOfflineInv.thenApplyAsync(optionalSpectatorInv -> {
            optionalSpectatorInv.ifPresent(spectatorInv -> {

                //first set the minecraft-saved contents onto the player
                playerInv.setStorageContents(spectatorInv.getStorageContents());
                playerInv.setArmorContents(spectatorInv.getArmourContents());
                playerInv.setExtraContents(spectatorInv.getOffHandContents());
                player.setItemOnCursor(spectatorInv.getCursorContents());

                if (loadFromPWI.get()) {
                    //load the data from the player onto the profile, or load the profile from persistent storage
                    PlayerProfile profile = pwiHook.getOrCreateProfile(player, profileKey);

                    //then set it back from the profile
                    spectatorInv.setStorageContents(Arrays.copyOf(profile.getInventory(), 36));
                    spectatorInv.setArmourContents(Arrays.copyOfRange(profile.getInventory(), 36, 40));
                    spectatorInv.setOffHandContents(Arrays.copyOfRange(profile.getInventory(), 40, 41));
                    //PlayerProfile has no getter for the item on the cursor!
                }

                //mark inventory as tied to the profile key
                if (tieToProfile) {
                    inventoryKeys.put(spectatorInv, profileKey);
                    inventories.put(profileKey, spectatorInv);
                }
            });

            return SpectateResponse.fromOptional(optionalSpectatorInv, NotCreatedReason.implementationFault(target));
        }, serverThreadExecutor);
    }

    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory, ProfileKey profileKey, boolean saveVanilla) {
        //if the spectated player is managed by PWI (because its world is managed by PWI)
        //then also save the inventory to PWI's storage
        //that can be done by loading the profile, applying the contents from the MainSpectatorInventory and saving it again

        if (!pwiHook.pwiManagedInventories()) {
            //not managed by pwi
            return wrapped.saveInventory(inventory);

        } else {
            Player player = plugin.getServer().getPlayer(inventory.getSpectatedPlayerId());
            if (player == null) player = new FakePlayer(inventory.getSpectatedPlayerId(), inventory.getSpectatedPlayerName(), plugin.getServer());
            PlayerInventory playerInv = player.getInventory();

            playerInv.setStorageContents(inventory.getStorageContents());
            playerInv.setArmorContents(inventory.getArmourContents());
            playerInv.setItemInOffHand(inventory.getOffHandContents()[0]);
            player.setItemOnCursor(inventory.getCursorContents());

            PlayerProfile profile = pwiHook.getOrCreateProfile(player, profileKey);

            ItemStack[] profileArmour = inventory.getArmourContents(); //should be redundant, but is not due to a flaw in PerWorldInventory's implementation.
            ItemStack[] profileInventory = new ItemStack[41];
            System.arraycopy(inventory.getStorageContents(), 0, profileInventory, 0, 36);
            System.arraycopy(inventory.getArmourContents(), 0, profileInventory, 36, 4);
            System.arraycopy(inventory.getOffHandContents(), 0, profileInventory, 40, 1);

            PlayerProfile updatedProfile = profile.copy(
                    profileArmour,
                    profile.getEnderChest(),
                    profileInventory,
                    profile.getAllowFlight(),
                    profile.getDisplayName(),
                    profile.getExhaustion(),
                    profile.getExperience(),
                    profile.isFlying(),
                    profile.getFoodLevel(),
                    profile.getMaxHealth(),
                    profile.getHealth(),
                    profile.getGameMode(),
                    profile.getLevel(),
                    profile.getSaturation(),
                    profile.getPotionEffects(),
                    profile.getFallDistance(),
                    profile.getFireTicks(),
                    profile.getMaximumAir(),
                    profile.getRemainingAir(),
                    profile.getBalance());
            pwiHook.getProfileCache().put(profileKey, updatedProfile);

            CompletableFuture<Void> saveTask = CompletableFuture.runAsync(() -> pwiHook.getDataSource().savePlayer(profileKey, updatedProfile), asyncExecutor);
            if (saveVanilla) saveTask = CompletableFuture.allOf(saveTask, wrapped.saveInventory(inventory));
            return saveTask;
        }
    }

    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, Mirror<EnderChestSlot> mirror, ProfileKey profileKey) {
        return createOfflineEnderChest(playerId, playerName, title, mirror, profileKey, pwiHook.isGroupManagedByPWI(profileKey.getGroup()));
    }

    private CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, Mirror<EnderChestSlot> mirror, ProfileKey profileKey, boolean tieToProfile) {
        //don't ask the cache because it may contain a live inventory! (and we could get called by asSnapshotInventory!)

        //check whether the player is not exempted.
        final Target target = Target.byUniqueId(playerId);
        if (profileKey.getGroup().getWorlds().stream().anyMatch(world -> exempt.isExemptedFromHavingEnderchestSpectated(target, world)))
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

        //try non-managed
        CompletableFuture<Optional<EnderSpectatorInventory>> nonPwiEnderSpectatorFuture = wrapped.createOfflineEnderChest(playerId, playerName, title, mirror);
        if (!pwiHook.pwiManagedEnderChests()) return nonPwiEnderSpectatorFuture.thenApply(opt -> SpectateResponse.fromOptional(opt, NotCreatedReason.implementationFault(target)));

        //create a fake player for PWI so that we can load data onto it!
        FakePlayer player = new FakePlayer(playerId, playerName, plugin.getServer());
        Inventory enderInv = player.getEnderChest();

        AtomicBoolean loadFromPWI = new AtomicBoolean(true);
        Location location = pwiHook.getDataSource().getLogout(player);
        World logoutWorld = location != null ? location.getWorld() : plugin.getServer().getWorlds().get(0);
        if (!pwiHook.pwiLoadDataOnJoin() && profileKey.getGroup().containsWorld(logoutWorld.getName())) {
            loadFromPWI.set(false); //just load from vanilla.
        }

        return nonPwiEnderSpectatorFuture.thenApplyAsync(optionalSpectatorInv -> {
            optionalSpectatorInv.ifPresent(spectatorInv -> {
                //first set the minecraft-saved contents onto the fake player
                enderInv.setStorageContents(spectatorInv.getStorageContents());

                if (loadFromPWI.get()) {
                    //load the data from the player onto the profile, or load the profile from persistent storage
                    PlayerProfile profile = pwiHook.getOrCreateProfile(player, profileKey);

                    //then set it back from the profile
                    spectatorInv.setStorageContents(profile.getEnderChest());
                }

                //mark inventory as tied to the profile key
                if (tieToProfile) {
                    enderchestKeys.put(spectatorInv, profileKey);
                    enderchests.put(profileKey, spectatorInv);
                }
            });

            return SpectateResponse.fromOptional(optionalSpectatorInv, NotCreatedReason.implementationFault(target));
        }, serverThreadExecutor);
    }

    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest, ProfileKey profileKey, boolean saveVanilla) {
        //if the spectated player is managed by PWI (because its world is managed by PWI)
        //then also save the inventory to PWI's storage
        //that can be done by loading the profile, applying the contents from the EnderSpectatorInventory and saving it again

        if (!pwiHook.pwiManagedEnderChests()) {
            //not managed by pwi
            return wrapped.saveEnderChest(enderChest);
        }

        else {
            FakePlayer fakePlayer = new FakePlayer(enderChest.getSpectatedPlayerId(), enderChest.getSpectatedPlayerName(), plugin.getServer());
            Inventory playerEC = fakePlayer.getEnderChest();

            playerEC.setStorageContents(enderChest.getStorageContents());
            PlayerProfile profile = pwiHook.getOrCreateProfile(fakePlayer, profileKey);

            ItemStack[] profileEnderChest = enderChest.getStorageContents();

            PlayerProfile updatedProfile = profile.copy(
                    profile.getArmor(),
                    profileEnderChest,
                    profile.getInventory(),
                    profile.getAllowFlight(),
                    profile.getDisplayName(),
                    profile.getExhaustion(),
                    profile.getExperience(),
                    profile.isFlying(),
                    profile.getFoodLevel(),
                    profile.getMaxHealth(),
                    profile.getHealth(),
                    profile.getGameMode(),
                    profile.getLevel(),
                    profile.getSaturation(),
                    profile.getPotionEffects(),
                    profile.getFallDistance(),
                    profile.getFireTicks(),
                    profile.getMaximumAir(),
                    profile.getRemainingAir(),
                    profile.getBalance());
            pwiHook.getProfileCache().put(profileKey, updatedProfile);

            CompletableFuture<Void> saveTask = CompletableFuture.runAsync(() -> pwiHook.getDataSource().savePlayer(profileKey, updatedProfile), asyncExecutor);
            if (saveVanilla) saveTask = CompletableFuture.allOf(saveTask, wrapped.saveEnderChest(enderChest));
            return saveTask;
        }
    }

    private <Slot, S extends SpectatorInventory<Slot>> Optional<S> asLiveInventory(S snapshotInventory, boolean transferToLiveInventory) {
        assert plugin.getServer().isPrimaryThread() : "can't call asLiveInventory asynchronously";

        Player player = plugin.getServer().getPlayer(snapshotInventory.getSpectatedPlayerId());
        if (player == null) return Optional.empty();

        String title = snapshotInventory.getTitle();

        S live = null;

        //TODO can I use ShallowCopy here? probably, yes!
        if (snapshotInventory instanceof MainSpectatorInventory) {
            Mirror<PlayerInventorySlot> mirror = (Mirror<PlayerInventorySlot>) snapshotInventory.getMirror();
            live = (S) spectateInventory(player, title, mirror);
        } else if (snapshotInventory instanceof EnderSpectatorInventory) {
            Mirror<EnderChestSlot> mirror = (Mirror<EnderChestSlot>) snapshotInventory.getMirror();
            live = (S) spectateEnderChest(player, title, mirror);
        } else {
            throw new RuntimeException("Unreachable");
        }

        if (transferToLiveInventory) {
            live.setContents(snapshotInventory.getContents());
        }

        return Optional.of(live);
    }

    private <S extends SpectatorInventory> CompletableFuture<Optional<S>> asSnapShotInventory(S liveSpectatorInventory) {
        assert plugin.getServer().isPrimaryThread() : "can't call asSnapShotInventory asynchronously";

        UUID id = liveSpectatorInventory.getSpectatedPlayerId();
        String name = liveSpectatorInventory.getSpectatedPlayerName();;
        String title = liveSpectatorInventory.getTitle();;

        Player player = plugin.getServer().getPlayer(id);

        //if the spectated player is offline, then the inventory wasn't live in the first place.
        if (player == null) {
            return CompletableFuture.completedFuture(Optional.of(liveSpectatorInventory));
        }

        //if live spectator inventory is bound to a profile key AND the player does not match that profile, then the inventory wasn't live in the first place.
        ProfileKey profileKey = inventoryKeys.get(liveSpectatorInventory);
        if (profileKey != null && !pwiHook.isMatchedByProfile(player, profileKey)) {
            return CompletableFuture.completedFuture(Optional.of(liveSpectatorInventory));
        }

        //either the inventory is tied to a profile and the player matches that profile OR the inventory is not tied to a profile.
        //in the second case we need to make up a new profile key on the fly
        if (profileKey == null) {
            profileKey = pwiHook.getActiveProfileKey(player);
        }

        //can't wait till pattern matching arrives
        if (liveSpectatorInventory instanceof MainSpectatorInventory) {
            MainSpectatorInventory liveSpectator = (MainSpectatorInventory) liveSpectatorInventory;
            Mirror<PlayerInventorySlot> mirror = liveSpectator.getMirror();

            //who needs type safety anyway?
            return (CompletableFuture<Optional<S>>) (Object) createOfflineInventory(id, name, title, mirror, profileKey).thenApplyAsync(Function.identity(), serverThreadExecutor);

        } else if (liveSpectatorInventory instanceof EnderSpectatorInventory) {
            EnderSpectatorInventory liveSpectator = (EnderSpectatorInventory) liveSpectatorInventory;
            Mirror<EnderChestSlot> mirror = liveSpectator.getMirror();

            //who needs type safety anyway?
            return (CompletableFuture<Optional<S>>) (Object) createOfflineEnderChest(id, name, title, mirror, profileKey).thenApplyAsync(Function.identity(), serverThreadExecutor);
        }

        //unreachable
        return CompletedEmpty.the();
    }

}
