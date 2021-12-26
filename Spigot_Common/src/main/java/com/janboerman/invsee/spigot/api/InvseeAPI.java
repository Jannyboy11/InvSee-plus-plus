package com.janboerman.invsee.spigot.api;

import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.Level;

import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.utils.*;
import com.janboerman.invsee.spigot.internal.MappingsVersion;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.*;

public abstract class InvseeAPI {

    protected final Plugin plugin;
    protected final NamesAndUUIDs lookup;
    protected final Exempt exempt;

    private Map<UUID, WeakReference<MainSpectatorInventory>> openInventories = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<String, CompletableFuture<SpectateResponse<MainSpectatorInventory>>> pendingInventoriesByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<SpectateResponse<MainSpectatorInventory>>> pendingInventoriesByUuid = new ConcurrentHashMap<>();

    private Map<UUID, WeakReference<EnderSpectatorInventory>> openEnderChests = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<String, CompletableFuture<SpectateResponse<EnderSpectatorInventory>>> pendingEnderChestsByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<SpectateResponse<EnderSpectatorInventory>>> pendingEnderChestsByUuid = new ConcurrentHashMap<>();

    private Function<Player, String> mainSpectatorInvTitleProvider = player -> spectateInventoryTitle(player.getName());
    private Function<Player, String> enderSpectatorInvTitleProvider = player -> spectateEnderchestTitle(player.getName());

    private BiPredicate<MainSpectatorInventory, Player> transferInvToLivePlayer = (spectatorInv, player) -> true;
    private BiPredicate<EnderSpectatorInventory, Player> transferEnderToLivePlayer = (spectatorInv, player) -> true;

    public final Executor serverThreadExecutor;
    public final Executor asyncExecutor;

    protected final PlayerListener playerListener = new PlayerListener();
    protected final InventoryListener inventoryListener = new InventoryListener();

    protected InvseeAPI(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);

