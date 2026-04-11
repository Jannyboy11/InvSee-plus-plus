package com.janboerman.invsee.spigot.internal;

import com.janboerman.invsee.mojangapi.ElectroidAPI;
import com.janboerman.invsee.mojangapi.MojangAPI;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.Scheduler;
import com.janboerman.invsee.spigot.api.resolve.*;
import com.janboerman.invsee.spigot.internal.resolve.ResolveStrategyType;
import com.janboerman.invsee.utils.CaseInsensitiveMap;
import com.janboerman.invsee.utils.Maybe;
import com.janboerman.invsee.utils.SynchronizedIterator;

import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.Map.Entry;
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

    private final Map<String, UUID> uuidCache = Collections.synchronizedMap(new CaseInsensitiveMap<UUID>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, UUID> eldest) {
            return size() > 200;
        }
    });
    private final Map<String, UUID> uuidCacheView = Collections.unmodifiableMap(uuidCache);
    private final Map<UUID, String> userNameCache = Collections.synchronizedMap(new LinkedHashMap<UUID, String>() {
        @Override
        protected boolean removeEldestEntry(Entry<UUID, String> eldest) {
            return size() > 200;
        }
    });
    private final Map<UUID, String> userNameCacheView = Collections.unmodifiableMap(userNameCache);

    private final Plugin plugin;
    private final Scheduler scheduler;

    /** The list of strategies used to resolve players' unique IDs. Strategies are attempted in order. */
    public final List<UUIDResolveStrategy> uuidResolveStrategies;
    /** The list of strategies used to resolve players' usernames. Strategies are attempted in order. */
    public final List<NameResolveStrategy> nameResolveStrategies;

    private boolean bungeeCord = false, bungeeCordOnline = false;
    private boolean velocity = false, velocityOnline = false;

    final List<ResolveStrategyType> uuidResolveTypes;
    final List<ResolveStrategyType> nameResolveTypes;

    private final EnumMap<ResolveStrategyType, UUIDResolveStrategy> availableUuidResolveStrategies = new EnumMap<>(ResolveStrategyType.class);
    private final EnumMap<ResolveStrategyType, NameResolveStrategy> availableNameResolveStrategies = new EnumMap<>(ResolveStrategyType.class);

    /** @deprecated internal api */
    @Deprecated
    public NamesAndUUIDs(Plugin plugin, Scheduler scheduler) {
        Server server = plugin.getServer();
        this.plugin = plugin;
        this.scheduler = scheduler;

        if (SPIGOT) {
            Configuration spigotConfig = plugin.getServer().spigot().getConfig();
            ConfigurationSection settings = spigotConfig.getConfigurationSection("settings");
            if (settings != null) {
                this.bungeeCord = this.bungeeCordOnline = settings.getBoolean("bungeecord", false); //assume bungee in online mode since Spigot does not specify this (Paper does).
            }
        }

        if (PAPER) {
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
                plugin.getLogger().log(Level.WARNING, "Server acts as if it supports the paper config api, but it actually doesn't!", e);
            }
        }

        this.uuidResolveStrategies = Collections.synchronizedList(new ArrayList<>());
        this.nameResolveStrategies = Collections.synchronizedList(new ArrayList<>());

        this.uuidResolveTypes = ResolveStrategyType.defaultStrategies(onlineMode(server));
        this.nameResolveTypes = ResolveStrategyType.defaultStrategies(onlineMode(server));

        // register available resolve strategies.
        availableUuidResolveStrategies.put(ResolveStrategyType.ONLINE_PLAYER, new UUIDOnlinePlayerStrategy(server, scheduler::executeSyncGlobal));
        availableNameResolveStrategies.put(ResolveStrategyType.ONLINE_PLAYER, new NameOnlinePlayerStrategy(server, scheduler::executeSyncGlobal));

        availableUuidResolveStrategies.put(ResolveStrategyType.LOGGED_OUT_PLAYERS_CACHE, new UUIDInMemoryStrategy(uuidCache));
        availableNameResolveStrategies.put(ResolveStrategyType.LOGGED_OUT_PLAYERS_CACHE, new NameInMemoryStrategy(userNameCache));

        if (PAPER) {
            availableUuidResolveStrategies.put(ResolveStrategyType.PAPER_OFFLINE_PLAYER_CACHE, new UUIDPaperCacheStrategy(plugin, scheduler));
            // Paper does not provide Server#getOfflinePlayerIfCached with a UUID argument.. >:(
        }

        availableUuidResolveStrategies.put(ResolveStrategyType.PERMISSION_PLUGIN, new UUIDPermissionPluginStrategy(plugin, scheduler));
        availableNameResolveStrategies.put(ResolveStrategyType.PERMISSION_PLUGIN, new NamePermissionPluginStrategy(plugin, scheduler));

        if (bungeeCord || velocity) {
            availableUuidResolveStrategies.put(ResolveStrategyType.PROXY, new UUIDBungeeCordStrategy(plugin, scheduler));
            //there is no BungeeCord plugin message subchannel which can get a player name given a uuid.
            //the only way to do that currently is to 'get' a list of all players, and for every player in that list
            //request the uuid. If one matches the argument uuid, then that is the one.
            //this is too expensive for my tastes so I'm not going to implement a NameBungeeCordStrategy for now.
        }

        if (onlineMode(server)) {
            MojangAPI mojangApi = new MojangAPI(scheduler::executeAsync);
            ElectroidAPI electroidAPI = new ElectroidAPI(scheduler::executeAsync);

            availableUuidResolveStrategies.put(ResolveStrategyType.MOJANG_REST_API_CALL, new UUIDMojangAPIStrategy(plugin, mojangApi));
            availableNameResolveStrategies.put(ResolveStrategyType.MOJANG_REST_API_CALL, new NameMojangAPIStrategy(plugin, mojangApi));

            availableUuidResolveStrategies.put(ResolveStrategyType.ELECTROID_REST_API_CALL, new UUIDElectroidAPIStrategy(plugin, electroidAPI));
            availableNameResolveStrategies.put(ResolveStrategyType.ELECTROID_REST_API_CALL, new NameElectroidAPIStrategy(plugin, electroidAPI));
        } else {
            availableUuidResolveStrategies.put(ResolveStrategyType.SPOOF, new UUIDOfflineModeStrategy());
        }
    }

    // TODO get from config, if available.
    public void setUuidResolveTypes(List<ResolveStrategyType> resolveTypes) {
        this.uuidResolveTypes.clear();
        this.uuidResolveTypes.addAll(resolveTypes);
    }

    // TODO get from config, if available.
    public void setNameResolveTypes(List<ResolveStrategyType> resolveTypes) {
        this.nameResolveTypes.clear();
        this.nameResolveTypes.addAll(resolveTypes);
    }

    public void addUuidResolveStrategy(ResolveStrategyType resolveType, UUIDResolveStrategy strategy) {
        availableUuidResolveStrategies.put(resolveType, strategy);
    }

    public void addNameResolveType(ResolveStrategyType resolveTypes, NameResolveStrategy strategy) {
        availableNameResolveStrategies.put(resolveTypes, strategy);
    }

    public void materialiseUsernameAndUniqueIdResolveStrategies() {
        resetResolveStrategies(uuidResolveStrategies, uuidResolveTypes, availableUuidResolveStrategies);
        resetResolveStrategies(nameResolveStrategies, nameResolveTypes, availableNameResolveStrategies);
    }

    private static <T> void resetResolveStrategies(List<T> receiver, List<ResolveStrategyType> resolveTypes, Map<ResolveStrategyType, T> available) {
        receiver.clear();
        for (ResolveStrategyType resolveStrategyType : resolveTypes) {
            T strategy = available.get(resolveStrategyType);
            if (strategy != null) {
                receiver.add(strategy);
            }
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
        CompletableFuture<Optional<UUID>> result = resolveUUID(username, new SynchronizedIterator<>(uuidResolveStrategies.iterator()));
        result.thenAccept(optUuid -> optUuid.ifPresent(uuid -> cacheNameAndUniqueId(uuid, username)));
        return result;
    }

    /** Resolve a player's username, given their unique ID. */
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        CompletableFuture<Optional<String>> result = resolveUserName(uniqueId, new SynchronizedIterator<>(nameResolveStrategies.iterator()));
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
