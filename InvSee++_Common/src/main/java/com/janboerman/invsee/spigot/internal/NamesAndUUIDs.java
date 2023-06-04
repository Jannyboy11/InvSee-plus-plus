package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.mojangapi.MojangAPI;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.resolve.*;
import com.janboerman.invsee.utils.CaseInsensitiveMap;
import com.janboerman.invsee.utils.Maybe;
import com.janboerman.invsee.utils.SynchronizedIterator;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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
import java.util.logging.Level;

/**
 * Cache for player usernames and unique IDs.
 * Instance can be obtained via {@link InvseeAPI#namesAndUuidsLookup()}.
 *
 * @apiNote this API is not stable (yet).
 * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Advanced-usage#changing-how-uuid-lookups-work-unstable-api">Changing how UUID lookups work</a>
 */
public class NamesAndUUIDs {

    //static members
    protected static final boolean SPIGOT;
    protected static final boolean PAPER;
    static {
        boolean configExists;
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            configExists = true;
        } catch (ClassNotFoundException e) {
            configExists = false;
        }
        SPIGOT = configExists;

        boolean paperParticleBuilder;
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paperParticleBuilder = true;
        } catch (ClassNotFoundException e) {
            paperParticleBuilder = false;
        }
        PAPER = paperParticleBuilder;
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

    //TODO make order configurable from InvSee++'s config
    /** The list of strategies used to resolve players' unique IDs. Strategies are attempted in order. */
    public final List<UUIDResolveStrategy> uuidResolveStrategies;
    /** The list of strategies used to resolve players' usernames. Strategies are attempted in order. */
    public final List<NameResolveStrategy> nameResolveStrategies;

    private boolean bungeeCord = false, bungeeCordOnline = false;
    private boolean velocity = false, velocityOnline = false;

    /** @deprecated internal api */
    @Deprecated
    public NamesAndUUIDs(Plugin plugin, Scheduler scheduler) {

        MojangAPI mojangApi = new MojangAPI(HttpClient.newBuilder()
                .executor(scheduler::executeAsync)
                .build());

        this.uuidResolveStrategies = Collections.synchronizedList(new ArrayList<>(10));
        this.nameResolveStrategies = Collections.synchronizedList(new ArrayList<>(10));

        this.uuidResolveStrategies.add(new UUIDOnlinePlayerStrategy(plugin.getServer(), scheduler::executeSyncGlobal));
        this.nameResolveStrategies.add(new NameOnlinePlayerStrategy(plugin.getServer(), scheduler::executeSyncGlobal));

        this.uuidResolveStrategies.add(new UUIDInMemoryStrategy(uuidCache));
        this.nameResolveStrategies.add(new NameInMemoryStrategy(userNameCache));

        if (SPIGOT) {
            Configuration spigotConfig = plugin.getServer().spigot().getConfig();
            ConfigurationSection settings = spigotConfig.getConfigurationSection("settings");
            if (settings != null) {
                this.bungeeCord = this.bungeeCordOnline = settings.getBoolean("bungeecord", false); //assume bungee in online mode since Spigot does not specify this (Paper does).
            }
        }

        if (PAPER) {
            this.uuidResolveStrategies.add(new UUIDPaperCacheStrategy(plugin, scheduler));

            try {
                YamlConfiguration paperConfig = plugin.getServer().spigot().getPaperConfig();
                ConfigurationSection proxiesSection = paperConfig.getConfigurationSection("proxies");
                if (proxiesSection != null) {
                    //bungee
                    ConfigurationSection bungeeSection = proxiesSection.getConfigurationSection("bungee-cord");
                    if (bungeeSection != null) {
                        this.bungeeCordOnline = this.bungeeCord && bungeeSection.getBoolean("online-mode", false);
                    }
                    //velocity
                    ConfigurationSection velocitySection = proxiesSection.getConfigurationSection("velocity");
                    if (velocitySection != null) {
                        this.velocity = velocitySection.getBoolean("enabled", false);
                        this.velocityOnline = this.velocity && velocitySection.getBoolean("online-mode", false);
                    }
                }
            } catch (UnsupportedOperationException e/*can happen on Glowstone*/) {
                plugin.getLogger().log(Level.WARNING, "Server acts as if it includes the paper config api, but it actually doesn't!", e);
            }
        }

        this.uuidResolveStrategies.add(new UUIDPermissionPluginStrategy(plugin, scheduler));
        this.nameResolveStrategies.add(new NamePermissionPluginStrategy(plugin, scheduler));

        if (bungeeCord || velocity) {
            this.uuidResolveStrategies.add(new UUIDBungeeCordStrategy(plugin));
            //there is no BungeeCord plugin message subchannel which can get a player name given a uuid.
            //the only way to do that currently is to 'get' a list of all players, and for every player in that list
            //request the uuid. If one matches the argument uuid, then that is the one.
            //this is too expensive for my tastes so I'm not going to implement a NameBungeeCordStrategy for now.
        }

        //these methods 'resolve' names and uuids for players that have never played on the server before.
        //they implement the marker interface NewPlayerSupport.
        if (onlineMode(plugin.getServer())) {
            this.uuidResolveStrategies.add(new UUIDMojangAPIStrategy(plugin, mojangApi));
            this.nameResolveStrategies.add(new NameMojangAPIStrategy(plugin, mojangApi)); //ok, theoretically this could occur if the player already played before.
        } else {
            this.uuidResolveStrategies.add(new UUIDOfflineModeStrategy());
            //how to fake a username given a uuid, in offline mode?
        }
    }

    /** Is the server in online mode? This method will consider servers behind a proxy in online mode as online. */
    public final boolean onlineMode(Server server) {
        return server.getOnlineMode() || bungeeCordOnline || velocityOnline;
    }

    /** Get the known mappings from username to unique ID. */
    public Map<String, UUID> getUuidCache() {
        return uuidCacheView;
    }

    /** Get the known mappings from UUID to username. */
    public Map<UUID, String> getUserNameCache() {
        return userNameCacheView;
    }

    /** Cache a player's unique ID and username. */
    public void cacheNameAndUniqueId(UUID uuid, String userName) {
        this.userNameCache.put(uuid, userName);
        this.uuidCache.put(userName, uuid);
    }

    /** Resolve a player's unique ID, given their username. */
    public CompletableFuture<Optional<UUID>> resolveUUID(String username) {
        var result = resolveUUID(username, new SynchronizedIterator<>(uuidResolveStrategies.iterator()));
        result.thenAccept(optUuid -> optUuid.ifPresent(uuid -> cacheNameAndUniqueId(uuid, username)));
        return result;
    }

    /** Resolve a player's username, given their unique ID. */
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
