package com.janboerman.invsee.spigot.api;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.Level;

import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.response.ImplementationFault;
import com.janboerman.invsee.spigot.api.response.InventoryNotCreated;
import com.janboerman.invsee.spigot.api.response.InventoryOpenEventCancelled;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.NotOpenedReason;
import com.janboerman.invsee.spigot.api.response.OfflineSupportDisabled;
import com.janboerman.invsee.spigot.api.response.OpenResponse;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import com.janboerman.invsee.spigot.api.response.TargetDoesNotExist;
import com.janboerman.invsee.spigot.api.response.TargetHasExemptPermission;
import com.janboerman.invsee.spigot.api.response.UnknownTarget;
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

    /* TODO this class needs a BIG refactor.
     * TODO implementations should be able to override *just* the abstract methods.
     * TODO I should create a new interface for this.
     * TODO I think I'll call this new interface "Platform".
     * TODO we then use composition over inheritance!
     *
     * TODO later, we could also return an InvseeAPI instance PER PLUGIN.
     * TODO this is useful for logging, now that we have the PLUGIN_LOG_FILE.
     */

    protected final Plugin plugin;
    protected final NamesAndUUIDs lookup;
    protected final Exempt exempt;

    private Title mainInventoryTitle = target -> target.toString() + "'s inventory";
    private Title enderInventoryTitle = target -> target.toString() + "'s enderchest";

    private boolean offlinePlayerSupport = true;
    private boolean unknownPlayerSupport = true;

    private Mirror<PlayerInventorySlot> inventoryMirror = Mirror.defaultPlayerInventory();
    private Mirror<EnderChestSlot> enderchestMirror = Mirror.defaultEnderChest();

    private LogOptions logOptions = new LogOptions();

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

    /** @deprecated Unstable api*/
    @Deprecated
    public final NamesAndUUIDs namesAndUuidsLookup() {
        return lookup;
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


    // ========= creation options =========

    public final void setOfflinePlayerSupport(boolean offlinePlayerSupport) {
        this.offlinePlayerSupport = offlinePlayerSupport;
    }

    public final void setUnknownPlayerSupport(boolean unknownPlayerSupport) {
        this.unknownPlayerSupport = unknownPlayerSupport;
    }

    public final boolean offlinePlayerSupport() {
        return this.offlinePlayerSupport;
    }

    public final boolean unknownPlayerSupport() {
        return this.unknownPlayerSupport;
    }

    public final void setMainInventoryTitle(Title titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.mainInventoryTitle = titleFactory;
    }

    public final void setEnderInventoryTitle(Title titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.enderInventoryTitle = titleFactory;
    }

    public final void setMainInventoryMirror(Mirror<PlayerInventorySlot> mirror) {
        Objects.requireNonNull(mirror);
        this.inventoryMirror = mirror;
    }

    public final void setEnderInventoryMirror(Mirror<EnderChestSlot> mirror) {
        Objects.requireNonNull(mirror);
        this.enderchestMirror = mirror;
    }

    public final void setLogOptions(LogOptions options) {
        Objects.requireNonNull(options);
        this.logOptions = options.clone();
    }

    public CreationOptions<PlayerInventorySlot> mainInventoryCreationOptions(Player spectator) {
        final boolean bypassExempt = spectator.hasPermission(Exempt.BYPASS_EXEMPT_INVENTORY);
        return new CreationOptions<>(plugin, mainInventoryTitle, offlinePlayerSupport, inventoryMirror, unknownPlayerSupport, bypassExempt, logOptions.clone());
    }

    public CreationOptions<EnderChestSlot> enderInventoryCreationOptions(Player spectator) {
        final boolean bypassExempt = spectator.hasPermission(Exempt.BYPASS_EXEMPT_ENDERCHEST);
        return new CreationOptions<>(plugin, enderInventoryTitle, offlinePlayerSupport, enderchestMirror, unknownPlayerSupport, bypassExempt, logOptions.clone());
    }

    public CreationOptions<PlayerInventorySlot> mainInventoryCreationOptions() {
        return new CreationOptions<>(plugin, mainInventoryTitle, offlinePlayerSupport, inventoryMirror, unknownPlayerSupport, false, logOptions.clone());
    }

    public CreationOptions<EnderChestSlot> enderInventoryCreationOptions() {
        return new CreationOptions<>(plugin, enderInventoryTitle, offlinePlayerSupport, enderchestMirror, unknownPlayerSupport, false, logOptions.clone());
    }

    // ========= end of creation options =========


    // ============== internal apis ==============

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

    protected static Map<UUID, WeakReference<MainSpectatorInventory>> getOpenInventories(InvseeAPI api) {
        return api.openInventories;
    }

    protected static Map<UUID, WeakReference<EnderSpectatorInventory>> getOpenEnderChests(InvseeAPI api) {
        return api.openEnderChests;
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
        WeakReference<MainSpectatorInventory> ref;
        MainSpectatorInventory oldSpectatorInv;
        if (force || (ref = openInventories.get(spectatorInventory.getSpectatedPlayerId())) == null || (oldSpectatorInv = ref.get()) == null) {
            openInventories.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
        }
    }

    protected void cache(EnderSpectatorInventory spectatorInventory) {
        cache(spectatorInventory, false);
    }

    protected void cache(EnderSpectatorInventory spectatorInventory, boolean force) {
        WeakReference<EnderSpectatorInventory> ref;
        EnderSpectatorInventory oldSpectatorInv;
        if (force || (ref = openEnderChests.get(spectatorInventory.getSpectatedPlayerId())) == null || (oldSpectatorInv = ref.get()) == null) {
            openEnderChests.put(spectatorInventory.getSpectatedPlayerId(), new WeakReference<>(spectatorInventory));
        }
    }

    // =========== end of internal apis ===========


    // ================================== implementation methods ==================================

    public abstract MainSpectatorInventory spectateInventory(HumanEntity target, CreationOptions<PlayerInventorySlot> options);

    public abstract CompletableFuture<SpectateResponse<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options);

    public abstract CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory);

    //TODO should probably make this abstract.
    //by default: ignore creation options, implementations can override!
    public OpenResponse<MainSpectatorInventoryView> openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, CreationOptions<PlayerInventorySlot> options) {
        MainSpectatorInventoryView view = (MainSpectatorInventoryView) spectator.openInventory(spectatorInventory);
        if (view != null) {
            return OpenResponse.open(view);
        } else {
            return OpenResponse.closed(NotOpenedReason.generic());
        }
    }


    public abstract EnderSpectatorInventory spectateEnderChest(HumanEntity target, CreationOptions<EnderChestSlot> options);

    public abstract CompletableFuture<SpectateResponse<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, CreationOptions<EnderChestSlot> options);

    public abstract CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest);

    //TODO should probably make this abstract.
    //by default: ignore creation options, implementation can override!
    public OpenResponse<EnderSpectatorInventoryView> openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, CreationOptions<EnderChestSlot> options) {
        EnderSpectatorInventoryView view = (EnderSpectatorInventoryView) spectator.openInventory(spectatorInventory);
        if (view != null) {
            return OpenResponse.open(view);
        } else {
            return OpenResponse.closed(NotOpenedReason.generic());
        }
    }

    // ================================== API methods: Main Inventory ==================================

    // HumanEntity

    public final OpenResponse<MainSpectatorInventoryView> spectateInventory(Player spectator, HumanEntity target, CreationOptions<PlayerInventorySlot> options) {
        SpectateResponse<MainSpectatorInventory> response = mainSpectatorInventory(target, options);
        if (response.isSuccess()) {
            return openMainSpectatorInventory(spectator, response.getInventory(), options);
        } else {
            return OpenResponse.closed(NotOpenedReason.notCreated(response.getReason()));
        }
    }

    public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target) {
        return mainSpectatorInventory(target, mainInventoryCreationOptions());
    }

    public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target, CreationOptions<PlayerInventorySlot> options) {
        Target theTarget = Target.byPlayer(target);
        if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingMainInventorySpectated(theTarget)) {
            return SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(theTarget));
        } else {
            MainSpectatorInventory inv = spectateInventory(target, options);
            cache(inv);
            return SpectateResponse.succeed(inv);
        }
    }

    // UserName

    public final CompletableFuture<OpenResponse<MainSpectatorInventoryView>> spectateInventory(Player spectator, String targetName, CreationOptions<PlayerInventorySlot> options) {
        return spectateInventory(spectator, mainSpectatorInventory(targetName, options), options);
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName) {
        return mainSpectatorInventory(targetName, mainInventoryCreationOptions());
    }

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

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, options);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            cache(spectatorInventory);
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
                return mainSpectatorInventory(uuid, targetName, options);
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

    // UUID

    public final CompletableFuture<OpenResponse<MainSpectatorInventoryView>> spectateInventory(Player spectator, UUID targetId, String targetName, CreationOptions<PlayerInventorySlot> options) {
        return spectateInventory(spectator, mainSpectatorInventory(targetId, targetName, options), options);
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName) {
        return mainSpectatorInventory(playerId, playerName, mainInventoryCreationOptions());
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, CreationOptions<PlayerInventorySlot> options) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");
        Objects.requireNonNull(options, "creation options cannot be null!");

        final Target gameProfileTarget = Target.byGameProfile(playerId, playerName);
        final String title = options.getTitle().titleFor(gameProfileTarget);
        final Mirror<PlayerInventorySlot> mirror = options.getMirror();
        final boolean offlineSupport = options.isOfflinePlayerSupported();

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
            if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingMainInventorySpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title, mirror);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            cache(spectatorInventory);
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
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingMainInventorySpectated(target), asyncExecutor);
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
                return createOfflineInventory(playerId, playerName, options);
            }
        });

        //map to SpectateResponse and cache if success
        CompletableFuture<SpectateResponse<MainSpectatorInventory>> future = combinedFuture.<SpectateResponse<MainSpectatorInventory>>thenApply(eitherReasonOrInventory -> {
            eitherReasonOrInventory.ifSuccess(this::cache);
            return eitherReasonOrInventory;
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingInventoriesByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingInventoriesByUuid.remove(playerId));
        return future;
    }

    // ================================== API methods: Enderchest ==================================

    // HumanEntity

    public final OpenResponse<EnderSpectatorInventoryView> spectateEnderChest(Player spectator, HumanEntity target, CreationOptions<EnderChestSlot> options) {
        SpectateResponse<EnderSpectatorInventory> response = enderSpectatorInventory(target, options);
        if (response.isSuccess()) {
            return openEnderSpectatorInventory(spectator, response.getInventory(), options);
        } else {
            return OpenResponse.closed(NotOpenedReason.notCreated(response.getReason()));
        }
    }

    public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target) {
        return enderSpectatorInventory(target, enderInventoryCreationOptions());
    }

    public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target, CreationOptions<EnderChestSlot> options) {
        Target theTarget = Target.byPlayer(target);
        if (!options.canBypassExemptedPlayers() && exempt.isExemptedFromHavingEnderchestSpectated(theTarget)) {
            return SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(theTarget));
        } else {
            EnderSpectatorInventory inv = spectateEnderChest(target, options);
            cache(inv);
            return SpectateResponse.succeed(inv);
        }
    }

    // UserName

    public final CompletableFuture<OpenResponse<EnderSpectatorInventoryView>> spectateEnderChest(Player spectator, String targetName, CreationOptions<EnderChestSlot> options) {
        return spectateEnderChest(spectator, enderSpectatorInventory(targetName, options), options);
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName) {
        return enderSpectatorInventory(targetName, enderInventoryCreationOptions());
    }

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

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, options);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            cache(spectatorInventory);
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
                return enderSpectatorInventory(uuid, targetName, options);
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

    // UUID

    public final CompletableFuture<OpenResponse<EnderSpectatorInventoryView>> spectateEnderChest(Player spectator, UUID targetId, String targetName, CreationOptions<EnderChestSlot> options) {
        return spectateEnderChest(spectator, enderSpectatorInventory(targetId, targetName, options), options);
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName) {
        return enderSpectatorInventory(playerId, playerName, enderInventoryCreationOptions());
    }

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

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, options);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            cache(spectatorInventory);
            return CompletableFuture.completedFuture(SpectateResponse.succeed(spectatorInventory));
        } else if (!options.isOfflinePlayerSupported()) {
            return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.offlineSupportDisabled()));
        }

        //try cache (can actually do this, because if the target is exempted, then he/she is absent from the cache!)
        WeakReference<EnderSpectatorInventory> alreadyOpen = openEnderChests.get(playerId);
        if (alreadyOpen != null) {
            EnderSpectatorInventory inv = alreadyOpen.get();
            if (inv != null) {
                return CompletableFuture.completedFuture(SpectateResponse.succeed(inv));
            }
        }

        target = Target.byGameProfile(playerId, playerName);

        final CompletableFuture<Boolean> isExemptedFuture;
        if (options.canBypassExemptedPlayers()) {
            isExemptedFuture = CompletableFuture.completedFuture(false);
        } else {
            //make LuckPerms happy by doing the permission lookup async. I am not sure how well other permission plugins handle this, but everybody uses LuckPerms nowadays so...
            isExemptedFuture = CompletableFuture.supplyAsync(() -> exempt.isExemptedFromHavingEnderchestSpectated(target), asyncExecutor);
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
                return createOfflineEnderChest(playerId, playerName, options);
            }
        });

        //map to SpectateResult and cache if success
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> future = combinedFuture.<SpectateResponse<EnderSpectatorInventory>>thenApply(eitherReasonOrInventory -> {
            eitherReasonOrInventory.ifSuccess(this::cache);
            return eitherReasonOrInventory;
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
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
                        OpenResponse<MainSpectatorInventoryView> openResponse = openMainSpectatorInventory(spectator, response.getInventory(), options);
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
                        OpenResponse<EnderSpectatorInventoryView> openResponse = openEnderSpectatorInventory(spectator, response.getInventory(), options);
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
            Target target = Target.byPlayer(player);

            MainSpectatorInventory newInventorySpectator = null;
            EnderSpectatorInventory newEnderSpectator = null;

            //check if somebody was looking up the player and make sure they get the player's live inventory
            CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainInvNameFuture = pendingInventoriesByName.remove(userName);
            if (mainInvNameFuture != null) mainInvNameFuture.complete(SpectateResponse.succeed(newInventorySpectator = spectateInventory(player, mainInventoryCreationOptions())));
            CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainInvUuidFuture = pendingInventoriesByUuid.remove(uuid);
            if (mainInvUuidFuture != null) mainInvUuidFuture.complete(SpectateResponse.succeed(newInventorySpectator != null ? newInventorySpectator : (newInventorySpectator = spectateInventory(player, mainInventoryCreationOptions()))));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderNameFuture = pendingEnderChestsByName.remove(userName);
            if (enderNameFuture != null) enderNameFuture.complete(SpectateResponse.succeed(newEnderSpectator = spectateEnderChest(player, enderInventoryCreationOptions())));
            CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderUuidFuture = pendingEnderChestsByUuid.remove(uuid);
            if (enderUuidFuture != null) enderUuidFuture.complete(SpectateResponse.succeed(newEnderSpectator != null ? newEnderSpectator : (newEnderSpectator = spectateEnderChest(player, enderInventoryCreationOptions()))));


            //check if somebody was looking in the offline inventory and update player's inventory.
            //idem for ender.

            final WeakReference<MainSpectatorInventory> invRef = openInventories.get(uuid);
            final WeakReference<EnderSpectatorInventory> enderRef = openEnderChests.get(uuid);

            if (invRef != null) {
                final MainSpectatorInventory oldMainSpectator = invRef.get();
                if (oldMainSpectator != null && transferInvToLivePlayer.test(oldMainSpectator, player)) {
                    if (newInventorySpectator == null) {
                        newInventorySpectator = spectateInventory(player, mainInventoryCreationOptions());
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
                        newEnderSpectator = spectateEnderChest(player, enderInventoryCreationOptions());
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


    //
    // =================================== REALM OF THE DEPRECATED ===================================
    //
    //

    // =================================== deprecated public apis ====================================

    @Deprecated
    public final void setMainInventoryTitleFactory(Function<Target, String> titleFactory) {
        setMainInventoryTitle(Title.of(titleFactory));
    }

    @Deprecated
    public final void setEnderInventoryTitleFactory(Function<Target, String> titleFactory) {
        setEnderInventoryTitle(Title.of(titleFactory));
    }

    //

    @Deprecated public final CompletableFuture<Void> spectateInventory(Player spectator, String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(spectator, targetName, mainInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror))
                .whenComplete((either, throwable) -> handleMainInventoryExceptionsAndNotCreatedReasons(plugin, spectator, either, throwable, targetName))
                .thenApply(__ -> null);
    }

    @Deprecated public final CompletableFuture<Void> spectateInventory(Player spectator, UUID targetId, String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(spectator, targetId, targetName, mainInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror))
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

    @Deprecated public final CompletableFuture<Void> spectateEnderChest(Player spectator, String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(spectator, targetName, enderInventoryCreationOptions(spectator).withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror))
                .whenComplete((either, throwable) -> handleEnderInventoryExceptionsAndNotCreatedReasons(plugin, spectator, either, throwable, targetName))
                .thenApply(__ -> null);
    }

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

    @Deprecated public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target, String title) {
        return mainSpectatorInventory(target, mainInventoryCreationOptions().withTitle(title));
    }

    @Deprecated public final SpectateResponse<MainSpectatorInventory> mainSpectatorInventory(HumanEntity target, String title, Mirror<PlayerInventorySlot> mirror) {
        return mainSpectatorInventory(target, mainInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    // UserName target

    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title) {
        return mainSpectatorInventory(targetName, title, offlinePlayerSupport);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        return mainSpectatorInventory(targetName, title, offlineSupport, inventoryMirror);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return mainSpectatorInventory(targetName, mainInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror));
    }

    // UUID target

    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title) {
        return mainSpectatorInventory(playerId, playerName, title, offlinePlayerSupport);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
        return mainSpectatorInventory(playerId, playerName, title, offlineSupport, inventoryMirror);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport, Mirror<PlayerInventorySlot> mirror) {
        return mainSpectatorInventory(playerId, playerName, new CreationOptions<>(plugin, Title.of(title), offlineSupport, mirror, unknownPlayerSupport, false, LogOptions.empty()));
    }

    // open ender spectator inventory using parameters

    // HumanEntity target

    @Deprecated public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target, String title) {
        return enderSpectatorInventory(target, enderInventoryCreationOptions().withTitle(title));
    }

    @Deprecated public final SpectateResponse<EnderSpectatorInventory> enderSpectatorInventory(HumanEntity target, String title, Mirror<EnderChestSlot> mirror) {
        return enderSpectatorInventory(target, enderInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    // UserName target:

    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title) {
        return enderSpectatorInventory(targetName, title, offlinePlayerSupport);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        return enderSpectatorInventory(targetName, title, offlineSupport, enderchestMirror);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return enderSpectatorInventory(targetName, enderInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror));
    }

    // UUID target

    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title) {
        return enderSpectatorInventory(playerId, playerName, title, offlinePlayerSupport);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
        return enderSpectatorInventory(playerId, playerName, title, offlineSupport, enderchestMirror);
    }

    @Deprecated public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport, Mirror<EnderChestSlot> mirror) {
        return enderSpectatorInventory(playerId, playerName, enderInventoryCreationOptions().withTitle(title).withOfflinePlayerSupport(offlineSupport).withMirror(mirror));
    }

    // apis that open immideately:

    @Deprecated public final void openMainSpectatorInventory(Player spectator, MainSpectatorInventory spectatorInventory, String title, Mirror<PlayerInventorySlot> mirror) {
        openMainSpectatorInventory(spectator, spectatorInventory, mainInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    @Deprecated public final void openEnderSpectatorInventory(Player spectator, EnderSpectatorInventory spectatorInventory, String title, Mirror<EnderChestSlot> mirror) {
        openEnderSpectatorInventory(spectator, spectatorInventory, enderInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }

    // =================================== deprecated private apis ===================================

    // implementation methods:

    @Deprecated(forRemoval = true)
    public final CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title, Mirror<EnderChestSlot> mirror) {
        return createOfflineEnderChest(playerId, playerName, enderInventoryCreationOptions().withTitle(title).withMirror(mirror))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }
    @Deprecated(forRemoval = true)
    public final CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title) {
        return createOfflineEnderChest(playerId, playerName, enderInventoryCreationOptions().withTitle(title))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }

    @Deprecated(forRemoval = true)
    public final EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(player, enderInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }
    @Deprecated(forRemoval = true)
    public final EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        return spectateEnderChest(player, enderInventoryCreationOptions().withTitle(title));
    }

    @Deprecated(forRemoval = true)
    public final CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title, Mirror<PlayerInventorySlot> mirror) {
        return createOfflineInventory(playerId, playerName, mainInventoryCreationOptions().withTitle(title).withMirror(mirror))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }
    @Deprecated(forRemoval = true)
    public final CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title) {
        return createOfflineInventory(playerId, playerName, mainInventoryCreationOptions().withTitle(title))
                .thenApply(response -> response.isSuccess() ? Optional.of(response.getInventory()) : Optional.empty());
    }

    @Deprecated(forRemoval = true)
    public final MainSpectatorInventory spectateInventory(HumanEntity player, String title, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(player, mainInventoryCreationOptions().withTitle(title).withMirror(mirror));
    }
    @Deprecated(forRemoval = true)
    public final MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        return spectateInventory(player, mainInventoryCreationOptions().withTitle(title));
    }


}
