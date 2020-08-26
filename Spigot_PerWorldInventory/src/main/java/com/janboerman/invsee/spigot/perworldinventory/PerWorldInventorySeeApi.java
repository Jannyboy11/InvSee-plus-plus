package com.janboerman.invsee.spigot.perworldinventory;

import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.SpectatorInventory;
import me.ebonjaeger.perworldinventory.data.PlayerProfile;
import me.ebonjaeger.perworldinventory.data.ProfileKey;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PerWorldInventorySeeApi extends InvseeAPI {

    private final InvseeAPI wrapped;
    private final PerWorldInventoryHook pwiHook;

    private final Map<SpectatorInventory, Set<String>> inventoryWorlds = Collections.synchronizedMap(new WeakHashMap<>());

    public PerWorldInventorySeeApi(Plugin plugin, InvseeAPI wrapped, PerWorldInventoryHook pwiHook) {
        super(plugin);
        this.wrapped = Objects.requireNonNull(wrapped);
        this.pwiHook = Objects.requireNonNull(pwiHook);
        plugin.getServer().getPluginManager().registerEvents(new EventListener(), plugin);

        wrapped.setMainInventoryTransferPredicate((spectatorInventory, player) -> {
            if (!pwiHook.pwiManagedInventories()) return true;

            // a player logs in and his inventory was being edited by somebody.
            // do we transfer the contents from the spectator to the live player?
            // only if the inventories share the same group!

            Set<String> worlds = inventoryWorlds.get(spectatorInventory);
            if (worlds == null) {
                //inventory not managed by pwi
                if (pwiHook.isWorldManagedByPWI(player.getWorld().getName())) {
                    //player world is managed, inventory is not
                    //don't transfer
                    return false;
                } else {
                    //both are managed
                    return pwiHook.pwiUnmanagedWorldsSameGroup();
                }
            }

            //if worlds share the same inventory, then we should transfer
            return worlds.stream().anyMatch(w -> pwiHook.getPerWorldInventoryAPI().canWorldsShare(player.getWorld().getName(), w));
        });
        wrapped.setEnderChestTransferPredicate((spectatorInventory, player) -> {
            if (!pwiHook.pwiManagedEnderChests()) return true;

            // a player logs in and his enderchest was being edited by somebody.
            // do we transfer the contents from the spectator to the live player?
            // only if the enderchests share the same group!

            Set<String> worlds = inventoryWorlds.get(spectatorInventory);
            if (worlds == null) {
                //enderchest not managed by pwi
                if (pwiHook.isWorldManagedByPWI(player.getWorld().getName())) {
                    //player world is managed, enderchest is not
                    //don't transfer
                    return false;
                } else {
                    //both are managed
                    return pwiHook.pwiUnmanagedWorldsSameGroup();
                }
            }

            //if worlds share the same enderchest, then we should transfer
            return worlds.stream().anyMatch(w -> pwiHook.getPerWorldInventoryAPI().canWorldsShare(player.getWorld().getName(), w));
        });
    }

    public PerWorldInventoryHook getHook() {
        return pwiHook;
    }

    private final class EventListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onWorldChange(PlayerChangedWorldEvent event) {
            //if the player was being spectated, then update inventory world map
            Player player = event.getPlayer();

            String from = event.getFrom().getName();
            String to = player.getWorld().getName();

            //TODO decide whether to change the spectator inventory contents,
            //TODO or to update the world set this inventory is association with.
            getOpenMainSpectatorInventory(player.getUniqueId()).ifPresent(mainSpec -> {
                Set<String> boundTo = inventoryWorlds.get(mainSpec);
                if (boundTo != null) {
                    // inventory marked for a set of worlds

                    if (boundTo.contains(from) && !boundTo.contains(to)) {
                        //spectator inventory was bound to the world the player left
                        //set the contents to the profile of the to world


                    } else if (!boundTo.contains(from) && boundTo.contains(to))) {
                        //spectator inventory was bound to the world the player joined

                    }
                } //else: not bound to any world
            });

            //TODO EnderSpectatorInventory
        }
    }

    public MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        var result = wrapped.spectateInventory(player, title);
        //mark inv as managed by pwi
        inventoryWorlds.computeIfAbsent(result, r -> new HashSet<>()).add(player.getWorld().getName());
        return result;
    }

    @Override
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(playerId, playerName, plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        ProfileKey profileKey = new ProfileKey(playerId, pwiHook.getGroupForWorld(world.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        var result = createOfflineInventory(playerId, playerName, title, profileKey);
        //mark inv as managed by pwi
        result.thenAccept(optInv -> optInv.ifPresent(inv -> inventoryWorlds.computeIfAbsent(inv, r -> new HashSet<>()).add(world.getName())));
        return result;
    }

    @Override
    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory) {
        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(inventory.getSpectatedPlayerId(), inventory.getSpectatedPlayerName(), plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        ProfileKey profileKey = new ProfileKey(inventory.getSpectatedPlayerId(), pwiHook.getGroupForWorld(world.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        return saveInventory(inventory, profileKey);
    }

    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        var result = wrapped.spectateEnderChest(player, title);
        //mark inv as managed by pwi
        inventoryWorlds.computeIfAbsent(result, r -> new HashSet<>()).add(player.getWorld().getName());
        return result;
    }

    @Override
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title) {
        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(playerId, playerName, plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        ProfileKey profileKey = new ProfileKey(playerId, pwiHook.getGroupForWorld(world.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        var result = createOfflineEnderChest(playerId, playerName, title, profileKey);
        result.thenAccept(optInv -> optInv.ifPresent(inv -> inventoryWorlds.computeIfAbsent(inv, r -> new HashSet<>()).add(world.getName())));
        return result;
    }

    @Override
    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest) {
        Location logoutLocation = pwiHook.getDataSource().getLogout(new FakePlayer(enderChest.getSpectatedPlayerId(), enderChest.getSpectatedPlayerName(), plugin.getServer()));
        World world = logoutLocation != null ? logoutLocation.getWorld() : plugin.getServer().getWorlds().get(0);
        ProfileKey profileKey = new ProfileKey(enderChest.getSpectatedPlayerId(), pwiHook.getGroupForWorld(world.getName()), GameMode.SURVIVAL /*I don't really care about creative, do I?*/);
        return saveEnderChest(enderChest, profileKey);
    }

    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, ProfileKey profileKey) {
        CompletableFuture<Optional<MainSpectatorInventory>> nmsInvSpectator = wrapped.createOfflineInventory(playerId, playerName, title);
        if (!pwiHook.pwiManagedInventories()) return nmsInvSpectator;

        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null) player = new FakePlayer(playerId, playerName, plugin.getServer());
        PlayerInventory playerInv = player.getInventory();

        final Player finalPlayer = player;
        return nmsInvSpectator.thenApplyAsync(optionalSpectatorInv -> {
            optionalSpectatorInv.ifPresent(spectatorInv -> {
                //first set the minecraft-saved contents onto the player
                playerInv.setStorageContents(spectatorInv.getStorageContents());
                playerInv.setArmorContents(spectatorInv.getArmourContents());
                playerInv.setExtraContents(spectatorInv.getOffHandContents());
                finalPlayer.setItemOnCursor(spectatorInv.getCursorContents());

                PlayerProfile profile = pwiHook.getOrCreateProfile(finalPlayer, profileKey);
                if (!(finalPlayer instanceof FakePlayer) && finalPlayer.isOnline() && profileKey.getGroup().getWorlds().contains(finalPlayer.getWorld().getName())) {
                    //real online player - if the player is in a world that is part of the group, then set the profile contents
                    profile = profile.copy(
                            finalPlayer.getInventory().getArmorContents(),
                            finalPlayer.getEnderChest().getContents(),
                            finalPlayer.getInventory().getContents(),
                            finalPlayer.getAllowFlight(),
                            finalPlayer.getDisplayName(),
                            finalPlayer.getExhaustion(),
                            finalPlayer.getExp(),
                            finalPlayer.isFlying(),
                            finalPlayer.getFoodLevel(),
                            finalPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
                            finalPlayer.getHealth(),
                            finalPlayer.getGameMode(),
                            finalPlayer.getLevel(),
                            finalPlayer.getSaturation(),
                            finalPlayer.getActivePotionEffects(),
                            finalPlayer.getFallDistance(),
                            finalPlayer.getFireTicks(),
                            finalPlayer.getMaximumAir(),
                            finalPlayer.getRemainingAir(),
                            pwiHook.getEconomyService().getBalance(finalPlayer));
                    pwiHook.getProfileCache().put(profileKey, profile);
                }

                //then set it back from the profile
                spectatorInv.setStorageContents(Arrays.copyOf(profile.getInventory(), 36));
                spectatorInv.setArmourContents(Arrays.copyOfRange(profile.getInventory(), 36, 40));
                spectatorInv.setOffHandContents(Arrays.copyOfRange(profile.getInventory(), 40, 41));

                //mark inv as managed by pwi
                inventoryWorlds.put(spectatorInv, profileKey.getGroup().getWorlds());
            });

            return optionalSpectatorInv;
        }, serverThreadExecutor);
    }

    public CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory, ProfileKey profileKey) {
        //if the spectated player is managed by PWI (because its world is managed by PWI)
        //then also save the inventory to PWI's storage
        //that can be done by loading the profile, applying the contents from the MainSpectatorInventory and saving it again

        if (!pwiHook.pwiManagedInventories() || !inventoryWorlds.containsKey(inventory)) {
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

            return CompletableFuture.runAsync(() -> {
                pwiHook.getDataSource().savePlayer(profileKey, updatedProfile);
            }, asyncExecutor);
        }
    }

    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, ProfileKey profileKey) {
        CompletableFuture<Optional<EnderSpectatorInventory>> nmsInvSpectator = wrapped.createOfflineEnderChest(playerId, playerName, title);
        if (!pwiHook.pwiManagedEnderChests()) return nmsInvSpectator;

        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null) player = new FakePlayer(playerId, playerName, plugin.getServer());
        Inventory enderInv = player.getEnderChest();

        final Player finalPlayer = player;
        return nmsInvSpectator.thenApplyAsync(optionalSpectatorInv -> {
            optionalSpectatorInv.ifPresent(spectatorInv -> {
                //first set the minecraft-saved contents onto the player
                enderInv.setStorageContents(spectatorInv.getStorageContents());

                PlayerProfile profile = pwiHook.getOrCreateProfile(finalPlayer, profileKey);
                if (!(finalPlayer instanceof FakePlayer) && finalPlayer.isOnline() && profileKey.getGroup().getWorlds().contains(finalPlayer.getWorld().getName())) {
                    //real online player - if the player is in a world that is part of the group, then set the profile contents
                    profile = profile.copy(
                            finalPlayer.getInventory().getArmorContents(),
                            finalPlayer.getEnderChest().getContents(),
                            finalPlayer.getInventory().getContents(),
                            finalPlayer.getAllowFlight(),
                            finalPlayer.getDisplayName(),
                            finalPlayer.getExhaustion(),
                            finalPlayer.getExp(),
                            finalPlayer.isFlying(),
                            finalPlayer.getFoodLevel(),
                            finalPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
                            finalPlayer.getHealth(),
                            finalPlayer.getGameMode(),
                            finalPlayer.getLevel(),
                            finalPlayer.getSaturation(),
                            finalPlayer.getActivePotionEffects(),
                            finalPlayer.getFallDistance(),
                            finalPlayer.getFireTicks(),
                            finalPlayer.getMaximumAir(),
                            finalPlayer.getRemainingAir(),
                            pwiHook.getEconomyService().getBalance(finalPlayer));
                    pwiHook.getProfileCache().put(profileKey, profile);
                }

                //then set it back from the profile
                spectatorInv.setStorageContents(profile.getEnderChest());

                //mark inv as managed by pwi
                inventoryWorlds.put(spectatorInv, profileKey.getGroup().getWorlds());
            });

            return optionalSpectatorInv;
        }, serverThreadExecutor);
    }

    public CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest, ProfileKey profileKey) {
        //if the spectated player is managed by PWI (because its world is managed by PWI)
        //then also save the inventory to PWI's storage
        //that can be done by loading the profile, applying the contents from the EnderSpectatorInventory and saving it again

        if (!pwiHook.pwiManagedEnderChests() || !inventoryWorlds.containsKey(enderChest)) {
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

            return CompletableFuture.runAsync(() -> pwiHook.getDataSource().savePlayer(profileKey, updatedProfile), asyncExecutor);
        }
    }

}
