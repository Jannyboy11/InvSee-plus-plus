package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.logging.LogGranularity;
import com.janboerman.invsee.spigot.api.logging.LogOptions;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CreationOptions<Slot> implements Cloneable {

    private Plugin plugin;
    private Title title;
    private boolean offlinePlayerSupport = true;
    private Mirror<Slot> mirror;
    private boolean unknownPlayerSupport = true;
    private boolean bypassExempt = false;
    private LogOptions logOptions = new LogOptions();

    CreationOptions(Plugin plugin, Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt, LogOptions logOptions) {
        this.plugin = Objects.requireNonNull(plugin);
        this.title = Objects.requireNonNull(title);
        this.offlinePlayerSupport = offlinePlayerSupport;
        this.mirror = Objects.requireNonNull(mirror);
        this.unknownPlayerSupport = unknownPlayerSupport;
        this.bypassExempt = bypassExempt;
        this.logOptions = Objects.requireNonNull(logOptions);
    }

    @Deprecated(forRemoval = true)
    public static <Slot> CreationOptions<Slot> of(Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport) throws Exception {
        Plugin plugin = JavaPlugin.getPlugin((Class<JavaPlugin>) Class.forName("com.janboerman.invsee.spigot.InvseePlusPlus"));
        return new CreationOptions<>(plugin, title, offlinePlayerSupport, mirror, unknownPlayerSupport, false, new LogOptions().withGranularity(LogGranularity.LOG_NEVER));
    }

    @Deprecated(forRemoval = true)
    public static <Slot> CreationOptions<Slot> of(Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt) throws Exception {
        Plugin plugin = JavaPlugin.getPlugin((Class<JavaPlugin>) Class.forName("com.janboerman.invsee.spigot.InvseePlusPlus"));
        return new CreationOptions<>(plugin, title, offlinePlayerSupport, mirror, unknownPlayerSupport, bypassExempt, new LogOptions().withGranularity(LogGranularity.LOG_NEVER));
    }

    public static <Slot> CreationOptions<Slot> of(Plugin plugin, Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt, LogOptions logOptions) {
        return new CreationOptions<>(plugin, title, offlinePlayerSupport, mirror, unknownPlayerSupport, bypassExempt, logOptions);
    }

    @Deprecated
    public static CreationOptions<PlayerInventorySlot> defaultMainInventory() {
        return defaultMainInventory(Bukkit.getPluginManager().getPlugin("InvseePlusPlus"));
    }

    public static CreationOptions<PlayerInventorySlot> defaultMainInventory(Plugin plugin) {
        return new CreationOptions<>(plugin, Title.defaultMainInventory(), true, Mirror.defaultPlayerInventory(), true, false, new LogOptions());
    }

    @Deprecated
    public static CreationOptions<EnderChestSlot> defaultEnderInventory() {
        return defaultEnderInventory(Bukkit.getPluginManager().getPlugin("InvseePlusPlus"));
    }

    public static CreationOptions<EnderChestSlot> defaultEnderInventory(Plugin plugin) {
        return new CreationOptions<>(plugin, Title.defaultEnderInventory(), true, Mirror.defaultEnderChest(), true, false, new LogOptions());
    }

    @Override
    public CreationOptions<Slot> clone() {
        return new CreationOptions<>(getPlugin(), getTitle(), isOfflinePlayerSupported(), getMirror(), isUnknownPlayerSupported(), canBypassExemptedPlayers(), logOptions.clone());
    }

    public CreationOptions<Slot> withPlugin(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        return this;
    }

    public CreationOptions<Slot> withTitle(Title title) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        return this;
    }

    public CreationOptions<Slot> withTitle(String title) {
        return withTitle(Title.of(title));
    }

    public CreationOptions<Slot> withOfflinePlayerSupport(boolean offlinePlayerSupport) {
        this.offlinePlayerSupport = offlinePlayerSupport;
        return this;
    }

    public CreationOptions<Slot> withMirror(Mirror<Slot> mirror) {
        this.mirror = Objects.requireNonNull(mirror, "mirror cannot be null");
        return this;
    }

    public CreationOptions<Slot> withUnknownPlayerSupport(boolean unknownPlayerSupport) {
        this.unknownPlayerSupport = unknownPlayerSupport;
        return this;
    }

    public CreationOptions<Slot> withBypassExemptedPlayers(boolean bypassExemptedPlayers) {
        this.bypassExempt = bypassExemptedPlayers;
        return this;
    }

    public CreationOptions<Slot> withLogOptions(LogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "logOptions cannot be null");
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
                && Objects.equals(this.getLogOptions(), that.getLogOptions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlugin(), getTitle(), isOfflinePlayerSupported(), getMirror(), isUnknownPlayerSupported(), getLogOptions());
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
                + "}";
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Title getTitle() {
        return title;
    }

    public boolean isOfflinePlayerSupported() {
        return offlinePlayerSupport;
    }

    public Mirror<Slot> getMirror() {
        return mirror;
    }

    public boolean isUnknownPlayerSupported() {
        return unknownPlayerSupport;
    }

    public boolean canBypassExemptedPlayers() {
        return bypassExempt;
    }

    public LogOptions getLogOptions() {
        return logOptions;
    }
}
