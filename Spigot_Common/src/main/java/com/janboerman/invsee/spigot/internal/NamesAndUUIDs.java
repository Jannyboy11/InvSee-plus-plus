package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.mojangapi.MojangAPI;
import com.janboerman.invsee.spigot.api.resolve.*;
import com.janboerman.invsee.utils.CaseInsensitiveMap;
import com.janboerman.invsee.utils.Maybe;
import com.janboerman.invsee.utils.SynchronizedIterator;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NamesAndUUIDs {

    //static members
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

    private final Map<String, UUID> uuidCache = Collections.synchronizedMap(new CaseInsensitiveMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, UUID> eldest) {
            return size() > 200;
        }
    });
    private final Map<String, UUID> uuidCacheView = Collections.unmodifiableMap(uuidCache);
    private final Map<UUID, String> userNameCache = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Entry<UUID, String> eldest) {
            return size() > 200;
        }
    });
    private final Map<UUID, String> userNameCacheView = Collections.unmodifiableMap(userNameCache);

    public final List<UUIDResolveStrategy> uuidResolveStrategies;
    public final List<NameResolveStrategy> nameResolveStrategies;

    public NamesAndUUIDs(Plugin plugin) {

        Executor asyncExecutor = runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);

        MojangAPI mojangApi = new MojangAPI(HttpClient.newBuilder()
                .executor(asyncExecutor)
                .build());

        this.uuidResolveStrategies = Collections.synchronizedList(new ArrayList<>(5));
        this.nameResolveStrategies = Collections.synchronizedList(new ArrayList<>(5));

        this.uuidResolveStrategies.add(new UUIDOnlinePlayerStrategy(plugin.getServer()));
        this.nameResolveStrategies.add(new NameOnlinePlayerStrategy(plugin.getServer()));

        this.uuidResolveStrategies.add(new UUIDInMemoryStrategy(uuidCache));
        this.nameResolveStrategies.add(new NameInMemoryStrategy(userNameCache));

        if (SPIGOT) {
            Configuration spigotConfig = plugin.getServer().spigot().getConfig();
            ConfigurationSection settings = spigotConfig.getConfigurationSection("settings");
            if (settings != null && settings.getBoolean("bungeecord", false)) {
                this.uuidResolveStrategies.add(new UUIDBungeeCordStrategy(plugin));
                //there is no BungeeCord plugin message subchannel which can get a player name given a uuid.
                //the only way to do that currently is to 'get' a list of all players, and for every player in that list
                //request the uuid. If one matches the argument uuid, then that is the one.
                //this is too expensive for my tastes so I'm not going to implement a NameBungeeCordStrategy for now.
            }
        }

        if (plugin.getServer().getOnlineMode()) {
            this.uuidResolveStrategies.add(new UUIDMojangAPIStrategy(plugin, mojangApi));
            this.nameResolveStrategies.add(new NameMojangAPIStrategy(plugin, mojangApi));
        } //TODO else: add strategy that calculates offline uuid based on username.
    }

    public Map<String, UUID> getUuidCache() {
        return uuidCacheView;
    }

    public Map<UUID, String> getUserNameCache() {
        return userNameCacheView;
    }

    public void cacheNameAndUniqueId(UUID uuid, String userName) {
        this.userNameCache.put(uuid, userName);
        this.uuidCache.put(userName, uuid);
    }

    public CompletableFuture<Optional<UUID>> resolveUUID(String username) {
        var result = resolveUUID(username, new SynchronizedIterator<>(uuidResolveStrategies.iterator()));
        result.thenAccept(optUuid -> optUuid.ifPresent(uuid -> cacheNameAndUniqueId(uuid, username)));
        return result;
    }

    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        var result = resolveUserName(uniqueId, new SynchronizedIterator<>(nameResolveStrategies.iterator()));
        result.thenAccept(optName -> optName.ifPresent(name -> cacheNameAndUniqueId(uniqueId, name)));
        return result;
    }

    private static CompletableFuture<Optional<UUID>> resolveUUID(String userName, SynchronizedIterator<UUIDResolveStrategy> strategies) {
        Maybe<UUIDResolveStrategy> maybeStrat = strategies.moveNext();
        if (!maybeStrat.isPresent()) return CompletedEmpty.the();

        UUIDResolveStrategy strategy = maybeStrat.get();

        return strategy.resolveUniqueId(userName).thenCompose((Optional<UUID> optionalUuid) -> {
            if (optionalUuid.isPresent()) return CompletableFuture.completedFuture(optionalUuid);
            return resolveUUID(userName, strategies);
        });
    }

    private static CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId, SynchronizedIterator<NameResolveStrategy> strategies) {
        Maybe<NameResolveStrategy> maybeStrat = strategies.moveNext();
        if (!maybeStrat.isPresent()) return CompletedEmpty.the();

        NameResolveStrategy strategy = maybeStrat.get();

        return strategy.resolveUserName(uniqueId).thenCompose((Optional<String> optionalName) -> {
            if (optionalName.isPresent()) {
                return CompletableFuture.completedFuture(optionalName);
            } else {
                return resolveUserName(uniqueId, strategies);
            }
        });
    }

}
