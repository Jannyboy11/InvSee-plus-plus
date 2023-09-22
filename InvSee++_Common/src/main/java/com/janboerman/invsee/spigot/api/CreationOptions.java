package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.logging.LogGranularity;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Options used to customise how {@link SpectatorInventory}s are opened.
 * @param <Slot> the spectator inventory slot type. Usually {@link PlayerInventorySlot} or {@link EnderChestSlot}.
 */
public class CreationOptions<Slot> implements Cloneable {

    private Plugin plugin;
    private Title title;
    private boolean offlinePlayerSupport = true;
    private Mirror<Slot> mirror;
    private boolean unknownPlayerSupport = true;
    private boolean bypassExempt = false;
    private LogOptions logOptions = new LogOptions();
    private PlaceholderPalette placeholderPalette = PlaceholderPalette.empty();

    CreationOptions(Plugin plugin, Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt, LogOptions logOptions, PlaceholderPalette palette) {
        this.plugin = plugin;
        this.title = Objects.requireNonNull(title);
        this.offlinePlayerSupport = offlinePlayerSupport;
        this.mirror = Objects.requireNonNull(mirror);
        this.unknownPlayerSupport = unknownPlayerSupport;
        this.bypassExempt = bypassExempt;
        this.logOptions = Objects.requireNonNull(logOptions);
        this.placeholderPalette = palette;
    }

    /**
     * Create new CreationOptions.
     * @param plugin the plugin which wants to create the {@linkplain SpectatorInventory}
     * @param title the title of the {@linkplain SpectatorInventory}
     * @param offlinePlayerSupport whether offline players can be spectated
     * @param mirror the mirror which to view items through
     * @param unknownPlayerSupport whether unknown players can be spectated
     * @param bypassExempt whether exempted players can be spectated
     * @param logOptions the logging options used by the created {@link SpectatorInventoryView}.
     * @param placeholderPalette the placeholder palette
     * @return new creation options
     * @param <Slot> the slot type
     */
    public static <Slot> CreationOptions<Slot> of(Plugin plugin, Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt, LogOptions logOptions, PlaceholderPalette placeholderPalette) {
        return new CreationOptions<>(plugin, title, offlinePlayerSupport, mirror, unknownPlayerSupport, bypassExempt, logOptions, placeholderPalette);
    }

    @Deprecated(forRemoval = true, since = "0.19.6")
    public static <Slot> CreationOptions<Slot> of(Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport) throws Exception {
        Plugin plugin = JavaPlugin.getPlugin((Class<JavaPlugin>) Class.forName("com.janboerman.invsee.spigot.InvseePlusPlus"));
        return new CreationOptions<>(plugin, title, offlinePlayerSupport, mirror, unknownPlayerSupport, false, new LogOptions().withGranularity(LogGranularity.LOG_NEVER), PlaceholderPalette.empty());
    }

    @Deprecated(forRemoval = true, since = "0.19.6")
    public static <Slot> CreationOptions<Slot> of(Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt) throws Exception {
        Plugin plugin = JavaPlugin.getPlugin((Class<JavaPlugin>) Class.forName("com.janboerman.invsee.spigot.InvseePlusPlus"));
        return new CreationOptions<>(plugin, title, offlinePlayerSupport, mirror, unknownPlayerSupport, bypassExempt, new LogOptions().withGranularity(LogGranularity.LOG_NEVER), PlaceholderPalette.empty());
    }

    @Deprecated(forRemoval = true, since = "0.22.11")
    public static <Slot> CreationOptions<Slot> of(Plugin plugin, Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt, LogOptions logOptions) {
        return new CreationOptions<>(plugin, title, offlinePlayerSupport, mirror, unknownPlayerSupport, bypassExempt, logOptions, PlaceholderPalette.empty());
    }

    @Deprecated(forRemoval = true, since = "0.20.0")
    public static CreationOptions<PlayerInventorySlot> defaultMainInventory() {
        return defaultMainInventory(Bukkit.getPluginManager().getPlugin("InvseePlusPlus"));
    }

    /**
     * Get default creation options
     * @param plugin the plugin which wants to create {@linkplain MainSpectatorInventory}s.
     * @return new creation options
     */
    public static CreationOptions<PlayerInventorySlot> defaultMainInventory(Plugin plugin) {
        return new CreationOptions<>(plugin, Title.defaultMainInventory(), true, Mirror.defaultPlayerInventory(), true, false, new LogOptions(), PlaceholderPalette.empty());
    }

    @Deprecated(forRemoval = true, since = "0.20.0")
    public static CreationOptions<EnderChestSlot> defaultEnderInventory() {
        return defaultEnderInventory(Bukkit.getPluginManager().getPlugin("InvseePlusPlus"));
    }
    /**
     * Get default creation options
     * @param plugin the plugin which wants to create {@linkplain EnderSpectatorInventory}s.
     * @return new creation options
     */
    public static CreationOptions<EnderChestSlot> defaultEnderInventory(Plugin plugin) {
        return new CreationOptions<>(plugin, Title.defaultEnderInventory(), true, Mirror.defaultEnderChest(), true, false, new LogOptions(), PlaceholderPalette.empty());
    }

