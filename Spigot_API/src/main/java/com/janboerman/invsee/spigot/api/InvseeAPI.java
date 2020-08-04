package com.janboerman.invsee.spigot.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public abstract class InvseeAPI {

    private static final CompletableFuture COMPLETED_EMPTY = CompletableFuture.completedFuture(Optional.empty());
    private static final UUIDResolveStrategy NON_RESOLVING_STRATEGY = userName -> (CompletableFuture<Optional<UUID>>) COMPLETED_EMPTY;


    protected final List<UUIDResolveStrategy> uuidResolveStrategies;
    protected final Plugin plugin;

    private final InMemoryStrategy.CaseInsensitiveMap<UUID> cache = new InMemoryStrategy.CaseInsensitiveMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, UUID> eldest) {
            return size() > 200;
        }
    };

    protected InvseeAPI(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.uuidResolveStrategies = new ArrayList<>(3);
        this.uuidResolveStrategies.addAll(List.of(
                new OnlinePlayerStrategy(plugin.getServer()),
                new InMemoryStrategy(cache),
                new MojangAPIStrategy(plugin)));

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
        } catch (ClassNotFoundException ignored) {
        }

        throw new RuntimeException("Unsupported server software. Please run on (a fork of) CraftBukkit.");
    }

    protected final CompletableFuture<Optional<UUID>> resolveUUID(String userName) {
        UUIDResolveStrategy resolveStrategy = NON_RESOLVING_STRATEGY;

        ListIterator<UUIDResolveStrategy> iterator = uuidResolveStrategies.listIterator(uuidResolveStrategies.size());
        while (iterator.hasPrevious()) {
            UUIDResolveStrategy strat = iterator.previous();
            final UUIDResolveStrategy strategy = resolveStrategy;
            resolveStrategy = us -> strat.resolveUUID(us).thenCompose(optionalUuid -> {
                if (optionalUuid.isPresent()) {
                    return CompletableFuture.completedFuture(optionalUuid);
                }

                return strategy.resolveUUID(userName);
            });
        }

        return resolveStrategy.resolveUUID(userName);
    }

    public CompletableFuture<Optional<SpectatorInventory>> createInventory(String userName) {
        return resolveUUID(userName).thenCompose(optUuid -> {
            if (optUuid.isPresent()) {
                UUID uuid = optUuid.get();
                return createInventory(uuid);
            }

            return (CompletableFuture<Optional<SpectatorInventory>>) COMPLETED_EMPTY;
        });
    }

    public abstract CompletableFuture<Optional<SpectatorInventory>> createInventory(UUID player);

    public abstract void saveInventory(SpectatorInventory inventory);


    // ================================== Event Stuff ==================================

    private static final class PlayerListener implements Listener {
        //TODO if a player joins, check whether there is a player that is viewing the offline inventory
        //TODO if so, then set the updates contents to the player's 'online' inventory)

        //TODO if a player leaves, and somebody is editing his inventory, save the inventory when the spectator is done editing


        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            //TODO
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            //TODO
        }

    }

    private static final class InventoryListener implements Listener {
        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            //TODO if the spectated player is offline, save.
        }
    }


}
