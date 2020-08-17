package com.janboerman.invsee.spigot.api;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.logging.Level;

import com.janboerman.invsee.utils.CaseInsensitiveMap;
import com.janboerman.invsee.utils.Rethrow;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public abstract class InvseeAPI {

    protected static final boolean SPIGOT;
    static {
        boolean configExists;
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            configExists = true;
        } catch (ClassNotFoundException e) {
            configExists = false;
        }
        SPIGOT = configExists;
    }
    protected static final CompletableFuture COMPLETED_EMPTY = CompletableFuture.completedFuture(Optional.empty());

    protected final List<UUIDResolveStrategy> uuidResolveStrategies;
    protected final Plugin plugin;
    private final Map<String, UUID> uuidCache = Collections.synchronizedMap(new CaseInsensitiveMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, UUID> eldest) {
            return size() > 200;
        }
    });
    private final Map<String, UUID> uuidCacheView = Collections.unmodifiableMap(uuidCache);

    private final Map<UUID, WeakReference<MainSpectatorInventory>> openInventories = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<String, CompletableFuture<Optional<MainSpectatorInventory>>> pendingInventoriesByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<Optional<MainSpectatorInventory>>> pendingInventoriesByUuid = new ConcurrentHashMap<>();

    private final Map<UUID, WeakReference<EnderSpectatorInventory>> openEnderChests = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<String, CompletableFuture<Optional<EnderSpectatorInventory>>> pendingEnderChestsByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    private final Map<UUID, CompletableFuture<Optional<EnderSpectatorInventory>>> pendingEnderChestsByUuid = new ConcurrentHashMap<>();

    private Function<Player, String> mainSpectatorInvTitleProvider = player -> player.getName() + "'s inventory";
    private Function<Player, String> enderSpectatorInvTitleProvider = player -> player.getName() + "'s enderchest";

    public final Executor serverThreadExecutor;
    public final Executor asyncExecutor;

    protected InvseeAPI(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.uuidResolveStrategies = Collections.synchronizedList(new ArrayList<>(4));
        this.uuidResolveStrategies.add(new InMemoryStrategy(uuidCache));
        if (SPIGOT) {
            Configuration spigotConfig = plugin.getServer().spigot().getConfig();
            ConfigurationSection settings = spigotConfig.getConfigurationSection("settings");
            if (settings != null && settings.getBoolean("bungeecord", false)) {
                this.uuidResolveStrategies.add(new BungeeCordStrategy(plugin));
            }
        }
        this.uuidResolveStrategies.add(new MojangAPIStrategy(plugin));

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), plugin);
        pluginManager.registerEvents(new InventoryListener(), plugin);

        this.serverThreadExecutor = runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable);
        this.asyncExecutor = runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public final void setMainInventoryTitleFactory(Function<Player, String> titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.mainSpectatorInvTitleProvider = titleFactory;
    }

    public void setEnderInventoryTitleFactory(Function<Player, String> titleFactory) {
        Objects.requireNonNull(titleFactory);
        this.enderSpectatorInvTitleProvider = titleFactory;
    }

    public static InvseeAPI setup(Plugin plugin) {
        Server server = plugin.getServer();

        try {
            Constructor<?> ctor = null;

            if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_16_R1.CraftServer")) {
                ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_16_R1.InvseeImpl").getConstructor(Plugin.class);
            } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_16_R2.CraftServer")) {
                ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_16_R2.InvseeImpl").getConstructor(Plugin.class);
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

    private CompletableFuture<Optional<UUID>> resolveUUID(String username, Iterator<UUIDResolveStrategy> strategies) {
        if (strategies.hasNext()) {
            UUIDResolveStrategy strategy = strategies.next();
            return strategy.resolveUUID(username).thenCompose((Optional<UUID> optionalUuid) -> {
                if (optionalUuid.isPresent()) {
                    uuidCache.put(username, optionalUuid.get());
                    return CompletableFuture.completedFuture(optionalUuid);
                } else {
                    return resolveUUID(username, strategies);
                }
            });
        } else {
            return (CompletableFuture<Optional<UUID>>) COMPLETED_EMPTY;
        }
    }

    //TODO uuid->username resolving?!

    public Map<String, UUID> getUuidCache() {
        return uuidCacheView;
    }

    protected CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        return resolveUUID(userName, uuidResolveStrategies.iterator());
    }

    public abstract MainSpectatorInventory spectateInventory(HumanEntity player, String title);
    protected abstract CompletableFuture<Optional<MainSpectatorInventory>> createOfflineInventory(UUID playerId, String playerName, String title);
    protected abstract CompletableFuture<Void> saveInventory(MainSpectatorInventory inventory);

    public abstract EnderSpectatorInventory spectateEnderChest(HumanEntity player, String title);
    protected abstract CompletableFuture<Optional<EnderSpectatorInventory>> createOfflineEnderChest(UUID playerId, String playerName, String title);
    protected abstract CompletableFuture<Void> saveEnderChest(EnderSpectatorInventory enderChest);

    public CompletableFuture<Optional<MainSpectatorInventory>> spectateInventory(String userName, String title) {
        Objects.requireNonNull(userName, "userName cannot be null!");

        //try online
        Player target = plugin.getServer().getPlayerExact(userName);
        if (target != null) {
            MainSpectatorInventory spectatorInventory = spectateInventory(target, title);
            UUID uuid = target.getUniqueId();
            uuidCache.put(userName, uuid);
            openInventories.put(uuid, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(Optional.of(spectatorInventory));
        }

        //try offline
        var future = resolveUUID(userName).thenCompose(optUuid -> {
            if (optUuid.isPresent()) {
                UUID uuid = optUuid.get();
                return spectateInventory(uuid, userName, title);
            }

            return (CompletableFuture<Optional<MainSpectatorInventory>>) COMPLETED_EMPTY;
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingInventoriesByName.put(userName, future);
        future.whenComplete((result, error) -> pendingInventoriesByName.remove(userName));
        return future;
    }

    public final CompletableFuture<Optional<MainSpectatorInventory>> spectateInventory(UUID playerId, String playerName, String title) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");

        //try cache
        WeakReference<MainSpectatorInventory> alreadyOpen = openInventories.get(playerId);
        if (alreadyOpen != null) {
            MainSpectatorInventory inv = alreadyOpen.get();
            if (inv != null) {
                return CompletableFuture.completedFuture(Optional.of(inv));
            }
        }

        //try online
        Player target = plugin.getServer().getPlayer(playerId);
        if (target != null) {
            MainSpectatorInventory spectatorInventory = spectateInventory(target, title);
            openInventories.put(playerId, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(Optional.of(spectatorInventory));
        }

        //try offline
        var future = createOfflineInventory(playerId, playerName, title).thenApply(optionalInv -> {
            optionalInv.ifPresent(inv -> openInventories.put(playerId, new WeakReference<>(inv)));
            return optionalInv;
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingInventoriesByUuid.put(playerId, future);
        future.whenComplete((result, error) -> pendingInventoriesByUuid.remove(playerId));
        return future;
    }

    public CompletableFuture<Optional<EnderSpectatorInventory>> spectateEnderChest(String userName, String title) {
        Objects.requireNonNull(userName, "userName cannot be null!");

        //try online
        Player target = plugin.getServer().getPlayerExact(userName);
        if (target != null) {
            EnderSpectatorInventory spectatorInventory = spectateEnderChest(target, title);
            UUID uuid = target.getUniqueId();
            uuidCache.put(userName, uuid);
            openEnderChests.put(uuid, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(Optional.of(spectatorInventory));
        }

        //try offline
        var future = resolveUUID(userName).thenCompose(optUuid -> {
            if (optUuid.isPresent()) {
                UUID uuid = optUuid.get();
                return spectateEnderChest(uuid, userName, title);
            }

            return (CompletableFuture<Optional<EnderSpectatorInventory>>) COMPLETED_EMPTY;
        }).handleAsync((success, error) -> {
            if (error == null) return success;
            return Rethrow.unchecked(error);
        }, serverThreadExecutor);
        pendingEnderChestsByName.put(userName, future);
        future.whenComplete((result, error) -> pendingEnderChestsByName.remove(userName));
        return future;
    }

    public final CompletableFuture<Optional<EnderSpectatorInventory>> spectateEnderChest(UUID playerId, String playerName, String title) {
        Objects.requireNonNull(playerId, "player UUID cannot be null!");
        Objects.requireNonNull(playerName, "player name cannot be null!");

        //try cache
        WeakReference<EnderSpectatorInventory> alreadyOpen = openEnderChests.get(playerId);
        if (alreadyOpen != null) {
            EnderSpectatorInventory inv = alreadyOpen.get();
            if (inv != null) {
                return CompletableFuture.completedFuture(Optional.of(inv));
            }
        }

        //try online
        Player target = plugin.getServer().getPlayer(playerId);
        if (target != null) {
            EnderSpectatorInventory spectatorInventory = spectateEnderChest(target, title);
            openEnderChests.put(playerId, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(Optional.of(spectatorInventory));
        }

        //try offline
        var future = createOfflineEnderChest(playerId, playerName, title).thenApply(optionalInv -> {
            optionalInv.ifPresent(inv -> openEnderChests.put(playerId, new WeakReference<>(inv)));
            return optionalInv;
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

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String userName = player.getName();

            MainSpectatorInventory newInventorySpectator = null;
            String mainTitle = mainSpectatorInvTitleProvider.apply(player);
            EnderSpectatorInventory newEnderSpectator = null;
            String enderTitle = enderSpectatorInvTitleProvider.apply(player);

            //check if somebody was looking up the player and make sure they get the player's live inventory
            CompletableFuture<Optional<MainSpectatorInventory>> mainInvNameFuture = pendingInventoriesByName.remove(userName);
            if (mainInvNameFuture != null) mainInvNameFuture.complete(Optional.of(newInventorySpectator = spectateInventory(player, mainTitle)));
            CompletableFuture<Optional<MainSpectatorInventory>> mainInvUuidFuture = pendingInventoriesByUuid.remove(uuid);
            if (mainInvUuidFuture != null) mainInvUuidFuture.complete(Optional.of(newInventorySpectator != null ? newInventorySpectator : (newInventorySpectator = spectateInventory(player, mainTitle))));
            CompletableFuture<Optional<EnderSpectatorInventory>> enderNameFuture = pendingEnderChestsByName.remove(userName);
            if (enderNameFuture != null) enderNameFuture.complete(Optional.of(newEnderSpectator = spectateEnderChest(player, enderTitle)));
            CompletableFuture<Optional<EnderSpectatorInventory>> enderUuidFuture = pendingEnderChestsByUuid.remove(uuid);
            if (enderUuidFuture != null) enderUuidFuture.complete(Optional.of(newEnderSpectator != null ? newEnderSpectator : (newEnderSpectator = spectateEnderChest(player, enderTitle))));

            //check if somebody was looking in the offline inventory and update player's inventory.
            for (Player online : player.getServer().getOnlinePlayers()) {
                Inventory topInventory = online.getOpenInventory().getTopInventory();
                if (topInventory instanceof MainSpectatorInventory) {
                    MainSpectatorInventory oldSpectatorInventory = (MainSpectatorInventory) topInventory;
                    if (oldSpectatorInventory.getSpectatedPlayerId().equals(uuid)) {
                        if (newInventorySpectator == null) {
                            newInventorySpectator = spectateInventory(player, mainTitle);
                            //this also updates the player's inventory! (because they are backed by the same NonNullList<ItemStacks>s)
                            newInventorySpectator.setStorageContents(oldSpectatorInventory.getStorageContents());
                            newInventorySpectator.setArmourContents(oldSpectatorInventory.getArmourContents());
                            newInventorySpectator.setOffHandContents(oldSpectatorInventory.getOffHandContents());
                        }

                        online.closeInventory();
                        online.openInventory(newInventorySpectator);
                    }
                } else if (topInventory instanceof EnderSpectatorInventory) {
                    EnderSpectatorInventory oldSpectatorInventory = (EnderSpectatorInventory) topInventory;
                    if (oldSpectatorInventory.getSpectatedPlayerId().equals(uuid)) {
                        if (newEnderSpectator == null) {
                            //this also update the player's enderchest! (because they are backed by the same NonNullList<ItemStack>)
                            newEnderSpectator = spectateEnderChest(player, enderTitle);
                            newEnderSpectator.setStorageContents(oldSpectatorInventory.getStorageContents());
                        }
                    }

                    online.closeInventory();
                    online.openInventory(newEnderSpectator);
                }
            }

            uuidCache.remove(userName, uuid);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            uuidCache.put(player.getName(), uuid);

            WeakReference<MainSpectatorInventory> invRef = openInventories.get(uuid);
            if (invRef != null) {
                MainSpectatorInventory inv = invRef.get();
                if (inv != null) {
                    boolean open = false;
                    for (Player online : player.getServer().getOnlinePlayers()) {
                        if (online.getOpenInventory().getTopInventory() == inv) {
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
                        if (online.getOpenInventory().getTopInventory() == ender) {
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

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            Inventory inventory = event.getInventory();
            if (inventory instanceof MainSpectatorInventory) {
                MainSpectatorInventory spectatorInventory = (MainSpectatorInventory) inventory;
                if (event.getPlayer().getServer().getPlayer(spectatorInventory.getSpectatedPlayerId()) == null) {
                    //spectated player is no longer online
                    saveInventory(spectatorInventory).exceptionally(throwable -> {
                        plugin.getLogger().log(Level.SEVERE, "Error while saving offline inventory", throwable);
                        event.getPlayer().sendMessage(ChatColor.RED + "Something went wrong when trying to save the inventory.");
                        return null;
                    });
                }
            } else if (inventory instanceof EnderSpectatorInventory) {
                EnderSpectatorInventory spectatorInventory = (EnderSpectatorInventory) inventory;
                if (event.getPlayer().getServer().getPlayer(spectatorInventory.getSpectatedPlayerId()) == null) {
                    //spectated player is no longer online
                    saveEnderChest(spectatorInventory).exceptionally(throwable -> {
                        plugin.getLogger().log(Level.SEVERE, "Error while saving offline enderchest", throwable);
                        event.getPlayer().sendMessage(ChatColor.RED + "Something went wrong when trying to save the enderchest.");
                        return null;
                    });
                }
            }
        }

    }

}
