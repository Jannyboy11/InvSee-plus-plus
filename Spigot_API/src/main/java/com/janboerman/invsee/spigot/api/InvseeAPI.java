package com.janboerman.invsee.spigot.api;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;

import com.janboerman.invsee.utils.CaseInsensitiveMap;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    private final Map<UUID, WeakReference<SpectatorInventory>> openInventories = Collections.synchronizedMap(new WeakHashMap<>());

    private final Map<String, UUID> cache = Collections.synchronizedMap(new CaseInsensitiveMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, UUID> eldest) {
            return size() > 200;
        }
    });

    protected InvseeAPI(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.uuidResolveStrategies = Collections.synchronizedList(new ArrayList<>(4));
        this.uuidResolveStrategies.add(new InMemoryStrategy(cache));
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
    }

    public static InvseeAPI setup(Plugin plugin) {
        Server server = plugin.getServer();

        try {
            Constructor<?> ctor = null;

            if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_16_R1.CraftServer")) {
                ctor = Class.forName("com.janboerman.invsee.spigot.impl_1_16.InvseeImpl").getConstructor(Plugin.class);
            } //make a bunch of else-ifs here for future minecraft versions.

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
                    cache.put(username, optionalUuid.get());
                    return CompletableFuture.completedFuture(optionalUuid);
                } else {
                    return resolveUUID(username, strategies);
                }
            });
        } else {
            return (CompletableFuture<Optional<UUID>>) COMPLETED_EMPTY;
        }
    }

    protected CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        return resolveUUID(userName, uuidResolveStrategies.iterator());
    }

    public abstract SpectatorInventory spectate(HumanEntity player);

    protected abstract CompletableFuture<Optional<SpectatorInventory>> createOfflineInventory(UUID player);

    protected abstract CompletableFuture<Void> saveInventory(SpectatorInventory inventory);

    public CompletableFuture<Optional<SpectatorInventory>> spectate(String userName) {
        Objects.requireNonNull(userName, "userName cannot be null!");

        //try online
        Player target = plugin.getServer().getPlayerExact(userName);
        if (target != null) {
            SpectatorInventory spectatorInventory = spectate(target);
            UUID uuid = target.getUniqueId();
            cache.put(userName, uuid);
            openInventories.put(uuid, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(Optional.of(spectatorInventory));
        }

        //try offline
        return resolveUUID(userName).thenCompose(optUuid -> {
            if (optUuid.isPresent()) {
                UUID uuid = optUuid.get();
                return spectate(uuid);
            }

            return (CompletableFuture<Optional<SpectatorInventory>>) COMPLETED_EMPTY;
        }).thenApplyAsync(Function.identity(), runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    public final CompletableFuture<Optional<SpectatorInventory>> spectate(UUID player) {
        Objects.requireNonNull(player, "player UUID cannot be null!");

        //try cache
        WeakReference<SpectatorInventory> alreadyOpen = openInventories.get(player);
        if (alreadyOpen != null) {
            SpectatorInventory inv = alreadyOpen.get();
            if (inv != null) {
                return CompletableFuture.completedFuture(Optional.of(inv));
            }
        }

        //try online
        Player target = plugin.getServer().getPlayer(player);
        if (target != null) {
            SpectatorInventory spectatorInventory = spectate(target);
            openInventories.put(player, new WeakReference<>(spectatorInventory));
            return CompletableFuture.completedFuture(Optional.of(spectatorInventory));
        }

        //try offline
        return createOfflineInventory(player).thenApply(optionalInv -> {
            optionalInv.ifPresent(inv -> openInventories.put(player, new WeakReference<>(inv)));
            return optionalInv;
        }).thenApplyAsync(Function.identity(), runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));
    }


    // ================================== Event Stuff ==================================

    private final class PlayerListener implements Listener {

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            
            //TODO in theory there is a race going on here; there might be a spectator waiting for the offline inventory already.
            //TODO so I need to cache the future in a Map<String, CompletableFuture<Optional<SpectatorInventory>>> and if it's still present
            //TODO then I need to complete (or obtrude?) the value.

            SpectatorInventory newSpectatorInventory = null;
            for (Player online : player.getServer().getOnlinePlayers()) {
                if (online.getOpenInventory().getTopInventory() instanceof SpectatorInventory) {
                    SpectatorInventory oldSpectatorInventory = (SpectatorInventory) online.getOpenInventory().getTopInventory();
                    if (oldSpectatorInventory.getSpectatedPlayer().equals(uuid)) {
                        if (newSpectatorInventory == null) {
                            newSpectatorInventory = spectate(player);
                            //this also updates the player's inventory! (because they are backed by the same NonNullList<ItemStacks>s)
                            newSpectatorInventory.setStorageContents(oldSpectatorInventory.getStorageContents());
                            newSpectatorInventory.setArmourContents(oldSpectatorInventory.getArmourContents());
                            newSpectatorInventory.setOffHandContents(oldSpectatorInventory.getOffHandContents());
                        }

                        online.closeInventory();
                        online.openInventory(newSpectatorInventory);
                    }
                }
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            WeakReference<SpectatorInventory> ref = openInventories.get(uuid);
            if (ref != null) {
                SpectatorInventory inv = ref.get();
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
        }

    }

    private final class InventoryListener implements Listener {
        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (event.getInventory() instanceof SpectatorInventory) {
                SpectatorInventory spectatorInventory = (SpectatorInventory) event.getInventory();
                if (event.getPlayer().getServer().getPlayer(spectatorInventory.getSpectatedPlayer()) == null) {
                    saveInventory(spectatorInventory).exceptionally(throwable -> {
                        plugin.getLogger().log(Level.SEVERE, "Error while saving offline inventory", throwable);
                        event.getPlayer().sendMessage(ChatColor.RED + "Something went wrong when trying to save the inventory");
                        return null;
                    });
                }
            }
        }
    }

}
