package com.janboerman.invsee.spigot.api;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.Level;

import com.janboerman.invsee.spigot.api.logging.*;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.response.*;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.*;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
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

/**
 * InvSee++'s api main entrypoint. Instance can be obtained via:
 * <pre>
 * <code>
 * InvseePlusPlus plugin = (InvseePlusPlus) Bukkit.getPluginManager().getPlugin("InvSeePlusPlus");
 * InvseeAPI api = plugin.getApi();
 * </code>
 * </pre>
 */
public class InvseeAPI {

    /*
     * TODO later, we could also return an InvseeAPI instance PER PLUGIN. <-- That is going to be annoying with event listeners though.
     * TODO this is useful for logging, now that we have the PLUGIN_LOG_FILE.
     * TODO so, maybe the InvSeePlusPlus plugin does the event stuff, that way we can create multiple api instances (with shared NamesAndUUIDs as well as the Scheduler)
     */

    protected final Plugin plugin;
    protected final InvseePlatform platform;
    protected final NamesAndUUIDs lookup;
    protected final OpenSpectatorsCache openSpectatorsCache;
    protected final Exempt exempt;

    private Title mainInventoryTitle = target -> target.toString() + "'s inventory";
    private Title enderInventoryTitle = target -> target.toString() + "'s enderchest";

    private boolean offlinePlayerSupport = true;
    private boolean unknownPlayerSupport = true;

    protected Mirror<PlayerInventorySlot> inventoryMirror = Mirror.defaultPlayerInventory();
    protected Mirror<EnderChestSlot> enderchestMirror = Mirror.defaultEnderChest();

    private LogOptions logOptions = new LogOptions();
    private PlaceholderPalette placeholderPalette = PlaceholderPalette.empty();

    private final Map<String, CompletableFuture<SpectateResponse<MainSpectatorInventory>>> pendingInventoriesByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<SpectateResponse<MainSpectatorInventory>>> pendingInventoriesByUuid = new ConcurrentHashMap<>();

    private final Map<String, CompletableFuture<SpectateResponse<EnderSpectatorInventory>>> pendingEnderChestsByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<SpectateResponse<EnderSpectatorInventory>>> pendingEnderChestsByUuid = new ConcurrentHashMap<>();

    //TODO I don't like the design of this. This looks like a hack purely introduced for PerWorldInventory integration
    //TODO maybe we can create a proper abstraction and use that for Multiverse-Inventories / MyWorlds?
    //TODO See https://github.com/Jannyboy11/InvSee-plus-plus/issues/71
    private BiPredicate<MainSpectatorInventory, Player> transferInvToLivePlayer = (spectatorInv, player) -> true;
    private BiPredicate<EnderSpectatorInventory, Player> transferEnderToLivePlayer = (spectatorInv, player) -> true;

    private final Scheduler scheduler;
    /** @deprecated use {@link #getScheduler()} instead */
    @Deprecated/*(forRemoval = true, since = "0.20.0")*/ public final Executor serverThreadExecutor;
    /** @deprecated use {@link #getScheduler()} instead */
    @Deprecated/*(forRemoval = true, since = "0.20.0")*/ public final Executor asyncExecutor;

    protected final PlayerListener playerListener = new PlayerListener();
    protected final InventoryListener inventoryListener = new InventoryListener();

    /** @deprecated internal api */
    public InvseeAPI(Plugin plugin, InvseePlatform platform, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache openSpectatorsCache) {
        this.plugin = Objects.requireNonNull(plugin);

        this.lookup = Objects.requireNonNull(lookup);
        this.platform = Objects.requireNonNull(platform == null ? getPlatform() : platform);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.openSpectatorsCache = Objects.requireNonNull(openSpectatorsCache);
        this.exempt = new Exempt(plugin.getServer());

        registerListeners();

        this.serverThreadExecutor = scheduler::executeSyncGlobal;
        this.asyncExecutor = scheduler::executeAsync;
    }

    /** @apiNote unstable api */
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Deprecated //TODO refactor/remove this.
    protected InvseePlatform getPlatform() {
        return platform;
    }

    /** Called when InvSee++ disables. DO NOT CALL! */
    public void shutDown() {
        //complete futures. needed to ensure changes are saved.
        for (CompletableFuture<SpectateResponse<MainSpectatorInventory>> future : pendingInventoriesByUuid.values())
            try { future.join(); } catch (Throwable e) { e.printStackTrace(); }
        for (CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future : pendingEnderChestsByUuid.values())
            try { future.join(); } catch (Throwable e) { e.printStackTrace(); }

        //clean up logger resources
        LogOutput.closeGlobal();
    }

    /** Get the known mappings from players' usernames to unique IDs. */
    public Map<String, UUID> getUuidCache() {
        return lookup.getUuidCache();
    }

    /** Get the known mappings from players' unique IDs to usernames. */
    public Map<UUID, String> getUserNameCache() {
        return lookup.getUserNameCache();
    }

    /** @deprecated Unstable api*/
    @Deprecated
    public final NamesAndUUIDs namesAndUuidsLookup() {
        return lookup;
    }

    //TODO un-hack this registerListeners thing.
    //TODO should I make a protected constructor that takes a delegate InvseeAPI as a parameter?

