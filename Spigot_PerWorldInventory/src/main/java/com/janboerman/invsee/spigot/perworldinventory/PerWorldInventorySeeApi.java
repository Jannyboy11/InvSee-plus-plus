package com.janboerman.invsee.spigot.perworldinventory;

import com.google.common.collect.BiMap;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import com.janboerman.invsee.utils.WeakBiMap;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.data.PlayerProfile;
import me.ebonjaeger.perworldinventory.data.ProfileKey;
import me.ebonjaeger.perworldinventory.event.InventoryLoadCompleteEvent;
import me.ebonjaeger.perworldinventory.event.InventoryLoadEvent;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PerWorldInventorySeeApi extends InvseeAPI {

    private final InvseeAPI wrapped;
    private final PerWorldInventoryHook pwiHook;

    //there can be more than one open spectator inventories per target player.
    //use the superclass openInventory-mechanic only for profile-unspecific spectator inventories
    private final WeakBiMap<ProfileKey, MainSpectatorInventory> inventories = new WeakBiMap<>();
    private final WeakBiMap<ProfileKey, MainSpectatorInventory>.Reversed inventoryKeys = inventories.swap();
    private final WeakBiMap<ProfileKey, EnderSpectatorInventory> enderchests = new WeakBiMap<>();
    private final WeakBiMap<ProfileKey, EnderSpectatorInventory>.Reversed enderchestKeys = enderchests.swap();

    private final PwiEventListener pwiEventListener = new PwiEventListener();
    private final TiedInventoryListener tiedInventoryListener = new TiedInventoryListener();
    private final TiedPlayerListener tiedPlayerListener = new TiedPlayerListener();

    public PerWorldInventorySeeApi(Plugin plugin, InvseeAPI wrapped, PerWorldInventoryHook pwiHook) {
        super(plugin);
        this.wrapped = Objects.requireNonNull(wrapped);
        this.pwiHook = Objects.requireNonNull(pwiHook);

        wrapped.unregsiterListeners();
        setOpenInventories(wrapped.getOpenInventories());
        setOpenEnderChests(wrapped.getOpenEnderChests());

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

            ProfileKey profileKey = inventoryKeys.get(spectatorInventory);
            if (profileKey == null) return true; //not tied to a profile, so just transfer

            //check whether world and gamemode match
            return pwiHook.isMatchedByProfile(player, profileKey);
        });
    }

    public void registerListeners() {
        super.registerListeners();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(pwiEventListener, plugin);
        pluginManager.registerEvents(tiedInventoryListener, plugin);
        pluginManager.registerEvents(tiedPlayerListener, plugin);
    }

    public void unregsiterListeners() {
        super.unregsiterListeners();
        HandlerList.unregisterAll(pwiEventListener);
        HandlerList.unregisterAll(tiedInventoryListener);
        HandlerList.unregisterAll(tiedPlayerListener);
    }

    public PerWorldInventoryHook getHook() {
        return pwiHook;
    }

    private final class TiedPlayerListener implements Listener {

//        @EventHandler
//        public void onTargetJoin(PlayerJoinEvent event) {
//            if (pwiHook.pwiLoadDataOnJoin()) return; //InventoryLoadCompleteEvent will be called.
//
//            //super class player listener does resetting of live open inventories. and it uses our custom predicate
//            //do I need to do anything here?
//            //I don't think there is anything.
//        }

        @EventHandler
        public void onTargetQuit(PlayerQuitEvent event) {
            //remove from maps if nobody is watching.

            Player player = event.getPlayer();
            ProfileKey key = pwiHook.getActiveProfileKey(player);

            inventories.compute(key, (k, mainSpectatorInventory) -> {
                if (mainSpectatorInventory == null || mainSpectatorInventory.getViewers().isEmpty()) return null;   //remove if no longer being spectated
                return mainSpectatorInventory;
            });
            enderchests.compute(key, (k, enderSpectatorInventory) -> {
                if (enderSpectatorInventory == null || enderSpectatorInventory.getViewers().isEmpty()) return null; //remove if no longer being spectated
                return enderSpectatorInventory;
            });
        }
    }

    private final class TiedInventoryListener implements Listener {

        @EventHandler
        public void onTargetInventoryOpen(InventoryOpenEvent event) {
            HumanEntity player = event.getPlayer();

            ProfileKey activeProfileKey = pwiHook.getActiveProfileKey(player);
            MainSpectatorInventory mainSpectator = inventories.get(activeProfileKey);
            if (mainSpectator != null) {
                mainSpectator.watch(event.getView());
            }

            wrapped.getOpenMainSpectatorInventory(player.getUniqueId()).ifPresent(spectator -> spectator.watch(event.getView()));
        }

        @EventHandler
        public void onTargetInventoryClose(InventoryCloseEvent event) {
            HumanEntity player = event.getPlayer();

            ProfileKey activeProfileKey = pwiHook.getActiveProfileKey(player);
            MainSpectatorInventory mainSpectator = inventories.get(activeProfileKey);
            if (mainSpectator != null) {
                mainSpectator.unwatch();
            }

            wrapped.getOpenMainSpectatorInventory(player.getUniqueId()).ifPresent(spectator -> spectator.unwatch());
        }
    }

    //TODO test this
    private final class PwiEventListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPwiLoad(InventoryLoadEvent event) {
            //new inventory contents is about to be loaded onto the player.

            //  if the player was being spectated, then
            //      if the spectator inventory is tied to a profile key, then
            //          close the spectator inventory for all viewers
            //          re-open a fresh offline spectator inventory, tied to the same profile key

            Player player = event.getPlayer();

            //there can be more than one open spectator inventories per target player.
            //use the superclass openInventory-mechanic only for profile-unspecific spectator inventories
            //that's okay, because this event isn't even fired in that case.

            ProfileKey oldProfileKey = pwiHook.getActiveProfileKey(player);
            //ProfileKey newProfileKey = new ProfileKey(player.getUniqueId(), event.getGroup(), event.getNewGameMode());

            boolean wasInventoryLiveSpectated = inventories.containsKey(oldProfileKey);
            boolean wasEnderChestLiveSpectated = enderchests.containsKey(oldProfileKey);

            if (wasInventoryLiveSpectated) {
                MainSpectatorInventory spectatorInventory = inventories.get(oldProfileKey);

                List<HumanEntity> viewers = new ArrayList<>(spectatorInventory.getViewers());   //copy
                ItemStack[] contents = spectatorInventory.getContents();                        //already is a copy
                viewers.forEach(HumanEntity::closeInventory);

                CompletableFuture<Optional<MainSpectatorInventory>> snapshotFuture = asSnapShotInventory(spectatorInventory);
                snapshotFuture.thenAccept(optional -> optional.ifPresentOrElse(newSpectatorInventory -> {
                    inventories.put(oldProfileKey, newSpectatorInventory);
                    newSpectatorInventory.setContents(contents);
                    viewers.forEach(v -> v.openInventory(newSpectatorInventory));
                }, /*orElse part*/ () -> inventories.remove(oldProfileKey)));
            }

            if (wasEnderChestLiveSpectated) {
                EnderSpectatorInventory spectatorInventory = enderchests.get(oldProfileKey);

                List<HumanEntity> viewers = new ArrayList<>(spectatorInventory.getViewers());   //copy
                ItemStack[] contents = spectatorInventory.getContents();                        //already is a copy

                CompletableFuture<Optional<EnderSpectatorInventory>> snapshotFuture = asSnapShotInventory(spectatorInventory);
                snapshotFuture.thenAccept(optional -> optional.ifPresentOrElse(newSpectatorInventory -> {
                    enderchests.put(oldProfileKey, newSpectatorInventory);
                    newSpectatorInventory.setContents(contents);
                    viewers.forEach(v -> v.openInventory(newSpectatorInventory));
                }, /*orElse part*/ () -> enderchests.remove(oldProfileKey)));
            }
        }

        @EventHandler
        public void onPwiLoadComplete(InventoryLoadCompleteEvent event) {
            //new inventory contents was loaded onto the player.

            //  if there is is an open spectator inventory for the new profile, then
            //      close the spectator inventory for all viewers
            //      re-open a live spectator inventory, tied to the same profile key

            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            ProfileKey newProfileKey = new ProfileKey(playerId, event.getGroup(), event.getGameMode());

            boolean inventorySpectated = inventories.containsKey(newProfileKey);
            boolean enderchestSpectated = enderchests.containsKey(newProfileKey);

            if (inventorySpectated) {
                MainSpectatorInventory snapshotSpectator = inventories.get(newProfileKey);

                List<HumanEntity> viewers = new ArrayList<>(snapshotSpectator.getViewers());    //copy
                ItemStack[] contents = snapshotSpectator.getContents();                         //already is a copy
                viewers.forEach(HumanEntity::closeInventory);

                Optional<MainSpectatorInventory> liveFuture = asLiveInventory(snapshotSpectator, false);
                liveFuture.ifPresentOrElse(liveSpectator -> {
                    inventories.put(newProfileKey, liveSpectator);
                    liveSpectator.setContents(contents);    //updates the player's inventory!
                    viewers.forEach(v -> v.openInventory(liveSpectator));
                }, /*orElse part*/ () -> inventories.remove(newProfileKey));
            }

            if (enderchestSpectated) {
                EnderSpectatorInventory snapshotSpectator = enderchests.get(newProfileKey);

                List<HumanEntity> viewers = new ArrayList<>(snapshotSpectator.getViewers());    //copy
                ItemStack[] contents = snapshotSpectator.getContents();                         //already is a copy
                viewers.forEach(HumanEntity::closeInventory);

                Optional<EnderSpectatorInventory> liveFuture = asLiveInventory(snapshotSpectator, false);
                liveFuture.ifPresentOrElse(liveSpectator -> {
                    enderchests.put(newProfileKey, liveSpectator);
                    liveSpectator.setContents(contents);
                    viewers.forEach(v -> v.openInventory(liveSpectator));
                }, /*orElse part*/ () -> inventories.remove(newProfileKey));
            }
        }
    }

    @Override
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        return wrapped.spectateInventory(player, title);
    }

    public MainSpectatorInventory spectateInventory(HumanEntity player, String title, ProfileKey profileKey) {
        MainSpectatorInventory spectatorInv = spectateInventory(player, title);
        inventories.put(profileKey, spectatorInv);
        return spectatorInv;
    }

    @Override
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(playerId, playerName, plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        Group group = pwiHook.getGroupForWorld(world.getName());
        ProfileKey profileKey = new ProfileKey(playerId, group, GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        return createOfflineInventory(playerId, playerName, title, profileKey, false);
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory) {
        ProfileKey profileKey = inventoryKeys.get(inventory);
        CompletableFuture<Void> wrappedFuture = null;
        if (profileKey == null) {
            //spectator inventory is not tied - just use the player's last location
            Location location = null;
            Player target = plugin.getServer().getPlayer(inventory.getSpectatedPlayerId());
            if (target != null) location = target.getLocation();
            if (location == null) location = pwiHook.getDataSource().getLogout(new FakePlayer(inventory.getSpectatedPlayerId(), inventory.getSpectatedPlayerName(), plugin.getServer()));
            World world = location != null ? location.getWorld() : plugin.getServer().getWorlds().get(0);
            profileKey = new ProfileKey(inventory.getSpectatedPlayerId(), pwiHook.getGroupForWorld(world.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);

            wrappedFuture = wrapped.saveInventory(inventory);   //shouldn't I be doing this anyway? technically only necessary for un-tied inventories.
        }
        
        CompletableFuture<Void> pwiFuture = saveInventory(inventory, profileKey);
        if (wrappedFuture != null) {
            return CompletableFuture.allOf(pwiFuture, wrappedFuture);
        } else {
            return pwiFuture;
        }
    }

    @Override
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        return wrapped.spectateEnderChest(player, title);
    }

    public EnderSpectatorInventory spectateEnderChests(HumanEntity player, String title, ProfileKey profileKey) {
        EnderSpectatorInventory spectatorInv = spectateEnderChest(player, title);
        enderchests.put(profileKey, spectatorInv);
        return spectatorInv;
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title) {
        Optional<EnderSpectatorInventory> cached = wrapped.getOpenEnderSpectatorInventory(playerId);
        if (cached.isPresent()) return CompletableFuture.completedFuture(cached);

        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(playerId, playerName, plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        Group group = pwiHook.getGroupForWorld(world.getName());
        ProfileKey profileKey = new ProfileKey(playerId, group, GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        return createOfflineEnderChest(playerId, playerName, title, profileKey, false);
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest) {
        ProfileKey profileKey = inventoryKeys.get(enderChest);
        CompletableFuture<Void> wrappedFuture = null;
        if (profileKey == null) {
            Location location = null;
            Player player = plugin.getServer().getPlayer(enderChest.getSpectatedPlayerId());
            if (player != null) location = player.getLocation();
            if (location == null) location = pwiHook.getDataSource().getLogout(new FakePlayer(enderChest.getSpectatedPlayerId(), enderChest.getSpectatedPlayerName(), plugin.getServer()));
            World world = location != null ? location.getWorld() : plugin.getServer().getWorlds().get(0);
            profileKey = new ProfileKey(enderChest.getSpectatedPlayerId(), pwiHook.getGroupForWorld(world.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);

            wrappedFuture = wrapped.saveEnderChest(enderChest);   //shouldn't I be doing this anyway? technically only necessary for un-tied inventories.
        }

        CompletableFuture<Void> pwiFuture = saveEnderChest(enderChest, profileKey);
        if (wrappedFuture != null) {
            return CompletableFuture.allOf(pwiFuture, wrappedFuture);
        } else {
            return pwiFuture;
        }
    }

    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, ProfileKey profileKey) {
        return createOfflineInventory(playerId, playerName, title, profileKey, pwiHook.isGroupManagedByPWI(profileKey.getGroup()));
    }

    private CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, ProfileKey profileKey, boolean tieToProfile) {
        //try cache
        MainSpectatorInventory cached = inventories.get(profileKey);
        if (cached != null) return CompletableFuture.completedFuture(Optional.of(cached));

        //try non-managed
        CompletableFuture<Optional<MainSpectatorInventory>> nonPwiMainSpectatorFuture = wrapped.createOfflineInventory(playerId, playerName, title);
        if (!pwiHook.pwiManagedInventories()) return nonPwiMainSpectatorFuture;

        //create a fake player for PWI so that we can load data onto it!
        FakePlayer player = new FakePlayer(playerId, playerName, plugin.getServer());
        PlayerInventory playerInv = player.getInventory();

        return nonPwiMainSpectatorFuture.thenApplyAsync(optionalSpectatorInv -> {
            optionalSpectatorInv.ifPresent(spectatorInv -> {
                //first set the minecraft-saved contents onto the player
                playerInv.setStorageContents(spectatorInv.getStorageContents());
                playerInv.setArmorContents(spectatorInv.getArmourContents());
                playerInv.setExtraContents(spectatorInv.getOffHandContents());
                player.setItemOnCursor(spectatorInv.getCursorContents());

                //load the data from the player onto the profile, or load the profile from persistent storage
                PlayerProfile profile = pwiHook.getOrCreateProfile(player, profileKey);

                //then set it back from the profile
                spectatorInv.setStorageContents(Arrays.copyOf(profile.getInventory(), 36));
                spectatorInv.setArmourContents(Arrays.copyOfRange(profile.getInventory(), 36, 40));
                spectatorInv.setOffHandContents(Arrays.copyOfRange(profile.getInventory(), 40, 41));
                //PlayerProfile has no getter for the item on the cursor!

                //mark inventory as tied to the profile key
                if (tieToProfile) {
                    inventoryKeys.put(spectatorInv, profileKey);
                }
            });

            return optionalSpectatorInv;
        }, serverThreadExecutor);
    }

    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory, ProfileKey profileKey) {
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

            return CompletableFuture.runAsync(() -> {
                pwiHook.getDataSource().savePlayer(profileKey, updatedProfile);
            }, asyncExecutor);
        }
    }

    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, ProfileKey profileKey) {
        return createOfflineEnderChest(playerId, playerName, title, profileKey, pwiHook.isGroupManagedByPWI(profileKey.getGroup()));
    }

    private CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, ProfileKey profileKey, boolean tieToProfile) {
        //try cache
        EnderSpectatorInventory cached = enderchests.get(profileKey);
        if (cached != null) return CompletableFuture.completedFuture(Optional.of(cached));

        //try non-managed
        CompletableFuture<Optional<EnderSpectatorInventory>> nonPwiEnderSpectatorFuture = wrapped.createOfflineEnderChest(playerId, playerName, title);
        if (!pwiHook.pwiManagedEnderChests()) return nonPwiEnderSpectatorFuture;

        //create a fake player for PWI so that we can load data onto it!
        FakePlayer player = new FakePlayer(playerId, playerName, plugin.getServer());
        Inventory enderInv = player.getEnderChest();

        return nonPwiEnderSpectatorFuture.thenApplyAsync(optionalSpectatorInv -> {
            optionalSpectatorInv.ifPresent(spectatorInv -> {
                //first set the minecraft-saved contents onto the fake player
                enderInv.setStorageContents(spectatorInv.getStorageContents());

                //load the data from the player onto the profile, or load the profile from persistent storage
                PlayerProfile profile = pwiHook.getOrCreateProfile(player, profileKey);

                //then set it back from the profile
                spectatorInv.setStorageContents(profile.getEnderChest());

                //mark inventory as tied to the profile key
                if (tieToProfile) {
                    enderchestKeys.put(spectatorInv, profileKey);
                }
            });

            return optionalSpectatorInv;
        }, serverThreadExecutor);
    }

    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest, ProfileKey profileKey) {
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

            return CompletableFuture.runAsync(() -> pwiHook.getDataSource().savePlayer(profileKey, updatedProfile), asyncExecutor);
        }
    }

    private <S extends SpectatorInventory> Optional<S> asLiveInventory(S snapshotInventory, boolean transferToLiveInventory) {
        assert plugin.getServer().isPrimaryThread() : "can't call asLiveInventory asynchronously";

        Player player = plugin.getServer().getPlayer(snapshotInventory.getSpectatedPlayerId());
        if (player == null) return Optional.empty();

        String title = snapshotInventory.getTitle();

        S live = null;

        //can't wait till pattern matching arrives
        if (snapshotInventory instanceof MainSpectatorInventory) {
            live = (S) spectateInventory(player, title);
        } else if (snapshotInventory instanceof EnderSpectatorInventory) {
            live = (S) spectateEnderChest(player, title);
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
        if (profileKey != null && !pwiHook.isMatchedByProfile(player, inventoryKeys.get(liveSpectatorInventory))) {
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

            //who needs type safety anyway?
            return (CompletableFuture<Optional<S>>) (Object) createOfflineInventory(id, name, title, profileKey).thenApplyAsync(Function.identity(), serverThreadExecutor);

        } else if (liveSpectatorInventory instanceof EnderSpectatorInventory) {
            EnderSpectatorInventory liveSpectator = (EnderSpectatorInventory) liveSpectatorInventory;

            //who needs type safety anyway?
            return (CompletableFuture<Optional<S>>) (Object) createOfflineEnderChest(id, name, title, profileKey).thenApplyAsync(Function.identity(), serverThreadExecutor);
        }

        //unreachable
        return CompletableFuture.completedFuture(Optional.empty());
    }

}
