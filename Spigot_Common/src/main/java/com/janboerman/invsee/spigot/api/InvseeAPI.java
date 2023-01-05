package com.janboerman.invsee.spigot.api;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.Level;

import com.janboerman.invsee.spigot.api.response.ImplementationFault;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.OfflineSupportDisabled;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.response.TargetDoesNotExist;
import com.janboerman.invsee.spigot.api.response.TargetHasExemptPermission;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import com.janboerman.invsee.spigot.internal.inventory.Personal;
import com.janboerman.invsee.utils.*;
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

    private Function<Player, String> mainSpectatorInvTitleProvider = player -> spectateInventoryTitle(player.getName());
    private Function<Player, String> enderSpectatorInvTitleProvider = player -> spectateEnderchestTitle(player.getName());

    private boolean offlineSupport = true;

    private Mirror<PlayerInventorySlot> inventoryMirror = Mirror.defaultPlayerInventory();
    private Mirror<EnderChestSlot> enderchestMirror = Mirror.defaultEnderChest();

    private Map<UUID, WeakReference<MainSpectatorInventory>> openInventories = Collections.synchronizedMap(new WeakHashMap<>());    //TODO does this need to be synchronised still?
    //TODO this^ design can fail very badly when two players spectate the same player, using different profiles!
    private final Map<String, CompletableFuture<SpectateResponse<MainSpectatorInventory>>> pendingInventoriesByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<SpectateResponse<MainSpectatorInventory>>> pendingInventoriesByUuid = new ConcurrentHashMap<>();

    private Map<UUID, WeakReference<EnderSpectatorInventory>> openEnderChests = Collections.synchronizedMap(new WeakHashMap<>());   //TODO does this need to be synchronised still?
    //TODO this^ design can fail very badly when two players spectate the same player, using different profiles!
    private final Map<String, CompletableFuture<SpectateResponse<EnderSpectatorInventory>>> pendingEnderChestsByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<SpectateResponse<EnderSpectatorInventory>>> pendingEnderChestsByUuid = new ConcurrentHashMap<>();

    //TODO I don't like the design of this. This looks like a hack purely introduced for PerWorldInventory integration
    //TODO maybe we can create a proper abstraction and use that for Multiverse-Inventories / MyWorlds?
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

    public void shutDown() {
        for (var future : pendingInventoriesByUuid.values())
            try { future.join(); } catch (Throwable e) { e.printStackTrace(); }
        for (var future : pendingEnderChestsByUuid.values())
            try { future.join(); } catch (Throwable e) { e.printStackTrace(); }
    }

    public Map<String, UUID> getUuidCache() {
        return lookup.getUuidCache();
    }

    public Map<UUID, String> getUserNameCache() {
        return lookup.getUserNameCache();
    }

    /** this method has no reason to exist. */
    @Deprecated(forRemoval = true)
    public String spectateInventoryTitle(String targetPlayerName) {
        return targetPlayerName + "'s inventory";
    }

    /** @deprecated this method has no reason to exist. */
    @Deprecated(forRemoval = true)
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

    public void unregisterListeners() {
        HandlerList.unregisterAll(playerListener);
        HandlerList.unregisterAll(inventoryListener);
    }

    public final void setOfflineSupport(boolean offlineSupport) {
        this.offlineSupport = offlineSupport;
    }

    public final void setMainInventoryTitleFactory(Function<Player, String> titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.mainSpectatorInvTitleProvider = titleFactory;
    }

    public final void setEnderInventoryTitleFactory(Function<Player, String> titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.enderSpectatorInvTitleProvider = titleFactory;
    }

    public final void setMainInventoryMirror(Mirror<PlayerInventorySlot> mirror) {
        Objects.requireNonNull(mirror);
        this.inventoryMirror = mirror;
    }

    public final void setEnderInventoryMirror(Mirror<EnderChestSlot> mirror) {
        Objects.requireNonNull(mirror);
        this.enderchestMirror = mirror;
    }

    //TODO I don't like the design of this.
    public final void setMainInventoryTransferPredicate(BiPredicate<MainSpectatorInventory, Player> bip) {
        Objects.requireNonNull(bip);
        this.transferInvToLivePlayer = bip;
    }

    //TODO I don't like the design of this.
    public final void setEnderChestTransferPredicate(BiPredicate<EnderSpectatorInventory, Player> bip) {
        Objects.requireNonNull(bip);
        this.transferEnderToLivePlayer = bip;
    }

    /** will get *protected* visibility */
    @Deprecated(forRemoval = true) //mark deprecated for removal, because for api consumers it will look as if it is removed.
    public Map<UUID, WeakReference<MainSpectatorInventory>> getOpenInventories() {
        return openInventories;
    }

    /** will get *protected* visibility */
    @Deprecated(forRemoval = true) //mark deprecated for removal, because for api consumers it will look as if it is removed.
    public Map<UUID, WeakReference<EnderSpectatorInventory>> getOpenEnderChests() {
        return openEnderChests;
    }

    //TODO I don't like the design of this
    protected void setOpenInventories(Map<UUID, WeakReference<MainSpectatorInventory>> openInventories) {
        this.openInventories = openInventories;
    }

    //TODO I don't like the design of this
    protected void setOpenEnderChests(Map<UUID, WeakReference<EnderSpectatorInventory>> openEnderChests) {
        this.openEnderChests = openEnderChests;
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


    protected void cache(MainSpectatorInventory spectatorInventory) {
        cache(spectatorInventory, false);
    }

    protected void cache(MainSpectatorInventory spectatorInventory, boolean force) {
        if (force) {
            openInventories.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
        } else {
            WeakReference<MainSpectatorInventory> ref = openInventories.get(spectatorInventory.getSpectatedPlayerId());
            MainSpectatorInventory oldSpectatorInv;
            if (ref == null || (oldSpectatorInv = ref.get()) == null) {
                openInventories.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
            } //else: don't update cache.
        }
    }

    protected void cache(EnderSpectatorInventory spectatorInventory) {
        cache(spectatorInventory, false);
    }

    protected void cache(EnderSpectatorInventory spectatorInventory, boolean force) {
        if (force) {
            openEnderChests.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
        } else {
            WeakReference<EnderSpectatorInventory> ref = openEnderChests.get(spectatorInventory.getSpectatedPlayerId());
            EnderSpectatorInventory oldSpectatorInv;
            if (ref == null || (oldSpectatorInv = ref.get()) == null) {
                openEnderChests.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
            } //else: don't update cache.
        }
    }

    // ================================== implementation methods ==================================

    //TODO for future compat: create a class CreationOptions (which includes Title and Mirror)?
    //TODO this could be a replacement for multiple parameters now.

    public MainSpectatorInventory spectateInventory(HumanEntity player, String title, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(player, title);
    }
    /**
     * Use {@link #spectateInventory(HumanEntity, String, Mirror)} instead.
     * @deprecated used to be overridden by implementations of the api, never intended to be called by api consumers.
     */
    @Deprecated
    public final MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        return spectateInventory(player, title, inventoryMirror);
    }
    public CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror) {
        return createOfflineInventory(playerId, playerName, title);
    }
    /**
     * Use {@link #createOfflineInventory(UUID, String, String, Mirror)} instead.
     * @deprecated used to be overridden by implementations of the api, never intended to be called by api consumers.
     */
    @Deprecated
    public final CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        return createOfflineInventory(playerId, playerName, title, inventoryMirror);
    }
    public abstract CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory);

    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(player, title);
    }
    /**
     * Use {@link #spectateEnderChest(HumanEntity, String, Mirror)} instead.
     * @deprecated used to be overridden by implementations of the api, never intended to be called by api consumers.
     */
    @Deprecated
    public final EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        return spectateEnderChest(player, title, enderchestMirror);
    }
    public CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, Mirror<EnderChestSlot> mirror) {
        return createOfflineEnderChest(playerId, playerName, title);
    }
    /**
     * Use {@link #createOfflineEnderChest(UUID, String, String, Mirror)} instead.
     * @deprecated used to be overridden by implementations of the api, never intended to be called by api consumers.
     */
    @Deprecated
    public final CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title) {
        return createOfflineEnderChest(playerId, playerName, title, enderchestMirror);
    }
    public abstract CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest);

    // ================================== API methods: Main Inventory ==================================

    public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity player, String title) {
        Target target = Target.byPlayer(player);
        if (exempt.isExemptedFromHavingMainInventorySpectated(target)) {
            return SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target));
        } else {
            return SpectateResponse.succeed(spectateInventory(player, title));
        }
    }

    public final CompletableFuture<Void> spectateInventory(Player spectator, String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(spectator, mainSpectatorInventory(targetName, title, offlineSupport), title, mirror, targetName);
    }

    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title) {
        return mainSpectatorInventory(targetName, title, offlineSupport);
    }

    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        return mainSpectatorInventory(targetName, title, offlineSupport, inventoryMirror);
    }

    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");
        Objects.requireNonNull(mirror, "mirror cannot be null!");

        //try online
        final Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (exempt.isExemptedFromHavingMainInventorySpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title, mirror);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!offlineSupport) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = Target.byUsername(targetName);

        // Work around a LuckPerms bug where it can't perform a permission check for players who haven't logged into the server yet,
        // because it tries to find the player's UUID in its database. How silly - it could just return whether the default group has that permission or not.
        // See: https://www.spigotmc.org/threads/invsee.456148/page-5#post-4371623
        final CompletableFuture<Boolean> isExemptedFuture;
        if (plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else {
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingMainInventorySpectated(target), asyncExecutor);
        }

        final CompletableFuture<Optional<UUID>> uuidFuture = fetchUniqueId(targetName);

        final CompletableFuture<Either<NotCreatedReason, UUID>> combinedFuture = isExemptedFuture.thenCompose(isExempted -> {
            if (isExempted) {
                return CompletableFuture.completedFuture(Either.left(NotCreatedReason.targetHasExemptPermission(target)));
            } else {
                return uuidFuture.thenApply(optionalUuid -> {
                    if (optionalUuid.isPresent()) {
                        return Either.right(optionalUuid.get());
                    } else {
                        return Either.left(NotCreatedReason.targetDoesNotExists(target));
                    }
                });
            }
        });

        //map to SpectateResponse
        final CompletableFuture<SpectateResponse<MainSpectatorInventory>> future = combinedFuture.thenCompose(eitherReasonOrUuid -> {
            if (eitherReasonOrUuid.isRight()) {
                UUID uuid = eitherReasonOrUuid.getRight();
                return mainSpectatorInventory(uuid, targetName, title, offlineSupport, mirror);
            } else {
                NotCreatedReason reason = eitherReasonOrUuid.getLeft();
                return CompletableFuture.completedFuture(SpectateResponse.fail(reason));
            }
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingInventoriesByName.put(targetName, future);
        future.whenComplete((result, error) -> pendingInventoriesByName.remove(targetName));
        return future;
    }

    public final CompletableFuture<Void> spectateInventory(Player spectator, UUID targetId, String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(spectator, mainSpectatorInventory(targetId, targetName, title, offlineSupport), title, mirror, targetId.toString());
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title) {
        return mainSpectatorInventory(playerId, playerName, title, offlineSupport);
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
        return mainSpectatorInventory(playerId, playerName, title, offlineSupport, inventoryMirror);
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");
        Objects.requireNonNull(mirror, "mirror cannot be null!");

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
            if (exempt.isExemptedFromHavingMainInventorySpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title, mirror);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!offlineSupport) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = Target.byUniqueId(playerId);
        //make LuckPerms happy by doing the permission lookup async. I am not sure how well other permission plugins handle this, but everybody uses LuckPerms nowadays so...
        final CompletableFuture<Boolean> isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingMainInventorySpectated(target), asyncExecutor);
        final CompletableFuture<Optional<NotCreatedReason>> reasonFuture = isExemptedFuture.thenApply(isExempted -> {
            if (isExempted) {
                return Optional.of(NotCreatedReason.targetHasExemptPermission(target));
            } else {
                return Optional.empty();
            }
        });

        //I really need monad transformers to make this cleaner.
        final CompletableFuture<Either<NotCreatedReason, MainSpectatorInventory>> combinedFuture = reasonFuture.thenCompose(maybeReason -> {
            if (maybeReason.isPresent()) {
                return CompletableFuture.completedFuture(Either.left(maybeReason.get()));
            } else {
                return createOfflineInventory(playerId, playerName, title, mirror).thenApply(maybeInventory -> {
                    if (maybeInventory.isPresent()) {
                        return Either.right(maybeInventory.get());
                    } else {
                        return Either.left(NotCreatedReason.targetDoesNotExists(target));
                    }
                });
            }
        });

        //map to SpectateResponse and cache if success
        CompletableFuture<SpectateResponse<MainSpectatorInventory>> future = combinedFuture.<SpectateResponse<MainSpectatorInventory>>thenApply(eitherReasonOrInventory -> {
            if (eitherReasonOrInventory.isRight()) {
                MainSpectatorInventory inventory = eitherReasonOrInventory.getRight();
                cache(inventory);
                return SpectateResponse.succeed(inventory);
            } else {
                NotCreatedReason reason = eitherReasonOrInventory.getLeft();
                return SpectateResponse.fail(reason);
            }
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingInventoriesByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingInventoriesByUuid.remove(playerId));
        return future;
    }

    // ================================== API methods: Enderchest ==================================

    public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity player, String title) {
        Target target = Target.byPlayer(player);
        if (exempt.isExemptedFromHavingEnderchestSpectated(target)) {
            return SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target));
        } else {
            return SpectateResponse.succeed(spectateEnderChest(player, title));
        }
    }

    public final CompletableFuture<Void> spectateEnderChest(Player spectator, String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(spectator, enderSpectatorInventory(targetName, title, offlineSupport), title, mirror, targetName);
    }

    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title) {
        return enderSpectatorInventory(targetName, title, offlineSupport);
    }

    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        return enderSpectatorInventory(targetName, title, offlineSupport, enderchestMirror);
    }

    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");
        Objects.requireNonNull(mirror, "mirror cannot be null!");

        //try online
        Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (exempt.isExemptedFromHavingEnderchestSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, title, mirror);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!offlineSupport) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = Target.byUsername(targetName);

        // Work around a LuckPerms bug where it can't perform a permission check for players who haven't logged into the server yet,
        // because it tries to find the player's UUID in its database. How silly - it could just return whether the default group has that permission or not.
        // See: https://www.spigotmc.org/threads/invsee.456148/page-5#post-4371623
        final CompletableFuture<Boolean> isExemptedFuture;
        if (plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else {
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingEnderchestSpectated(target), asyncExecutor);
        }

        final CompletableFuture<Optional<UUID>> uuidFuture = fetchUniqueId(targetName);

        final CompletableFuture<Either<NotCreatedReason, UUID>> combinedFuture = isExemptedFuture.thenCompose(isExempted -> {
            if (isExempted) {
                return CompletableFuture.completedFuture(Either.left(NotCreatedReason.targetHasExemptPermission(target)));
            } else {
                return uuidFuture.thenApply(optionalUuid -> {
                    if (optionalUuid.isPresent()) {
                        return Either.right(optionalUuid.get());
                    } else {
                        return Either.left(NotCreatedReason.targetDoesNotExists(target));
                    }
                });
            }
        });

        //map to SpectateResponse and cache if success
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future = combinedFuture.thenCompose(eitherReasonOrUuid -> {
            if (eitherReasonOrUuid.isRight()) {
                UUID uuid = eitherReasonOrUuid.getRight();
                return enderSpectatorInventory(uuid, targetName, title, offlineSupport, mirror);
            } else {
                NotCreatedReason reason = eitherReasonOrUuid.getLeft();
                return CompletableFuture.completedFuture(SpectateResponse.fail(reason));
            }
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingEnderChestsByName.put(targetName, future);
        future.whenComplete((result, error) -> pendingEnderChestsByName.remove(targetName));
        return future;
    }

    public final CompletableFuture<Void> spectateEnderChest(Player spectator, UUID targetId, String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(spectator, enderSpectatorInventory(targetId, targetName, title, offlineSupport), title, mirror, targetId.toString());
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title) {
        return enderSpectatorInventory(playerId, playerName, title, offlineSupport);
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
        return enderSpectatorInventory(playerId, playerName, title, offlineSupport, enderchestMirror);
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");
        Objects.requireNonNull(mirror, "mirror cannot be null!");

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
            if (exempt.isExemptedFromHavingEnderchestSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, title, mirror);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!offlineSupport) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = Target.byUniqueId(playerId);
        //make LuckPerms happy by doing the permission lookup async. I am not sure how well other permission plugins handle this, but everybody uses LuckPerms nowadays so...
        final CompletableFuture<Boolean> isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingEnderchestSpectated(target), asyncExecutor);
        final CompletableFuture<Optional<NotCreatedReason>> reasonFuture = isExemptedFuture.thenApply(isExempted -> {
            if (isExempted) {
                return Optional.of(NotCreatedReason.targetHasExemptPermission(target));
            } else {
                return Optional.empty();
            }
        });

        //I really need monad transformers to make this cleaner.
        final CompletableFuture<Either<NotCreatedReason, EnderSpectatorInventory>> combinedFuture = reasonFuture.thenCompose(maybeReason -> {
            if (maybeReason.isPresent()) {
                return CompletableFuture.completedFuture(Either.left(maybeReason.get()));
            } else {
                return createOfflineEnderChest(playerId, playerName, title, mirror).thenApply(maybeInventory -> {
                    if (maybeInventory.isPresent()) {
                        return Either.right(maybeInventory.get());
                    } else {
                        return Either.left(NotCreatedReason.targetDoesNotExists(target));
                    }
                });
            }
        });

        //map to SpectateResult and cache if success
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future = combinedFuture.<SpectateResponse<EnderSpectatorInventory>>thenApply(eitherReasonOrInventory -> {
            if (eitherReasonOrInventory.isRight()) {
                EnderSpectatorInventory inv = eitherReasonOrInventory.getRight();
                cache(inv);
                return SpectateResponse.succeed(inv);
            } else {
                NotCreatedReason reason = eitherReasonOrInventory.getLeft();
                return SpectateResponse.fail(reason);
            }
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingEnderChestsByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingEnderChestsByUuid.remove(playerId));
        return future;
    }

    // ================================== Open Main/Ender Inventory ==================================

    private final CompletableFuture<Void> spectateInventory(Player spectator, CompletableFuture<SpectateResponse<MainSpectatorInventory>> future, String title, Mirror<PlayerInventorySlot> mirror, String targetNameOrUUID) {
        return future.whenComplete((response, throwable) -> {
            if (throwable == null) {
                if (response.isSuccess()) {
                    openMainSpectatorInventory(spectator, response.getInventory(), title, mirror);
                } else {
                    NotCreatedReason reason = response.getReason();
                    if (reason instanceof TargetDoesNotExist) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUUID + " does not exist.");
                    } else if (reason instanceof TargetHasExemptPermission) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUUID + " is exempted from being spectated.");
                    } else if (reason instanceof ImplementationFault) {
                        spectator.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + targetNameOrUUID + "'s inventory.");
                    } else if (reason instanceof OfflineSupportDisabled) {
                        spectator.sendMessage(ChatColor.RED + "Spectating offline players' inventories is disabled.");
                    } else {
                        spectator.sendMessage(ChatColor.RED + "Cannot open " + targetNameOrUUID + "'s inventory for an unknown reason.");
                    }
                }
            } else {
                spectator.sendMessage(ChatColor.RED + "An error occurred while trying to open " + targetNameOrUUID + "'s inventory.");
                plugin.getLogger().log(Level.SEVERE, "Error while trying to create main-inventory spectator inventory", throwable);
            }
        }).thenApply(__ -> null);
    }

    //by default: ignore mirror, implementations can override!
    public void openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, String title, Mirror<PlayerInventorySlot> mirror) {
        spectator.openInventory(spectatorInventory);
    }

    private final CompletableFuture<Void> spectateEnderChest(Player spectator, CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future, String title, Mirror<EnderChestSlot> mirror, String targetNameOrUUID) {
        return future.whenComplete((response, throwable) -> {
            if (throwable == null) {
                if (response.isSuccess()) {
                    openEnderSpectatorInventory(spectator, response.getInventory(), title, mirror);
                } else {
                    NotCreatedReason reason = response.getReason();
                    if (reason instanceof TargetDoesNotExist) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUUID + " does not exist.");
                    } else if (reason instanceof TargetHasExemptPermission) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUUID + " is exempted from being spectated.");
                    } else if (reason instanceof ImplementationFault) {
                        spectator.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + targetNameOrUUID + "'s enderchest.");
                    } else if (reason instanceof OfflineSupportDisabled) {
                        spectator.sendMessage(ChatColor.RED + "Spectating offline players' enderchests is disabled.");
                    } else {
                        spectator.sendMessage(ChatColor.RED + "Cannot open " + targetNameOrUUID + "'s enderchest for an unknown reason.");
                    }
                }
            } else {
                spectator.sendMessage(ChatColor.RED + "An error occurred while trying to open " + targetNameOrUUID + "'s enderchest.");
                plugin.getLogger().log(Level.SEVERE, "Error while trying to create ender-chest spectator inventory", throwable);
            }
        }).thenApply(__ -> null);
    }

    //by default: ignore mirror, implementation can override!
    public void openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, String title, Mirror<EnderChestSlot> mirror) {
        spectator.openInventory(spectatorInventory);
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
            if (mainInvNameFuture != null) mainInvNameFuture.complete(SpectateResponse.succeed(newInventorySpectator = spectateInventory(player, mainTitle, inventoryMirror)));
            CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainInvUuidFuture = pendingInventoriesByUuid.remove(uuid);
            if (mainInvUuidFuture != null) mainInvUuidFuture.complete(SpectateResponse.succeed(newInventorySpectator != null ? newInventorySpectator : (newInventorySpectator = spectateInventory(player, mainTitle, inventoryMirror))));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderNameFuture = pendingEnderChestsByName.remove(userName);
            if (enderNameFuture != null) enderNameFuture.complete(SpectateResponse.succeed(newEnderSpectator = spectateEnderChest(player, enderTitle, enderchestMirror)));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderUuidFuture = pendingEnderChestsByUuid.remove(uuid);
            if (enderUuidFuture != null) enderUuidFuture.complete(SpectateResponse.succeed(newEnderSpectator != null ? newEnderSpectator : (newEnderSpectator = spectateEnderChest(player, enderTitle, enderchestMirror))));


            //check if somebody was looking in the offline inventory and update player's inventory.
            //idem for ender.

            final WeakReference<MainSpectatorInventory> invRef = openInventories.get(uuid);
            final WeakReference<EnderSpectatorInventory> enderRef = openEnderChests.get(uuid);

            if (invRef != null) {
                final MainSpectatorInventory oldMainSpectator = invRef.get();
                if (oldMainSpectator != null && transferInvToLivePlayer.test(oldMainSpectator, player)) {
                    if (newInventorySpectator == null) {
                        newInventorySpectator = spectateInventory(player, mainTitle, inventoryMirror);
                        newInventorySpectator.setContents(oldMainSpectator); //set the contents of the player's inventory to the contents that the spectators have.
                    }

                    if (oldMainSpectator instanceof ShallowCopy) {
                        //shallow-copy the live itemstack lists into the open spectator inventory.
                        ((ShallowCopy<MainSpectatorInventory>) oldMainSpectator).shallowCopyFrom(newInventorySpectator);
                        //no need to update the cache because oldMainSpectator already came from the cache!
                    } else {
                        //does not support shallow copying, just close and re-open, and update the cache!
                        for (HumanEntity viewer : List.copyOf(oldMainSpectator.getViewers())) {
                            viewer.closeInventory();
                            viewer.openInventory(newInventorySpectator);
                        }
                        cache(newInventorySpectator, true);
                    }
                }
            }

            if (enderRef != null) {
                final EnderSpectatorInventory oldEnderSpectator = enderRef.get();
                if (oldEnderSpectator != null && transferEnderToLivePlayer.test(oldEnderSpectator, player)) {
                    if (newEnderSpectator == null) {
                        newEnderSpectator = spectateEnderChest(player, enderTitle, enderchestMirror);
                        newEnderSpectator.setContents(oldEnderSpectator); //set the contents of the player's enderchest to the contents that the spectators have.
                    }

                    if (oldEnderSpectator instanceof ShallowCopy) {
                        //shallow-copy the live itemstack list into the open spectator inventory.
                        ((ShallowCopy<EnderSpectatorInventory>) oldEnderSpectator).shallowCopyFrom(newEnderSpectator);
                        //no need to update the cache because oldEnderSpectator already came from the cache!
                    } else {
                        //does not support shallow copying, just close and re-open, and update the cache!
                        for (HumanEntity viewer : List.copyOf(oldEnderSpectator.getViewers())) {
                            viewer.closeInventory();
                            viewer.openInventory(newEnderSpectator);
                        }
                        cache(newEnderSpectator, true);
                    }
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, player.getName());

            //we shan't remove spectator inventories from the openInventories and openEnderChests cache
            //the reason this logic is flawed is that API consumers could still be holding on to SpectatorInventory references, hence they should stay in the cache, even if nobody is viewing.
            //entries will be evicted from the cache once the target player uuid gets garbage-collected (happens when the player logs out and no other plugins are holding on to their uuid).
            //I don't like this design though. other plugins could be holding on to the target player's uuid for whatever reason.
            //It's better to use the SpectatorInventory itself as the WeakHashMap key. In that case entries are removed when the SpectatorInventory itself is garbage-collected.
            //I should probably override .equals in every BukkitInventory implementation then: SpectatorInventories are equal when they are of the same type, and their targetPlayerUUIDs are equal!

            //What if I use a Trie implementation with UUID keys? (use the bits of the uuid as symbols in the key strings!)
            //If I go this route, then I need to continuously check whether nodes can be cleaned up from the Trie though. That does not have to be a problem, but I think it's rather ugly tho.

            //Alternatively, I could just create new UUID objects for every SpectatorInventory (in their constructors) so that I don't hold on to existing UUIDs.
            //In that case, it is important that I *ALWAYS* use the UUID obtained from the SpectatorInventory for the cache key.
            //^ This is the solution I went with for now. It's pretty disgusting since api implementors need to be aware that this is necessary now!!
            //TODO can we improve the situation?
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
                    }); //idem: don't remove from cache.
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
                    }); //idem: don't remove from cache.
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onTargetOpen(InventoryOpenEvent event) {
            HumanEntity target = event.getPlayer();
            WeakReference<MainSpectatorInventory> spectatorInvRef = openInventories.get(target.getUniqueId());
            MainSpectatorInventory spectatorInventory;
            if (spectatorInvRef != null && (spectatorInventory = spectatorInvRef.get()) instanceof Personal) {
                //instanceof evaluates to 'false' for null values, so we can be sure that spectatorInventory is not null.
                //set the cursor reference and crafting inventory
                ((Personal) spectatorInventory).watch(event.getView());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onTargetClose(InventoryCloseEvent event) {
            HumanEntity target = event.getPlayer();
            WeakReference<MainSpectatorInventory> spectatorInvRef = openInventories.get(target.getUniqueId());
            MainSpectatorInventory spectatorInventory;
            if (spectatorInvRef != null && (spectatorInventory = spectatorInvRef.get()) instanceof Personal) {
                //instanceof evaluates to 'false' for null values, so we can be sure that spectatorInventory is not null.
                //reset personal contents to the player's own crafting contents
                ((Personal) spectatorInventory).unwatch();
            }
        }

    }

}