    /** @deprecated internal api, subject to change */
    public void registerListeners() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(playerListener, plugin);
        pluginManager.registerEvents(inventoryListener, plugin);
    }

    /** @deprecated internal api, subject to change */
    public void unregisterListeners() {
        HandlerList.unregisterAll(playerListener);
        HandlerList.unregisterAll(inventoryListener);
    }


    // ========= creation options =========

    /** Set the offline player support status for {@link #mainInventoryCreationOptions()} and {@link #enderInventoryCreationOptions()}. */
    public final void setOfflinePlayerSupport(boolean offlinePlayerSupport) {
        this.offlinePlayerSupport = offlinePlayerSupport;
    }

    /** Set the unknown player support status for {@link #mainInventoryCreationOptions()} and {@link #enderInventoryCreationOptions()}. */
    public final void setUnknownPlayerSupport(boolean unknownPlayerSupport) {
        this.unknownPlayerSupport = unknownPlayerSupport;
    }

    /** Get whether the CreationOptions returned by {@link #mainInventoryCreationOptions()} and {@link #enderInventoryCreationOptions()}
     * will support spectating offline players. */
    public final boolean offlinePlayerSupport() {
        return this.offlinePlayerSupport;
    }
    /** Get whether the CreationOptions returned by {@link #mainInventoryCreationOptions()} and {@link #enderInventoryCreationOptions()}
     * will support spectating unknown players. */
    public final boolean unknownPlayerSupport() {
        return this.unknownPlayerSupport;
    }

    /** Set the {@link Title} to be used by {@link #mainInventoryCreationOptions()}. */
    public final void setMainInventoryTitle(Title title) {
        Objects.requireNonNull(title);
        this.mainInventoryTitle = title;
    }

    /** Set the {@link Title} to be used by {@link #enderInventoryCreationOptions()}. */
    public final void setEnderInventoryTitle(Title title) {
        Objects.requireNonNull(title);
        this.enderInventoryTitle = title;
    }

    /** Set the {@link Mirror} to be used by {@link #mainInventoryCreationOptions()}. */
    public final void setMainInventoryMirror(Mirror<PlayerInventorySlot> mirror) {
        Objects.requireNonNull(mirror);
        this.inventoryMirror = mirror;
    }

    /** Set the {@link Mirror} to be used by {@link #enderInventoryCreationOptions()}. */
    public final void setEnderInventoryMirror(Mirror<EnderChestSlot> mirror) {
        Objects.requireNonNull(mirror);
        this.enderchestMirror = mirror;
    }

    /** Set the {@link LogOptions} to be used by {@link #mainInventoryCreationOptions()} and {@link #enderInventoryCreationOptions()}. */
    public final void setLogOptions(LogOptions options) {
        Objects.requireNonNull(options);
        this.logOptions = options.clone();
    }

    /** Set the {@link PlaceholderPalette} that will be used for placeholders for absent items in non-container slots. */
    public final void setPlaceholderPalette(PlaceholderPalette palette) {
        Objects.requireNonNull(palette);
        this.placeholderPalette = palette;
    }

    /**
     * Get the configured CreationOptions for {@link MainSpectatorInventory}s.
     * @param spectator the spectating player
     * @return the creation options
     */
    public CreationOptions<PlayerInventorySlot> mainInventoryCreationOptions(Player spectator) {
        final boolean bypassExempt = spectator.hasPermission(Exempt.BYPASS_EXEMPT_INVENTORY);
        return new CreationOptions<>(plugin, mainInventoryTitle, offlinePlayerSupport, inventoryMirror, unknownPlayerSupport, bypassExempt, logOptions.clone(), placeholderPalette);
    }

    /**
     * Get the configured CreationOptions for {@link EnderSpectatorInventory}s.
     * @param spectator the spectating player
     * @return the creation options
     */
    public CreationOptions<EnderChestSlot> enderInventoryCreationOptions(Player spectator) {
        final boolean bypassExempt = spectator.hasPermission(Exempt.BYPASS_EXEMPT_ENDERCHEST);
        return new CreationOptions<>(plugin, enderInventoryTitle, offlinePlayerSupport, enderchestMirror, unknownPlayerSupport, bypassExempt, logOptions.clone(), placeholderPalette);
    }

    /**
     * Get the configured CreationOptions for {@link MainSpectatorInventory}s.
     * These creation options will always bypass the target's exemption status.
     * @return the creation options
     */
    public CreationOptions<PlayerInventorySlot> mainInventoryCreationOptions() {
        return new CreationOptions<>(plugin, mainInventoryTitle, offlinePlayerSupport, inventoryMirror, unknownPlayerSupport, false, logOptions.clone(), placeholderPalette);
    }

    /**
     * Get the configured CreationOptions for {@link EnderSpectatorInventory}s.
     * These creation options will always bypass the target's exemption status.
     * @return the creation options
     */
    public CreationOptions<EnderChestSlot> enderInventoryCreationOptions() {
        return new CreationOptions<>(plugin, enderInventoryTitle, offlinePlayerSupport, enderchestMirror, unknownPlayerSupport, false, logOptions.clone(), placeholderPalette);
    }

    // ========= end of creation options =========


    // ============== internal apis ==============

    /** @deprecated internal api */
    //TODO I don't like the design of this.
    public final void setMainInventoryTransferPredicate(BiPredicate<MainSpectatorInventory, Player> bip) {
        Objects.requireNonNull(bip);
        this.transferInvToLivePlayer = bip;
    }

    /** @deprecated internal api */
    //TODO I don't like the design of this.
    public final void setEnderChestTransferPredicate(BiPredicate<EnderSpectatorInventory, Player> bip) {
        Objects.requireNonNull(bip);
        this.transferEnderToLivePlayer = bip;
    }

    /** Get a cached {@link MainSpectatorInventory} for the target player.
     * @deprecated internal api */
    @Deprecated
    public Optional<MainSpectatorInventory> getOpenMainSpectatorInventory(UUID player) {
        return Optional.ofNullable(openSpectatorsCache.getMainSpectatorInventory(player));
    }

    /** Get a cached {@link EnderSpectatorInventory} for the target player.
     * @deprecated internal api */
    @Deprecated
    public Optional<EnderSpectatorInventory> getOpenEnderSpectatorInventory(UUID player) {
        return Optional.ofNullable(openSpectatorsCache.getEnderSpectatorInventory(player));
    }

    /** Fetches the unique ID of the player with the provided username.
     * @see NamesAndUUIDs */
    public final CompletableFuture<Optional<UUID>> fetchUniqueId(String userName) {
        return lookup.resolveUUID(userName).thenApplyAsync(Function.identity(), scheduler::executeSyncGlobal);
    }

    /** Fetches the username of the player with the provided unique ID.
     * @see NamesAndUUIDs */
    public final CompletableFuture<Optional<String>> fetchUserName(UUID uniqueId) {
        return lookup.resolveUserName(uniqueId).thenApplyAsync(Function.identity(), scheduler::executeSyncGlobal);
    }

    /** @deprecated internal api */
    @Deprecated//(forRemoval = true, since = "0.25.2")
    protected void cache(MainSpectatorInventory spectatorInventory) {
        openSpectatorsCache.cache(spectatorInventory, false);
    }

    /** @deprecated internal api */
    @Deprecated//(forRemoval = true, since = "0.25.2")
    protected void cache(MainSpectatorInventory spectatorInventory, boolean force) {
        openSpectatorsCache.cache(spectatorInventory, force);
    }

    /** @deprecated internal api */
    @Deprecated//(forRemoval = true, since = "0.25.2")
    protected void cache(EnderSpectatorInventory spectatorInventory) {
        openSpectatorsCache.cache(spectatorInventory);
    }

    /** @deprecated internal api */
    @Deprecated//(forRemoval = true, since = "0.25.2")
    protected void cache(EnderSpectatorInventory spectatorInventory, boolean force) {
        openSpectatorsCache.cache(spectatorInventory, force);
    }

    // =========== end of internal apis ===========


    // ================================== API methods: Main Inventory ==================================

    /**
     * Save the target player's inventory contents to his/her player data file.
     * @param inventory the inventory contents
     * @return a future which completes once the contents is saved
     */
    //TODO make this final.
    public CompletableFuture<SaveResponse> saveInventory(MainSpectatorInventory inventory) {
        return platform.saveInventory(inventory);
    }

    // HumanEntity

    /**
     * Opens a {@link MainSpectatorInventory} for a player.
     * @param spectator the spectating player
     * @param target the target player
     * @param options the creation options
     * @return an {@link OpenResponse} which, if successful, gives access to an {@link MainSpectatorInventoryView} which represents the open window
     */
    public final OpenResponse<MainSpectatorInventoryView> spectateInventory(Player spectator, HumanEntity target, CreationOptions<PlayerInventorySlot> options) {
        SpectateResponse<MainSpectatorInventory> response = mainSpectatorInventory(target, options);
        if (response.isSuccess()) {
            return platform.openMainSpectatorInventory(spectator, response.getInventory(), options);
        } else {
            return OpenResponse.closed(NotOpenedReason.notCreated(response.getReason()));
        }
    }

    /**
     * Create a {@link MainSpectatorInventory} which will provide access to the inventory contents of a target player.
     * @param target the target player
     * @return a {@link SpectateResponse} which, if successful, will give access to the {@link MainSpectatorInventory}
     */
    public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target) {
        return mainSpectatorInventory(target, mainInventoryCreationOptions());
    }

    /**
     * Create a {@link MainSpectatorInventory} which will provide access to the inventory contents of a target player.
     * @param target the target player
     * @param options the creation options
     * @return a {@link SpectateResponse} which, if successful, will give access to the {@link MainSpectatorInventory}
     */
    public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target, CreationOptions<PlayerInventorySlot> options) {
        Target theTarget = Target.byPlayer(target);
        if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingMainInventorySpectated(theTarget)) {
            return SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(theTarget));
        } else {
            MainSpectatorInventory inv = platform.spectateInventory(target, options);
            openSpectatorsCache.cache(inv);
            return SpectateResponse.succeed(inv);
        }
    }

    // UserName

    /**
     * Opens a {@link MainSpectatorInventory} for a player.
     * @param spectator the spectating player
     * @param targetName the name of the target player
     * @param options the creation options
     * @return a future which will complete with an {@link OpenResponse} which, if successful, gives access to an {@link MainSpectatorInventoryView} which represents the open window
     */
    public final CompletableFuture<OpenResponse<MainSpectatorInventoryView>> spectateInventory(Player spectator, String targetName, CreationOptions<PlayerInventorySlot> options) {
        return spectateInventory(spectator, mainSpectatorInventory(targetName, options), options);
    }

    /**
     * Create a {@link MainSpectatorInventory} which will provide access to the inventory contents of a target player.
     * @param targetName the username of the target player
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link MainSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName) {
        return mainSpectatorInventory(targetName, mainInventoryCreationOptions());
    }

    /**
     * Create a {@link MainSpectatorInventory} which will provide access to the inventory contents of a target player.
     * @param targetName the username of the target player
     * @param options the creation options
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link MainSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, CreationOptions<PlayerInventorySlot> options) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");
        Objects.requireNonNull(options, "options cannot be null!");

        //try online
        final Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingMainInventorySpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = platform.spectateInventory(targetPlayer, options);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            openSpectatorsCache.cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!options.isOfflinePlayerSupported()) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = Target.byUsername(targetName);

        final CompletableFuture<Boolean> isExemptedFuture;
        if (options.canBypassExemptedPlayers()) {
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else if (plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            // Work around a LuckPerms bug where it can't perform a permission check for players who haven't logged into the server yet,
            // because it tries to find the player's UUID in its database. How silly - it could just return whether the default group has that permission or not.
            // See: https://www.spigotmc.org/threads/invsee.456148/page-5#post-4371623
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else {
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingMainInventorySpectated(target), scheduler::executeAsync);
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
                return mainSpectatorInventory(uuid, targetName, options);
            } else {
                NotCreatedReason reason = eitherReasonOrUuid.getLeft();
                return CompletableFuture.completedFuture(SpectateResponse.fail(reason));
            }
        });
        pendingInventoriesByName.put(targetName, future);
        future.whenComplete((result, error) -> pendingInventoriesByName.remove(targetName));
        return future;
    }

    // UUID

    /**
     * Opens a {@link MainSpectatorInventory} for a player.
     * @param spectator the spectating player
     * @param targetId the unique ID of the target player
     * @param targetName the name of the target player
     * @param options the creation options
     * @return a future which will complete with an {@link OpenResponse} which, if successful, gives access to an {@link MainSpectatorInventoryView} which represents the open window
     */
    public final CompletableFuture<OpenResponse<MainSpectatorInventoryView>> spectateInventory(Player spectator, UUID targetId, String targetName, CreationOptions<PlayerInventorySlot> options) {
        return spectateInventory(spectator, mainSpectatorInventory(targetId, targetName, options), options);
    }

    /**
     * Create a {@link MainSpectatorInventory} which will provide access to the inventory contents of a target player.
     * @param playerId the unique ID of the target player
     * @param playerName the username of the target player
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link MainSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName) {
        return mainSpectatorInventory(playerId, playerName, mainInventoryCreationOptions());
    }

    /**
     * Create a {@link MainSpectatorInventory} which will provide access to the inventory contents of a target player.
     * @param playerId the unique ID of the target player
     * @param playerName the username of the target player
     * @param options the creation options
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link MainSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");
        Objects.requireNonNull(options, "creation options cannot be null!");

        final Target gameProfileTarget = Target.byGameProfile(playerId, playerName);
        final String title = options.getTitle().titleFor(gameProfileTarget);
        final Mirror<PlayerInventorySlot> mirror = options.getMirror();
        final boolean offlineSupport = options.isOfflinePlayerSupported();

        //try online
        Player targetPlayer = plugin.getServer().getPlayer(playerId);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingMainInventorySpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title, mirror);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            openSpectatorsCache.cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!offlineSupport) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = gameProfileTarget;
        final CompletableFuture<Boolean> isExemptedFuture;
        if (options.canBypassExemptedPlayers()) {
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else {
            //make LuckPerms happy by doing the permission lookup async. I am not sure how well other permission plugins handle this, but everybody uses LuckPerms nowadays so...
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingMainInventorySpectated(target), scheduler::executeAsync);
        }

        final CompletableFuture<Optional<NotCreatedReason>> reasonFuture = isExemptedFuture.thenApply(isExempted -> {
            if (isExempted) {
                return Optional.of(NotCreatedReason.targetHasExemptPermission(target));
            } else {
                return Optional.empty();
            }
        });

        //I really need monad transformers to make this cleaner.
        final CompletableFuture<SpectateResponse<MainSpectatorInventory>> combinedFuture = reasonFuture.thenCompose(maybeReason -> {
            if (maybeReason.isPresent()) {
                return CompletableFuture.completedFuture(SpectateResponse.fail(maybeReason.get()));
            } else {
                //try cache
                MainSpectatorInventory alreadyOpen = openSpectatorsCache.getMainSpectatorInventory(playerId);
                if (alreadyOpen != null) {
                    return CompletableFuture.completedFuture(SpectateResponse.succeed(alreadyOpen));
                }

                //not in cache: create offline inventory
                return platform.createOfflineInventory(playerId, playerName, options);
            }
        });

        //map to SpectateResponse and cache if success
        CompletableFuture<SpectateResponse<MainSpectatorInventory>> future = combinedFuture.<SpectateResponse<MainSpectatorInventory>>thenApply(eitherReasonOrInventory -> {
            eitherReasonOrInventory.ifSuccess(openSpectatorsCache::cache);
            return eitherReasonOrInventory;
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, runnable -> scheduler.executeSyncPlayer(playerId, runnable, null));
        pendingInventoriesByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingInventoriesByUuid.remove(playerId));
        return future;
    }

    // ================================== API methods: Enderchest ==================================

    /**
     * Save the target player's ender chest contents to his/her player data file.
     * @param enderChest the ender chest contents
     * @return a future which completes once the contents is saved
     */
    //TODO make this final (requires restructure: composition over inheritance)...
    public CompletableFuture<SaveResponse> saveEnderChest(EnderSpectatorInventory enderChest) {
        return platform.saveEnderChest(enderChest);
    }

    // HumanEntity

    /**
     * Opens an {@link EnderSpectatorInventory} for a player.
     * @param spectator the spectating player
     * @param target the target player
     * @param options the creation options
     * @return an {@link OpenResponse} which, if successful, gives access to an {@link EnderSpectatorInventoryView} which represents the open window
     */
    public final OpenResponse<EnderSpectatorInventoryView> spectateEnderChest(Player spectator, HumanEntity target, CreationOptions<EnderChestSlot> options) {
        SpectateResponse<EnderSpectatorInventory> response = enderSpectatorInventory(target, options);
        if (response.isSuccess()) {
            return platform.openEnderSpectatorInventory(spectator, response.getInventory(), options);
        } else {
            return OpenResponse.closed(NotOpenedReason.notCreated(response.getReason()));
        }
    }

    /**
     * Create an {@link EnderSpectatorInventory} which will provide access to the ender chest contents of a target player.
     * @param target the target player
     * @return a {@link SpectateResponse} which, if successful, will give access to the {@link EnderSpectatorInventory}
     */
    public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target) {
        return enderSpectatorInventory(target, enderInventoryCreationOptions());
    }

    /**
     * Create an {@link EnderSpectatorInventory} which will provide access to the ender chest contents of a target player.
     * @param target the target player
     * @param options the creation options
     * @return a {@link SpectateResponse} which, if successful, will give access to the {@link EnderSpectatorInventory}
     */
    public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target, CreationOptions<EnderChestSlot> options) {
        Target theTarget = Target.byPlayer(target);
        if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingEnderchestSpectated(theTarget)) {
            return SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(theTarget));
        } else {
            EnderSpectatorInventory inv = platform.spectateEnderChest(target, options);
            openSpectatorsCache.cache(inv);
            return SpectateResponse.succeed(inv);
        }
    }

    // UserName

    /**
     * Opens an {@link EnderSpectatorInventory} for a player.
     * @param spectator the spectating player
     * @param targetName the name of the target player
     * @param options the creation options
     * @return a future which will complete with an {@link OpenResponse} which, if successful, gives access to an {@link EnderSpectatorInventoryView} which represents the open window
     */
    public final CompletableFuture<OpenResponse<EnderSpectatorInventoryView>> spectateEnderChest(Player spectator, String targetName, CreationOptions<EnderChestSlot> options) {
        return spectateEnderChest(spectator, enderSpectatorInventory(targetName, options), options);
    }

    /**
     * Create an {@link EnderSpectatorInventory} which will provide access to the ender chest contents of a target player.
     * @param targetName the username of the target player
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link EnderSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName) {
        return enderSpectatorInventory(targetName, enderInventoryCreationOptions());
    }

    /**
     * Create an {@link EnderSpectatorInventory} which will provide access to the ender chest contents of a target player.
     * @param targetName the username of the target player
     * @param options the creation options
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link EnderSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, CreationOptions<EnderChestSlot> options) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");
        Objects.requireNonNull(options, "options cannot be null!");

        //try online
        Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingEnderchestSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = platform.spectateEnderChest(targetPlayer, options);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            openSpectatorsCache.cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!options.isOfflinePlayerSupported()) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = Target.byUsername(targetName);

        final CompletableFuture<Boolean> isExemptedFuture;
        if (options.canBypassExemptedPlayers()) {
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else if (plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            // Work around a LuckPerms bug where it can't perform a permission check for players who haven't logged into the server yet,
            // because it tries to find the player's UUID in its database. How silly - it could just return whether the default group has that permission or not.
            // See: https://www.spigotmc.org/threads/invsee.456148/page-5#post-4371623
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else {
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingEnderchestSpectated(target), scheduler::executeAsync);
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
                return enderSpectatorInventory(uuid, targetName, options);
            } else {
                NotCreatedReason reason = eitherReasonOrUuid.getLeft();
                return CompletableFuture.completedFuture(SpectateResponse.fail(reason));
            }
        });
        pendingEnderChestsByName.put(targetName, future);
        future.whenComplete((result, error) -> pendingEnderChestsByName.remove(targetName));
        return future;
    }

    // UUID

    /**
     * Opens an {@link EnderSpectatorInventory} for a player.
     * @param spectator the spectating player
     * @param targetId the unique ID of the target player
     * @param targetName the name of the target player
     * @param options the creation options
     * @return a future which will complete with an {@link OpenResponse} which, if successful, gives access to an {@link EnderSpectatorInventoryView} which represents the open window
     */
    public final CompletableFuture<OpenResponse<EnderSpectatorInventoryView>> spectateEnderChest(Player spectator, UUID targetId, String targetName, CreationOptions<EnderChestSlot> options) {
        return spectateEnderChest(spectator, enderSpectatorInventory(targetId, targetName, options), options);
    }

    /**
     * Create an {@link EnderSpectatorInventory} which will provide access to the ender chest contents of a target player.
     * @param playerId the unique ID of the target player
     * @param playerName the username of the target player
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link EnderSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName) {
        return enderSpectatorInventory(playerId, playerName, enderInventoryCreationOptions());
    }

    /**
     * Create an {@link EnderSpectatorInventory} which will provide access to the ender chest contents of a target player.
     * @param playerId the unique ID of the target player
     * @param playerName the username of the target player
     * @param options the creation options
     * @return a future which will complete with a {@link SpectateResponse} which, if successful, will give access to the {@link EnderSpectatorInventory}
     */
    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");
        Objects.requireNonNull(options, "options cannot be null!");

        //try online
        Player targetPlayer = plugin.getServer().getPlayer(playerId);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingEnderchestSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = platform.spectateEnderChest(targetPlayer, options);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            openSpectatorsCache.cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!options.isOfflinePlayerSupported()) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        target = Target.byGameProfile(playerId, playerName);

        final CompletableFuture<Boolean> isExemptedFuture;
        if (options.canBypassExemptedPlayers()) {
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else {
            //make LuckPerms happy by doing the permission lookup async. I am not sure how well other permission plugins handle this, but everybody uses LuckPerms nowadays so...
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingEnderchestSpectated(target), scheduler::executeAsync);
        }

        final CompletableFuture<Optional<NotCreatedReason>> reasonFuture = isExemptedFuture.thenApply(isExempted -> {
            if (isExempted) {
                return Optional.of(NotCreatedReason.targetHasExemptPermission(target));
            } else {
                return Optional.empty();
            }
        });

        //I really need monad transformers to make this cleaner.
        final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> combinedFuture = reasonFuture.thenCompose(maybeReason -> {
            if (maybeReason.isPresent()) {
                return CompletableFuture.completedFuture(SpectateResponse.fail(maybeReason.get()));
            } else {
                //try cache
                EnderSpectatorInventory alreadyOpen = openSpectatorsCache.getEnderSpectatorInventory(playerId);
                if (alreadyOpen != null) {
                    return CompletableFuture.completedFuture(SpectateResponse.succeed(alreadyOpen));
                }

                //not in cache: create offline inventory
                return platform.createOfflineEnderChest(playerId, playerName, options);
            }
        });

        //map to SpectateResult and cache if success
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future = combinedFuture.<SpectateResponse<EnderSpectatorInventory>>thenApply(eitherReasonOrInventory -> {
            eitherReasonOrInventory.ifSuccess(openSpectatorsCache::cache);
            return eitherReasonOrInventory;
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, runnable -> scheduler.executeSyncPlayer(playerId, runnable, null));
        pendingEnderChestsByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingEnderChestsByUuid.remove(playerId));
        return future;
    }

    // ================================== Open Main/Ender Inventory ==================================

    private final CompletableFuture<OpenResponse<MainSpectatorInventoryView>> spectateInventory(Player spectator, CompletableFuture<SpectateResponse<MainSpectatorInventory>> future, CreationOptions<PlayerInventorySlot> options) {
        final CompletableFuture<OpenResponse<MainSpectatorInventoryView>> result = new CompletableFuture<>();
        future.whenComplete((SpectateResponse<MainSpectatorInventory> response, Throwable throwable) -> {
            if (throwable == null) {
                if (response.isSuccess()) {
                    try {
                        OpenResponse<MainSpectatorInventoryView> openResponse = platform.openMainSpectatorInventory(spectator, response.getInventory(), options);
                        result.complete(openResponse);
                    } catch (Throwable ex) {
                        result.completeExceptionally(ex);
                    }
                } else {
                    result.complete(OpenResponse.closed(NotOpenedReason.notCreated(response.getReason())));
                }
            } else {
                result.completeExceptionally(throwable);
            }
        });
        return result;
    }

    private final CompletableFuture<OpenResponse<EnderSpectatorInventoryView>> spectateEnderChest(Player spectator, CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future, CreationOptions<EnderChestSlot> options) {
        final CompletableFuture<OpenResponse<EnderSpectatorInventoryView>> result = new CompletableFuture<>();
        future.whenComplete((SpectateResponse<EnderSpectatorInventory> response, Throwable throwable) -> {
            if (throwable == null) {
                if (response.isSuccess()) {
                    try {
                        OpenResponse<EnderSpectatorInventoryView> openResponse = platform.openEnderSpectatorInventory(spectator, response.getInventory(), options);
                        result.complete(openResponse);
                    } catch (Throwable ex) {
                        result.completeExceptionally(ex);
                    }
                } else {
                    result.complete(OpenResponse.closed(NotOpenedReason.notCreated(response.getReason())));
                }
            } else {
                result.completeExceptionally(throwable);
            }
        });
        return result;
    }

    // ================================== Event Stuff ==================================

    private final class PlayerListener implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String userName = player.getName();

            MainSpectatorInventory newInventorySpectator = null;
            EnderSpectatorInventory newEnderSpectator = null;

            //check if somebody was looking up the player and make sure they get the player's live inventory
            CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainInvNameFuture = pendingInventoriesByName.remove(userName);
            if (mainInvNameFuture != null) mainInvNameFuture.complete(SpectateResponse.succeed(newInventorySpectator = platform.spectateInventory(player, mainInventoryCreationOptions())));
            CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainInvUuidFuture = pendingInventoriesByUuid.remove(uuid);
            if (mainInvUuidFuture != null) mainInvUuidFuture.complete(SpectateResponse.succeed(newInventorySpectator != null ? newInventorySpectator : (newInventorySpectator = platform.spectateInventory(player, mainInventoryCreationOptions()))));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderNameFuture = pendingEnderChestsByName.remove(userName);
            if (enderNameFuture != null) enderNameFuture.complete(SpectateResponse.succeed(newEnderSpectator = platform.spectateEnderChest(player, enderInventoryCreationOptions())));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderUuidFuture = pendingEnderChestsByUuid.remove(uuid);
            if (enderUuidFuture != null) enderUuidFuture.complete(SpectateResponse.succeed(newEnderSpectator != null ? newEnderSpectator : (newEnderSpectator = platform.spectateEnderChest(player, enderInventoryCreationOptions()))));


            //check if somebody was looking in the offline inventory and update player's inventory.
            //idem for ender.

            final MainSpectatorInventory oldMainSpectator = openSpectatorsCache.getMainSpectatorInventory(uuid);
            final EnderSpectatorInventory oldEnderSpectator = openSpectatorsCache.getEnderSpectatorInventory(uuid);

            if (oldMainSpectator != null && transferInvToLivePlayer.test(oldMainSpectator, player)) {
                if (newInventorySpectator == null) {
                    newInventorySpectator = platform.spectateInventory(player, mainInventoryCreationOptions());
                    newInventorySpectator.setContents(oldMainSpectator); //set the contents of the player's inventory to the contents that the spectators have.
                }

                if (oldMainSpectator instanceof ShallowCopy) {
                    //shallow-copy the live itemstack lists into the open spectator inventory.
                    ((ShallowCopy<MainSpectatorInventory>) oldMainSpectator).shallowCopyFrom(newInventorySpectator);
                    //no need to update the cache because oldMainSpectator already came from the cache!
                } else {
                    //does not support shallow copying, just close and re-open, and update the cache!
                    for (HumanEntity viewer : Compat.listCopy(oldMainSpectator.getViewers())) {
                        viewer.closeInventory();
                        viewer.openInventory(newInventorySpectator);
                    }
                    openSpectatorsCache.cache(newInventorySpectator, true);
                }
            }

            if (oldEnderSpectator != null && transferEnderToLivePlayer.test(oldEnderSpectator, player)) {
                if (newEnderSpectator == null) {
                    newEnderSpectator = platform.spectateEnderChest(player, enderInventoryCreationOptions());
                    newEnderSpectator.setContents(oldEnderSpectator); //set the contents of the player's enderchest to the contents that the spectators have.
                }

                if (oldEnderSpectator instanceof ShallowCopy) {
                    //shallow-copy the live itemstack list into the open spectator inventory.
                    ((ShallowCopy<EnderSpectatorInventory>) oldEnderSpectator).shallowCopyFrom(newEnderSpectator);
                    //no need to update the cache because oldEnderSpectator already came from the cache!
                } else {
                    //does not support shallow copying, just close and re-open, and update the cache!
                    for (HumanEntity viewer : Compat.listCopy(oldEnderSpectator.getViewers())) {
                        viewer.closeInventory();
                        viewer.openInventory(newEnderSpectator);
                    }
                    openSpectatorsCache.cache(newEnderSpectator, true);
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, player.getName());

            //no need to remove open inventories (and enderchests) from the openInventories cache, since we're using a weak reference to the UUID of the target inventory as the map key.
            //As soon as no player is spectating that inventory anymore and the JVM garbage collects the SpectatorInventory, the entry will be evicted from the openInventories cache automatically.
            //This *REQUIRES* that the UUIDs used by SpectatorInventories are copies of the UUIDs of the Players. See UUIDHelper#copy(UUID).
        }

        //TODO as Paper may introduce creating multiple nms ServerPlayer objects for the same CraftPlayer,
        //TODO we need to track this properly and *reset* the nms inventory contents to that of th new (live) player,
        //TODO whenever the player changes worlds.
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
            MainSpectatorInventory spectatorInventory = openSpectatorsCache.getMainSpectatorInventory(target.getUniqueId());
            if (spectatorInventory instanceof Personal) {
                //instanceof evaluates to 'false' for null values, so we can be sure that spectatorInventory is not null.
                //set the cursor reference and crafting inventory
                ((Personal) spectatorInventory).watch(event.getView());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onTargetClose(InventoryCloseEvent event) {
            HumanEntity target = event.getPlayer();
            MainSpectatorInventory spectatorInventory = openSpectatorsCache.getMainSpectatorInventory(target.getUniqueId());
            if (spectatorInventory instanceof Personal) {
                //instanceof evaluates to 'false' for null values, so we can be sure that spectatorInventory is not null.
                //reset personal contents to the player's own crafting contents
                ((Personal) spectatorInventory).unwatch();
            }
        }

    }


    //
    // =================================== REALM OF THE DEPRECATED ===================================
    //
    //

    // =================================== deprecated public apis ====================================

    /** @deprecated use {@link #setMainInventoryTitle(Title)} */
    @Deprecated public final void setMainInventoryTitleFactory(Function<Target, String> titleFactory) {
        setMainInventoryTitle(Title.of(titleFactory));
    }

    /** @deprecated use {@link #setEnderInventoryTitle(Title)} */
    @Deprecated public final void setEnderInventoryTitleFactory(Function<Target, String> titleFactory) {
        setEnderInventoryTitle(Title.of(titleFactory));
    }

    //

    /** @deprecated use {@link #spectateInventory(Player, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<Void> spectateInventory(Player spectator, String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(spectator, targetName, mainInventoryCreationOptions(spectator).withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror))
                .whenComplete((either, throwable) -> handleMainInventoryExceptionsAndNotCreatedReasons(plugin, spectator, either, throwable, targetName))
                .thenApply(__ -> null);
    }

    /** @deprecated use {@link #spectateInventory(Player, UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<Void> spectateInventory(Player spectator, UUID targetId, String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(spectator, targetId, targetName, mainInventoryCreationOptions(spectator).withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror))
                .whenComplete((either, throwable) -> handleMainInventoryExceptionsAndNotCreatedReasons(plugin, spectator, either, throwable, targetId.toString()))
                .thenApply(__ -> null);
    }

    private static <SIV extends SpectatorInventoryView<?>> void handleMainInventoryExceptionsAndNotCreatedReasons(Plugin plugin, Player spectator, OpenResponse<SIV> openResponse, Throwable throwable, String targetNameOrUuid) {
        if (throwable == null) {
            if (!openResponse.isOpen()) {
                NotOpenedReason notOpenedReason = openResponse.getReason();
                if (notOpenedReason instanceof InventoryOpenEventCancelled) {
                    spectator.sendMessage(ChatColor.RED + "Another plugin prevented you from spectating " + targetNameOrUuid + "'s inventory");
                } else if (notOpenedReason instanceof InventoryNotCreated) {
                    NotCreatedReason notCreatedReason = ((InventoryNotCreated) notOpenedReason).getNotCreatedReason();
                    if (notCreatedReason instanceof TargetDoesNotExist) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUuid + " does not exist.");
                    } else if (notCreatedReason instanceof UnknownTarget) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUuid + " has not logged onto the server yet.");
                    }  else if (notCreatedReason instanceof TargetHasExemptPermission) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUuid + " is exempted from being spectated.");
                    } else if (notCreatedReason instanceof ImplementationFault) {
                        spectator.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + targetNameOrUuid + "'s inventory.");
                    } else if (notCreatedReason instanceof OfflineSupportDisabled) {
                        spectator.sendMessage(ChatColor.RED + "Spectating offline players' inventories is disabled.");
                    } else {
                        spectator.sendMessage(ChatColor.RED + "Could not create " + targetNameOrUuid + "'s inventory for an unknown reason.");
                    }
                } else {
                    spectator.sendMessage(ChatColor.RED + "Could not open " + targetNameOrUuid + "'s inventory for an unknown reason.");
                }
            }
        } else {
            spectator.sendMessage(ChatColor.RED + "An error occurred while trying to open " + targetNameOrUuid + "'s inventory.");
            plugin.getLogger().log(Level.SEVERE, "Error while trying to create main-inventory spectator inventory", throwable);
        }
    }

    /** @deprecated use {@link #spectateEnderChest(Player, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<Void> spectateEnderChest(Player spectator, String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(spectator, targetName, enderInventoryCreationOptions(spectator).withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror))
                .whenComplete((either, throwable) -> handleEnderInventoryExceptionsAndNotCreatedReasons(plugin, spectator, either, throwable, targetName))
                .thenApply(__ -> null);
    }

    /** @deprecated use {@link #spectateEnderChest(Player, UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<Void> spectateEnderChest(Player spectator, UUID targetId, String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(spectator, targetId, targetName, enderInventoryCreationOptions(spectator).withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror))
                .whenComplete((either, throwable) -> handleEnderInventoryExceptionsAndNotCreatedReasons(plugin, spectator, either, throwable, targetId.toString()))
                .thenApply(__ -> null);
    }

    private static <SIV extends SpectatorInventoryView<?>> void handleEnderInventoryExceptionsAndNotCreatedReasons(Plugin plugin, Player spectator, OpenResponse<SIV> openResponse, Throwable throwable, String targetNameOrUuid) {
        if (throwable == null) {
            if (!openResponse.isOpen()) {
                NotOpenedReason notOpenedReason = openResponse.getReason();
                if (notOpenedReason instanceof InventoryOpenEventCancelled) {
                    spectator.sendMessage(ChatColor.RED + "Another plugin prevented you from spectating " + targetNameOrUuid + "'s ender chest.");
                } else if (notOpenedReason instanceof InventoryNotCreated) {
                    NotCreatedReason reason = ((InventoryNotCreated) notOpenedReason).getNotCreatedReason();
                    if (reason instanceof TargetDoesNotExist) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUuid + " does not exist.");
                    } else if (reason instanceof UnknownTarget) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUuid + " has not logged onto the server yet.");
                    } else if (reason instanceof TargetHasExemptPermission) {
                        spectator.sendMessage(ChatColor.RED + "Player " + targetNameOrUuid + " is exempted from being spectated.");
                    } else if (reason instanceof ImplementationFault) {
                        spectator.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + targetNameOrUuid + "'s enderchest.");
                    } else if (reason instanceof OfflineSupportDisabled) {
                        spectator.sendMessage(ChatColor.RED + "Spectating offline players' enderchests is disabled.");
                    } else {
                        spectator.sendMessage(ChatColor.RED + "Could not create " + targetNameOrUuid + "'s enderchest for an unknown reason.");
                    }
                } else {
                    spectator.sendMessage(ChatColor.RED + "Could not open " + targetNameOrUuid + "'s enderchest for an unknown reason.");
                }
            }
        } else {
            spectator.sendMessage(ChatColor.RED + "An error occurred while trying to open " + targetNameOrUuid + "'s enderchest.");
            plugin.getLogger().log(Level.SEVERE, "Error while trying to create ender-chest spectator inventory", throwable);
        }
    }

    // open main spectator inventory using parameters

    // HumanEntity target

    /** @deprecated use {@link #mainSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target, String title) {
        return mainSpectatorInventory(target, mainInventoryCreationOptions().withTitle(title));
    }

    /** @deprecated use {@link #mainSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target, String title, Mirror<PlayerInventorySlot> mirror) {
        return mainSpectatorInventory(target, mainInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    // UserName target

    /** @deprecated use {@link #mainSpectatorInventory(String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title) {
        return mainSpectatorInventory(targetName, title, offlinePlayerSupport);
    }

    /** @deprecated use {@link #mainSpectatorInventory(String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        return mainSpectatorInventory(targetName, title, offlineSupport, inventoryMirror);
    }

    /** @deprecated use {@link #mainSpectatorInventory(String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return mainSpectatorInventory(targetName, mainInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror));
    }

    // UUID target

    /** @deprecated use {@link #mainSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title) {
        return mainSpectatorInventory(playerId, playerName, title, offlinePlayerSupport);
    }

    /** @deprecated use {@link #mainSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
        return mainSpectatorInventory(playerId, playerName, title, offlineSupport, inventoryMirror);
    }

    /** @deprecated use {@link #mainSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return mainSpectatorInventory(playerId, playerName, CreationOptions.of(plugin, Title.of(title), offlineSupport, mirror, unknownPlayerSupport, false, LogOptions.empty()));
    }

    // open ender spectator inventory using parameters

    // HumanEntity target

    /** @deprecated use {@link #enderSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target, String title) {
        return enderSpectatorInventory(target, enderInventoryCreationOptions().withTitle(title));
    }

    /** @deprecated use {@link #enderSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target, String title, Mirror<EnderChestSlot> mirror) {
        return enderSpectatorInventory(target, enderInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    // UserName target:

    /** @deprecated use {@link #enderSpectatorInventory(String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title) {
        return enderSpectatorInventory(targetName, title, offlinePlayerSupport);
    }

    /** @deprecated use {@link #enderSpectatorInventory(String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        return enderSpectatorInventory(targetName, title, offlineSupport, enderchestMirror);
    }

    /** @deprecated use {@link #enderSpectatorInventory(String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return enderSpectatorInventory(targetName, enderInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror));
    }

    // UUID target

    /** @deprecated use {@link #enderSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title) {
        return enderSpectatorInventory(playerId, playerName, title, offlinePlayerSupport);
    }

    /** @deprecated use {@link #enderSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
        return enderSpectatorInventory(playerId, playerName, title, offlineSupport, enderchestMirror);
    }

    /** @deprecated use {@link #enderSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return enderSpectatorInventory(playerId, playerName, enderInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror));
    }

    // apis that open immideately:

    /** @deprecated use {@link #spectateInventory(Player, HumanEntity, CreationOptions)} or {@link #spectateInventory(Player, UUID, String, CreationOptions)} */
    @Deprecated public final void openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, String title, Mirror<PlayerInventorySlot> mirror) {
        platform.openMainSpectatorInventory(spectator, spectatorInventory, mainInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    /** @deprecated use {@link #spectateEnderChest(Player, HumanEntity, CreationOptions)} or {@link #spectateEnderChest(Player, UUID, String, CreationOptions)} */
    @Deprecated public final void openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, String title, Mirror<EnderChestSlot> mirror) {
        platform.openEnderSpectatorInventory(spectator, spectatorInventory, enderInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    // =================================== deprecated private apis ===================================

    // implementation methods:

    /** @deprecated use {@link #enderSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, Mirror<EnderChestSlot> mirror) {
        return platform.createOfflineEnderChest(playerId, playerName, enderInventoryCreationOptions().withTitle(title).withMirror(mirror))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }
    /** @deprecated use {@link #enderSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title) {
        return platform.createOfflineEnderChest(playerId, playerName, enderInventoryCreationOptions().withTitle(title))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }

    /** @deprecated use {@link #enderSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title, Mirror<EnderChestSlot> mirror) {
        return platform.spectateEnderChest(player, enderInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }
    /** @deprecated use {@link #enderSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        return platform.spectateEnderChest(player, enderInventoryCreationOptions().withTitle(title));
    }

    /** @deprecated use {@link #mainSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror) {
        return platform.createOfflineInventory(playerId, playerName, mainInventoryCreationOptions().withTitle(title).withMirror(mirror))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }
    /** @deprecated use {@link #mainSpectatorInventory(UUID, String, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        return platform.createOfflineInventory(playerId, playerName, mainInventoryCreationOptions().withTitle(title))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }

    /** @deprecated use {@link #mainSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final MainSpectatorInventory spectateInventory(HumanEntity player, String title, Mirror<PlayerInventorySlot> mirror) {
        return platform.spectateInventory(player, mainInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }
    /** @deprecated use {@link #mainSpectatorInventory(HumanEntity, CreationOptions)} */
    @Deprecated//(forRemoval = true, since = "0.19.6")
    public final MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        return platform.spectateInventory(player, mainInventoryCreationOptions().withTitle(title));
    }

}
