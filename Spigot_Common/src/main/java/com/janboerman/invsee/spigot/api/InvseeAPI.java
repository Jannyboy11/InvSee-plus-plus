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

    //TODO actually use these, also create setters!
    private Mirror<PlayerInventorySlot> inventoryMirror = Mirror.defaultPlayerInventory();
    private Mirror<EnderChestSlot> enderChestSlotMirror = Mirror.defaultEnderChest();

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

    public void unregisterListeners() {
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


    //TODO for future compat: create a class CreationOptions (which includes Title and Mirror)?

    public MainSpectatorInventory spectateInventory(HumanEntity player, String title, Mirror<PlayerInventorySlot> mirror) {
        return spectateInventory(player, title);
    }
    @Deprecated
    public MainSpectatorInventory spectateInventory(HumanEntity player, String title) {
        return spectateInventory(player, title, Mirror.defaultPlayerInventory());
    }
    public abstract CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title);
    public abstract CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory);

    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title, Mirror<EnderChestSlot> mirror) {
        return spectateEnderChest(player, title);
    }

    @Deprecated
    public EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title) {
        return spectateEnderChest(player, title, Mirror.defaultEnderChest());
    }
    public abstract CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title);
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
        return mainSpectatorInventory(targetName, title, true);
    }

    public CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");

        //try online
        final Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (exempt.isExemptedFromHavingMainInventorySpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            openInventories.put(uuid, new WeakReference<>(spectatorInventory));
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
                return mainSpectatorInventory(uuid, targetName, title);
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
        return mainSpectatorInventory(playerId, playerName, title, true);
    }

    public final CompletableFuture<SpectateResponse<MainSpectatorInventory>> mainSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
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
            if (exempt.isExemptedFromHavingMainInventorySpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            MainSpectatorInventory spectatorInventory = spectateInventory(targetPlayer, title);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            openInventories.put(playerId, new WeakReference<>(spectatorInventory));
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
                return createOfflineInventory(playerId, playerName, title).thenApply(maybeInventory -> {
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
                openInventories.put(playerId, new WeakReference<>(inventory));
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
        return enderSpectatorInventory(targetName, title, true);
    }

    public CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(String targetName, String title, boolean offlineSupport) {
        Objects.requireNonNull(targetName, "targetName cannot be null!");

        //try online
        Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        Target target;
        if (targetPlayer != null) {
            target = Target.byPlayer(targetPlayer);
            if (exempt.isExemptedFromHavingEnderchestSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, title);
            UUID uuid = targetPlayer.getUniqueId();
            lookup.cacheNameAndUniqueId(uuid, targetName);
            openEnderChests.put(uuid, new WeakReference<>(spectatorInventory));
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
                return enderSpectatorInventory(uuid, targetName, title);
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
        return enderSpectatorInventory(playerId, playerName, title, true);
    }

    public final CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderSpectatorInventory(UUID playerId, String playerName, String title, boolean offlineSupport) {
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
            if (exempt.isExemptedFromHavingEnderchestSpectated(target))
                return CompletableFuture.completedFuture(SpectateResponse.fail(NotCreatedReason.targetHasExemptPermission(target)));

            EnderSpectatorInventory spectatorInventory = spectateEnderChest(targetPlayer, title);
            lookup.cacheNameAndUniqueId(playerId, playerName);
            openEnderChests.put(playerId, new WeakReference<>(spectatorInventory));
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
                return createOfflineEnderChest(playerId, playerName, title).thenApply(maybeInventory -> {
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
                openEnderChests.put(playerId, new WeakReference<>(inv));
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
                        var targetDoesNotExist = (TargetDoesNotExist) reason;
                        spectator.sendMessage(ChatColor.RED + "Player " + targetDoesNotExist.getTarget() + " does not exist.");
                    } else if (reason instanceof TargetHasExemptPermission) {
                        var targetHasExemptPermission = (TargetHasExemptPermission) reason;
                        spectator.sendMessage(ChatColor.RED + "Player " + targetHasExemptPermission.getTarget() + " is exempted from being spectated.");
                    } else if (reason instanceof ImplementationFault) {
                        var implementationFault = (ImplementationFault) reason;
                        spectator.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + implementationFault.getTarget() + "'s inventory.");
                    } else if (reason instanceof OfflineSupportDisabled) {
                        spectator.sendMessage(ChatColor.RED + "Spectating offline players' inventories is disabled.");
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
                        var targetDoesNotExist = (TargetDoesNotExist) reason;
                        spectator.sendMessage(ChatColor.RED + "Player " + targetDoesNotExist.getTarget() + " does not exist.");
                    } else if (reason instanceof TargetHasExemptPermission) {
                        var targetHasExemptPermission = (TargetHasExemptPermission) reason;
                        spectator.sendMessage(ChatColor.RED + "Player " + targetHasExemptPermission.getTarget() + " is exempted from being spectated.");
                    } else if (reason instanceof ImplementationFault) {
                        var implementationFault = (ImplementationFault) reason;
                        spectator.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + implementationFault.getTarget() + "'s enderchest.");
                    } else if (reason instanceof OfflineSupportDisabled) {
                        spectator.sendMessage(ChatColor.RED + "Spectating offline players' enderchests is disabled.");
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
							
							//TODO instead of re-opening the inventory, couldn't we just 'set' the top inventory of the Container(/InventoryView)? This is possible since we implement our own anyway!
							//TODO maybe we can even 'set' the nms inventory instance within the bukkit wrapper.
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

							//Idem!
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