        this.serverThreadExecutor = runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable);
        this.asyncExecutor = runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);

        this.lookup = new NamesAndUUIDs(plugin);
        this.exempt = new Exempt(plugin.getServer());

        registerListeners();
    }

    public Map<String, UUID> getUuidCache() {
        return lookup.getUuidCache();
    }

    public Map<UUID, String> getUserNameCache() {
        return lookup.getUserNameCache();
    }

    public String spectateInventoryTitle(String targetPlayerName) {
        return targetPlayerName + "'s inventory";
    }

    public String spectateEnderchestTitle(String targetPlayerName) {
        return targetPlayerName + "'s enderchest";
    }

    //TODO un-hack this registerListeners thing.
    //TODO should I make a protected constructor that takes a delegate InvseeAPI as a parameter?

    public void registerListeners() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(playerListener, plugin);
        pluginManager.registerEvents(inventoryListener, plugin);
    }

    public void unregsiterListeners() {
        HandlerList.unregisterAll(playerListener);
        HandlerList.unregisterAll(inventoryListener);
    }

    public final void setMainInventoryTitleFactory(Function<Player, String> titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.mainSpectatorInvTitleProvider = titleFactory;
    }

    public final void setEnderInventoryTitleFactory(Function<Player, String> titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.enderSpectatorInvTitleProvider = titleFactory;
    }

    public final void setMainInventoryTransferPredicate(BiPredicate<MainSpectatorInventory, Player> bip) {
        Objects.requireNonNull(bip);
        this.transferInvToLivePlayer = bip;
    }

    public final void setEnderChestTransferPredicate(BiPredicate<EnderSpectatorInventory, Player> bip) {
        Objects.requireNonNull(bip);
        this.transferEnderToLivePlayer = bip;
    }

    public Map<UUID, WeakReference<MainSpectatorInventory>> getOpenInventories() {
        return openInventories;
    }

    public Map<UUID, WeakReference<EnderSpectatorInventory>> getOpenEnderChests() {
        return openEnderChests;
    }

    protected void setOpenInventories(Map<UUID, WeakReference<MainSpectatorInventory>> openInventories) {
        this.openInventories = openInventories;
    }

    protected void setOpenEnderChests(Map<UUID, WeakReference<EnderSpectatorInventory>> openEnderChests) {
        this.openEnderChests = openEnderChests;
    }


    public static InvseeAPI setup(Plugin plugin) {
        final Server server = plugin.getServer();

        try {
            Constructor<?> ctor = null;

            if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_12_R1.CraftServer")) {
                ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_12_R1.InvseeImpl").getConstructor(Plugin.class);
            } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_15_R1.CraftServer")) {
                ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_15_R1.InvseeImpl").getConstructor(Plugin.class);
            } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_16_R3.CraftServer")) {
                ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_16_R3.InvseeImpl").getConstructor(Plugin.class);
            } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_17_R1.CraftServer")) {
                switch (MappingsVersion.getMappingsVersion(server)) {
                    case MappingsVersion._1_17:     ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_17_R1.InvseeImpl").getConstructor(Plugin.class);      break;
                    case MappingsVersion._1_17_1:   ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_17_1_R1.InvseeImpl").getConstructor(Plugin.class);    break;
                }
            } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_18_R1.CraftServer")) {
                switch (MappingsVersion.getMappingsVersion(server)) {
                    case MappingsVersion._1_18:     ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_18_R1.InvseeImpl").getConstructor(Plugin.class);      break;
                    case MappingsVersion._1_18_1:   ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_18_1_R1.InvseeImpl").getConstructor(Plugin.class);    break;
                }
            }
            //make a bunch of else-ifs here for future minecraft versions.
            if (ctor != null) {
                return InvseeAPI.class.cast(ctor.newInstance(plugin));
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("InvseeAPI implementation class needs a public constructor that accepts just one argument; the bukkit Plugin instance.", e);
        } catch (ClassNotFoundException cnfe) {
            //should not occur, this is our own class.
            throw new RuntimeException(cnfe);
        }

        throw new RuntimeException("Unsupported server software. Please run on (a fork of) CraftBukkit.");
    }

    public Optional<MainSpectatorInventory> getOpenMainSpectatorInventory(UUID player) {
        return Optional.ofNullable(openInventories.get(player))
                .flatMap(ref -> Optional.ofNullable(ref.get()));
    }

    public Optional<EnderSpectatorInventory> getOpenEnderSpectatorInventory(UUID player) {
        return Optional.ofNullable(openEnderChests.get(player))
                .flatMap(ref -> Optional.ofNullable(ref.get()));
    }

    public final CompletableFuture<Optional<UUID>> fetchUniqueId(String userName) {
        return lookup.resolveUUID(userName).thenApplyAsync(Function.identity(), serverThreadExecutor);
    }

    public final CompletableFuture<Optional<String>> fetchUserName(UUID uniqueId) {
        return lookup.resolveUserName(uniqueId).thenApplyAsync(Function.identity(), serverThreadExecutor);
    }

    public abstract MainSpectatorInventory spectateInventory(HumanEntity player, String title);
    public abstract CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title);
    public abstract CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory);

    public abstract EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title);
    public abstract CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title);
    public abstract CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest);

    /**
     * @deprecated use {@link #mainSpectatorInventory(String, String)} instead
     */
    @Deprecated(forRemoval = true)
    public CompletableFuture<Optional<MainSpectatorInventory>> spectateInventory(String userName, String title) {
        return mainSpectatorInventory(userName, title).thenApply(response -> {
            if (response.isSuccess()) return Optional.of(response.getInventory());
            else return Optional.empty();
        });
    }

    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");

        //try online
        Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!exempt.canInventoryBeSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            openInventories.put(uuid, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        }

        target = Target.byUsername(targetName);
        if (!exempt.canInventoryBeSpectated(target))
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

        //try offline
        CompletableFuture<SpectateResponse<MainSpectatorInventory>> future = fetchUniqueId(targetName).thenCompose(optUuid -> {
            if (optUuid.isPresent()) {
                UUID uuid = optUuid.get();
                return mainSpectatorInventory(uuid, targetName, title);
            }

            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(target)));
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingInventoriesByName.put(targetName, future);
        future.whenComplete((result, error) -> pendingInventoriesByName.remove(targetName));
        return future;
    }

    /** @deprecated use {@link #mainSpectatorInventory(UUID, String, String)} instead*/
    @Deprecated(forRemoval = true)
    public final CompletableFuture<Optional<MainSpectatorInventory>> spectateInventory(UUID playerId, String playerName, String title) {
        return mainSpectatorInventory(playerId, playerName, title).thenApply(response -> {
            if (response.isSuccess()) return Optional.of(response.getInventory());
            else return Optional.empty();
        });
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");

        //try cache
        WeakReference<MainSpectatorInventory> alreadyOpen = openInventories.get(playerId);
        if (alreadyOpen != null) {
            MainSpectatorInventory inv = alreadyOpen.get();
            if (inv != null) {
                return CompletableFuture.completedFuture(SpectateResponse.succeed(inv));
            }
        }

        //try online
        Player targetPlayer = plugin.getServer().getPlayer(playerId);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!exempt.canInventoryBeSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            openInventories.put(playerId, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        }

        target = Target.byUniqueId(playerId);
        if (!exempt.canInventoryBeSpectated(target)) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));
        }

        //try offline
        CompletableFuture<SpectateResponse<MainSpectatorInventory>> future = createOfflineInventory(playerId, playerName, title)
                .<SpectateResponse<MainSpectatorInventory>>thenApply(optionalInv -> {
            if (optionalInv.isPresent()) {
                MainSpectatorInventory inv = optionalInv.get();
                openInventories.put(playerId, new WeakReference<>(inv));
                return SpectateResponse.succeed(inv);
            } else {
                return SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(target));
            }
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingInventoriesByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingInventoriesByUuid.remove(playerId));
        return future;
    }

    /** use {@link #enderSpectatorInventory(String, String)} instead */
    @Deprecated(forRemoval = true)
    public CompletableFuture<Optional<EnderSpectatorInventory>> spectateEnderChest(String userName, String title) {
        return enderSpectatorInventory(userName, title).thenApply(optInv -> {
            if (optInv.isSuccess()) return Optional.of(optInv.getInventory());
            else return Optional.empty();
        });
    }

    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");

        //try online
        Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!exempt.canEnderchestBeSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, title);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            openEnderChests.put(uuid, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        }

        target = Target.byUsername(targetName);
        if (!exempt.canEnderchestBeSpectated(target))
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

        //try offline
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future = fetchUniqueId(targetName).thenCompose(optUuid -> {
            if (optUuid.isPresent()) {
                UUID uuid = optUuid.get();
                return enderSpectatorInventory(uuid, targetName, title);
            }
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(target)));
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingEnderChestsByName.put(targetName, future);
        future.whenComplete((result, error) -> pendingEnderChestsByName.remove(targetName));
        return future;
    }

    /** @deprecated use {@link #enderSpectatorInventory(UUID, String, String)} instead */
    @Deprecated(forRemoval = true)
    public final CompletableFuture<Optional<EnderSpectatorInventory>> spectateEnderChest(UUID playerId, String playerName, String title) {
        return enderSpectatorInventory(playerId, playerName, title).thenApply(response -> {
            if (response.isSuccess()) return Optional.of(response.getInventory());
            else return Optional.empty();
        });
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");

        //try cache
        WeakReference<EnderSpectatorInventory> alreadyOpen = openEnderChests.get(playerId);
        if (alreadyOpen != null) {
            EnderSpectatorInventory inv = alreadyOpen.get();
            if (inv != null) {
                return CompletableFuture.completedFuture(SpectateResponse.succeed(inv));
            }
        }

        //try online
        Player targetPlayer = plugin.getServer().getPlayer(playerId);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!exempt.canEnderchestBeSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, title);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            openEnderChests.put(playerId, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        }

        target = Target.byUniqueId(playerId);
        if (!exempt.canEnderchestBeSpectated(target))
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

        //try offline
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future = createOfflineEnderChest(playerId, playerName, title)
                .<SpectateResponse<EnderSpectatorInventory>>thenApply(optionalInv -> {
            if (optionalInv.isPresent()) {
                EnderSpectatorInventory inv = optionalInv.get();
                openEnderChests.put(playerId, new WeakReference<>(inv));
                return SpectateResponse.succeed(inv);
            }
            return SpectateResponse.fail(NotCreatedReason.targetDoesNotExists(target));
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingEnderChestsByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingEnderChestsByUuid.remove(playerId));
        return future;
    }


    // ================================== Event Stuff ==================================

    private final class PlayerListener implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String userName = player.getName();

            MainSpectatorInventory newInventorySpectator = null;
            String mainTitle = mainSpectatorInvTitleProvider.apply(player);
            EnderSpectatorInventory newEnderSpectator = null;
            String enderTitle = enderSpectatorInvTitleProvider.apply(player);

            //check if somebody was looking up the player and make sure they get the player's live inventory
            CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainInvNameFuture = pendingInventoriesByName.remove(userName);
            if (mainInvNameFuture != null) mainInvNameFuture.complete(SpectateResponse.succeed(newInventorySpectator = spectateInventory(player, mainTitle)));
            CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainInvUuidFuture = pendingInventoriesByUuid.remove(uuid);
            if (mainInvUuidFuture != null) mainInvUuidFuture.complete(SpectateResponse.succeed(newInventorySpectator != null ? newInventorySpectator : (newInventorySpectator = spectateInventory(player, mainTitle))));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderNameFuture = pendingEnderChestsByName.remove(userName);
            if (enderNameFuture != null) enderNameFuture.complete(SpectateResponse.succeed(newEnderSpectator = spectateEnderChest(player, enderTitle)));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderUuidFuture = pendingEnderChestsByUuid.remove(uuid);
            if (enderUuidFuture != null) enderUuidFuture.complete(SpectateResponse.succeed(newEnderSpectator != null ? newEnderSpectator : (newEnderSpectator = spectateEnderChest(player, enderTitle))));

            //check if somebody was looking in the offline inventory and update player's inventory.
            for (Player online : player.getServer().getOnlinePlayers()) {
                Inventory topInventory = online.getOpenInventory().getTopInventory();
                if (topInventory instanceof MainSpectatorInventory) {
                    MainSpectatorInventory oldSpectatorInventory = (MainSpectatorInventory) topInventory;
                    if (oldSpectatorInventory.getSpectatedPlayerId().equals(uuid)) {
                        if (transferInvToLivePlayer.test(oldSpectatorInventory, player)) {
                            if (newInventorySpectator == null) {
                                newInventorySpectator = spectateInventory(player, mainTitle);
                                //this also updates the player's inventory! (because they are backed by the same NonNullList<ItemStacks>s)
                                newInventorySpectator.setStorageContents(oldSpectatorInventory.getStorageContents());
                                newInventorySpectator.setArmourContents(oldSpectatorInventory.getArmourContents());
                                newInventorySpectator.setOffHandContents(oldSpectatorInventory.getOffHandContents());
                                newInventorySpectator.setCursorContents(oldSpectatorInventory.getCursorContents());
                                newInventorySpectator.setPersonalContents(oldSpectatorInventory.getPersonalContents());
                            }

                            online.closeInventory();
                            online.openInventory(newInventorySpectator);
                        }
                    }
                } else if (topInventory instanceof EnderSpectatorInventory) {
                    EnderSpectatorInventory oldSpectatorInventory = (EnderSpectatorInventory) topInventory;
                    if (oldSpectatorInventory.getSpectatedPlayerId().equals(uuid)) {
                        if (transferEnderToLivePlayer.test(oldSpectatorInventory, player)) {
                            if (newEnderSpectator == null) {
                                //this also updates the player's enderchest! (because they are backed by the same NonNullList<ItemStack>)
                                newEnderSpectator = spectateEnderChest(player, enderTitle);
                                newEnderSpectator.setStorageContents(oldSpectatorInventory.getStorageContents());
                            }

                            online.closeInventory();
                            online.openInventory(newEnderSpectator);
                        }
                    }
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, player.getName());

            WeakReference<MainSpectatorInventory> invRef = openInventories.get(uuid);
            if (invRef != null) {
                MainSpectatorInventory inv = invRef.get();
                if (inv != null) {
                    boolean open = false;
                    for (Player online : player.getServer().getOnlinePlayers()) {
                        if (online.getOpenInventory().getTopInventory().equals(inv)) {
                            open = true;
                            break;
                        }
                    }
                    if (!open) {
                        openInventories.remove(uuid);
                    }
                }
            }
            WeakReference<EnderSpectatorInventory> enderRef = openEnderChests.get(uuid);
            if (enderRef != null) {
                EnderSpectatorInventory ender = enderRef.get();
                if (ender != null) {
                    boolean open = false;
                    for (Player online : player.getServer().getOnlinePlayers()) {
                        if (online.getOpenInventory().getTopInventory().equals(ender)) {
                            open = true;
                            break;
                        }
                    }
                    if (!open) {
                        openEnderChests.remove(uuid);
                    }
                }
            }
        }
    }

    private final class InventoryListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onSpectatorClose(InventoryCloseEvent event) {
            Inventory inventory = event.getInventory();
            if (inventory instanceof MainSpectatorInventory) {
                MainSpectatorInventory spectatorInventory = (MainSpectatorInventory) inventory;
                if (event.getPlayer().getServer().getPlayer(spectatorInventory.getSpectatedPlayerId()) == null) {
                    //spectated player is no longer online
                    saveInventory(spectatorInventory).whenComplete((voidResult, throwable) -> {
                        if (throwable != null) {
                            plugin.getLogger().log(Level.SEVERE, "Error while saving offline inventory", throwable);
                            event.getPlayer().sendMessage(ChatColor.RED + "Something went wrong when trying to save the inventory.");
                        }
                    }).thenRunAsync(() -> {
                        if (spectatorInventory.getViewers().isEmpty()) {
                            openInventories.compute(spectatorInventory.getSpectatedPlayerId(), (uuid, weakRef) -> {
                                if (weakRef != null && spectatorInventory.equals(weakRef.get())) {
                                    return null;    //removes the entry
                                }
                                return weakRef; //weakRef is either null or contains another value - just keep it
                            });
                        }
                    }, serverThreadExecutor);
                }
            } else if (inventory instanceof EnderSpectatorInventory) {
                EnderSpectatorInventory spectatorInventory = (EnderSpectatorInventory) inventory;
                if (event.getPlayer().getServer().getPlayer(spectatorInventory.getSpectatedPlayerId()) == null) {
                    //spectated player is no longer online
                    saveEnderChest(spectatorInventory).whenComplete((voidResult, throwable) -> {
                        if (throwable != null) {
                            plugin.getLogger().log(Level.SEVERE, "Error while saving offline enderchest", throwable);
                            event.getPlayer().sendMessage(ChatColor.RED + "Something went wrong when trying to save the enderchest.");
                        }
                    }).thenRunAsync(() -> {
                        if (spectatorInventory.getViewers().isEmpty()) {
                            openEnderChests.compute(spectatorInventory.getSpectatedPlayerId(), (uuid, weakRef) -> {
                                if (weakRef != null && spectatorInventory.equals(weakRef.get())) {
                                    return null;    //removes the entry
                                }
                                return weakRef; //weakRef is either null or contains another value - just keep it
                            });
                        }
                    }, serverThreadExecutor);
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onTargetOpen(InventoryOpenEvent event) {
            HumanEntity target = event.getPlayer();
            WeakReference<MainSpectatorInventory> spectatorInvRef = openInventories.get(target.getUniqueId());
            MainSpectatorInventory spectatorInventory;
            if (spectatorInvRef != null && (spectatorInventory = spectatorInvRef.get()) != null) {
                //set the cursor reference and crafting inventory
                spectatorInventory.watch(event.getView());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onTargetClose(InventoryCloseEvent event) {
            HumanEntity target = event.getPlayer();
            WeakReference<MainSpectatorInventory> spectatorInvRef = openInventories.get(target.getUniqueId());
            MainSpectatorInventory spectatorInventory;
            if (spectatorInvRef != null && (spectatorInventory = spectatorInvRef.get()) != null) {
                spectatorInventory.unwatch();
            }
        }

    }

}
