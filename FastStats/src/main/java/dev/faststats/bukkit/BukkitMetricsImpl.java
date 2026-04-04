package dev.faststats.bukkit;

import com.google.gson.JsonObject;
import com.janboerman.invsee.utils.Compat;
import dev.faststats.core.SimpleMetrics;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;

final class BukkitMetricsImpl extends SimpleMetrics implements BukkitMetrics {
    private final Plugin plugin;

    private final String pluginVersion;
    private final String minecraftVersion;
    private final String serverType;

    @SuppressWarnings({"deprecation", "Convert2MethodRef"})
    private BukkitMetricsImpl(final Factory factory, final Plugin plugin, final Path config) throws IllegalStateException {
        super(factory, config);

        this.plugin = plugin;
        final Server server = plugin.getServer();

        this.pluginVersion = tryOrEmpty(() -> plugin.getPluginMeta().getVersion())
                .orElseGet(() -> plugin.getDescription().getVersion());
        this.minecraftVersion = Compat.optionalOr(tryOrEmpty(() -> server.getMinecraftVersion()),
                () -> tryOrEmpty(() -> server.getBukkitVersion().split("-", 2)[0]))
                .orElseGet(() -> server.getVersion().split("\\(MC: |\\)", 3)[1]);
        this.serverType = server.getName();

        startSubmitting();
    }

    Plugin plugin() {
        return plugin;
    }

    private boolean checkOnlineMode() {
        final Server server = plugin.getServer();
        return Compat.optionalOr(
                tryOrEmpty(() -> server.getServerConfig().isProxyOnlineMode()),
                () -> tryOrEmpty(this::isProxyOnlineMode)
                ).orElseGet(server::getOnlineMode);
    }


    @SuppressWarnings("removal")
    private boolean isProxyOnlineMode() {
        final Server server = plugin.getServer();
        final ConfigurationSection proxies = server.spigot().getPaperConfig().getConfigurationSection("proxies");
        if (proxies == null) return false;

        if (proxies.getBoolean("velocity.enabled") && proxies.getBoolean("velocity.online-mode")) return true;

        final ConfigurationSection settings = server.spigot().getSpigotConfig().getConfigurationSection("settings");
        if (settings == null) return false;

        return settings.getBoolean("bungeecord") && proxies.getBoolean("bungee-cord.online-mode");
    }

    @Override
    protected void appendDefaultData(final JsonObject metrics) {
        metrics.addProperty("minecraft_version", minecraftVersion);
        metrics.addProperty("online_mode", checkOnlineMode());
        metrics.addProperty("player_count", getPlayerCount());
        metrics.addProperty("plugin_version", pluginVersion);
        metrics.addProperty("server_type", serverType);
    }

    private int getPlayerCount() {
        try {
            return plugin.getServer().getOnlinePlayers().size();
        } catch (final Throwable t) {
            error("Failed to get player count", t);
            return 0;
        }
    }

    @Override
    protected void printError(final String message, /*@Nullable*/ final Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, message, throwable);
    }

    @Override
    protected void printInfo(final String message) {
        plugin.getLogger().info(message);
    }

    @Override
    protected void printWarning(final String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void ready() {
        if (getErrorTracker().isPresent()) try {
            Class.forName("com.destroystokyo.paper.event.server.ServerExceptionEvent");
            plugin.getServer().getPluginManager().registerEvents(new PaperEventListener(this), plugin);
        } catch (final ClassNotFoundException ignored) {
        }
    }

    private <T> Optional<T> tryOrEmpty(final Supplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (final NoSuchMethodError | Exception e) {
            return Optional.empty();
        }
    }

    static final class Factory extends SimpleMetrics.Factory<Plugin, BukkitMetrics.Factory> implements BukkitMetrics.Factory {
        @Override
        public BukkitMetrics create(final Plugin plugin) throws IllegalStateException {
            final Path dataFolder = getPluginsFolder(plugin).resolve("faststats");
            final Path config = dataFolder.resolve("config.properties");
            return new BukkitMetricsImpl(this, plugin, config);
        }

        private static Path getPluginsFolder(final Plugin plugin) {
            try {
                return plugin.getServer().getPluginsFolder().toPath();
            } catch (final NoSuchMethodError e) {
                return plugin.getDataFolder().getParentFile().toPath();
            }
        }
    }
}