    /**
     * Creates a deep copy of these creation options.
     * @return new creation options with the same configured values as this one
     */
    @Override
    public CreationOptions<Slot> clone() {
        return new CreationOptions<>(getPlugin(), getTitle(), isOfflinePlayerSupported(), getMirror(), isUnknownPlayerSupported(), canBypassExemptedPlayers(), getLogOptions().clone(), getPlaceholderPalette());
    }

    /**
     * Set the plugin.
     * @param plugin the plugin
     * @return this
     */
    public CreationOptions<Slot> withPlugin(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        return this;
    }

    /**
     * Set the title.
     * @param title the title
     * @return this
     */
    public CreationOptions<Slot> withTitle(Title title) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        return this;
    }

    /**
     * Set the title.
     * @param title the title
     * @return this
     */
    public CreationOptions<Slot> withTitle(String title) {
        return withTitle(Title.of(title));
    }

    /**
     * Set offline player support.
     * @param offlinePlayerSupport true if offline players must be able to be spectated, otherwise false
     * @return this
     */
    public CreationOptions<Slot> withOfflinePlayerSupport(boolean offlinePlayerSupport) {
        this.offlinePlayerSupport = offlinePlayerSupport;
        return this;
    }

    /**
     * Set the mirror.
     * @param mirror the mirror
     * @return this
     */
    public CreationOptions<Slot> withMirror(Mirror<Slot> mirror) {
        this.mirror = Objects.requireNonNull(mirror, "mirror cannot be null");
        return this;
    }

    /**
     * Set unknown player support.
     * @param unknownPlayerSupport true if players who have not played on the server before must be able to be spectated, otherwise false
     * @return this
     */
    public CreationOptions<Slot> withUnknownPlayerSupport(boolean unknownPlayerSupport) {
        this.unknownPlayerSupport = unknownPlayerSupport;
        return this;
    }

    /**
     * Set exemption bypass.
     * @param bypassExemptedPlayers true if exempted players can still be spectated, false if exempted players cannot be spectated
     * @return this
     */
    public CreationOptions<Slot> withBypassExemptedPlayers(boolean bypassExemptedPlayers) {
        this.bypassExempt = bypassExemptedPlayers;
        return this;
    }

    /**
     * Set the logging options.
     * @param logOptions the logging options
     * @return this
     */
    public CreationOptions<Slot> withLogOptions(LogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "logOptions cannot be null");
        return this;
    }

    /**
     * Set the placeholder palette.
     * @param placeholderPalette the placeholder palette
     * @return this
     */
    public CreationOptions<Slot> withPlaceholderPalette(PlaceholderPalette placeholderPalette) {
        this.placeholderPalette = placeholderPalette;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CreationOptions)) return false;

        CreationOptions<?> that = (CreationOptions<?>) o;
        return this.getPlugin().equals(that.getPlugin())
                && this.getTitle().equals(that.getTitle())
                && this.isOfflinePlayerSupported() == that.isOfflinePlayerSupported()
                && this.getMirror() == that.getMirror()
                && this.isUnknownPlayerSupported() == that.isUnknownPlayerSupported()
                && this.canBypassExemptedPlayers() == that.canBypassExemptedPlayers()
                && Objects.equals(this.getLogOptions(), that.getLogOptions())
                && Objects.equals(this.getPlaceholderPalette(), that.getPlaceholderPalette());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlugin(), getTitle(), isOfflinePlayerSupported(), getMirror(), isUnknownPlayerSupported(), getLogOptions(), getPlaceholderPalette());
    }

    @Override
    public String toString() {
        return "CreationOptions"
                + "{plugin=" + getPlugin()
                + ",title=" + getTitle()
                + ",offlinePlayerSupport=" + isOfflinePlayerSupported()
                + ",mirror=" + getMirror()
                + ",unknownPlayerSupport=" + isUnknownPlayerSupported()
                + ",bypassExempt=" + canBypassExemptedPlayers()
                + ",logOptions=" + getLogOptions()
                + ",placeholderPalette=" + getPlaceholderPalette()
                + "}";
    }

    /** Get the configured plugin. */
    public Plugin getPlugin() {
        if (plugin == null) {
            plugin = JavaPlugin.getPlugin((Class<JavaPlugin>) Class.forName("com.janboerman.invsee.spigot.InvseePlusPlus"));
        }
        return plugin;
    }

    /** Get the configured title. */
    public Title getTitle() {
        return title;
    }

    /** Get the configured support for offline players. */
    public boolean isOfflinePlayerSupported() {
        return offlinePlayerSupport;
    }

    /** Get the configured mirror. */
    public Mirror<Slot> getMirror() {
        return mirror;
    }

    /** Get the configured support for unknown players. */
    public boolean isUnknownPlayerSupported() {
        return unknownPlayerSupport;
    }

    /** Get whether exempted players can still be spectated. */
    public boolean canBypassExemptedPlayers() {
        return bypassExempt;
    }

    /** Get the logging options */
    public LogOptions getLogOptions() {
        return logOptions;
    }

    /** Get the placeholder palette */
    public PlaceholderPalette getPlaceholderPalette() {
        return placeholderPalette;
    }
}
