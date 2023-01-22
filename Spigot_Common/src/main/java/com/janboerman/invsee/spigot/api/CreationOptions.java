package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;

import java.util.Objects;

public class CreationOptions<Slot> implements Cloneable {

    private Title title;
    private boolean offlinePlayerSupport = true;
    private Mirror<Slot> mirror;
    private boolean unknownPlayerSupport = true;
    private boolean bypassExempt = false;

    //TODO logging options

    CreationOptions(Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt) {
        this.title = Objects.requireNonNull(title);
        this.offlinePlayerSupport = offlinePlayerSupport;
        this.mirror = Objects.requireNonNull(mirror);
        this.unknownPlayerSupport = unknownPlayerSupport;
        this.bypassExempt = bypassExempt;
    }

    public static <Slot> CreationOptions<Slot> of(Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport) {
        return new CreationOptions<>(title, offlinePlayerSupport, mirror, unknownPlayerSupport, false);
    }

    public static <Slot> CreationOptions<Slot> of(Title title, boolean offlinePlayerSupport, Mirror<Slot> mirror, boolean unknownPlayerSupport, boolean bypassExempt) {
        return new CreationOptions<>(title, offlinePlayerSupport, mirror, unknownPlayerSupport, bypassExempt);
    }

    public static CreationOptions<PlayerInventorySlot> defaultMainInventory() {
        return new CreationOptions<>(Title.defaultMainInventory(), true, Mirror.defaultPlayerInventory(), true, false);
    }

    public static CreationOptions<EnderChestSlot> defaultEnderInventory() {
        return new CreationOptions<>(Title.defaultEnderInventory(), true, Mirror.defaultEnderChest(), true, false);
    }

    @Override
    public CreationOptions<Slot> clone() {
        return new CreationOptions<>(getTitle(), isOfflinePlayerSupported(), getMirror(), isUnknownPlayerSupported(), canBypassExemptedPlayers());
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

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CreationOptions)) return false;

        CreationOptions<?> that = (CreationOptions<?>) o;
        return this.getTitle().equals(that.getTitle())
                && this.isOfflinePlayerSupported() == that.isOfflinePlayerSupported()
                && this.getMirror() == that.getMirror()
                && this.isUnknownPlayerSupported() == that.isUnknownPlayerSupported()
                && this.canBypassExemptedPlayers() == that.canBypassExemptedPlayers();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), isOfflinePlayerSupported(), getMirror(), isUnknownPlayerSupported());
    }

    @Override
    public String toString() {
        return "CreationOptions"
                + "{title=" + getTitle()
                + ",offlinePlayerSupport=" + isOfflinePlayerSupported()
                + ",mirror=" + getMirror()
                + ",unknownPlayerSupport=" + isUnknownPlayerSupported()
                + ",bypassExempt=" + canBypassExemptedPlayers()
                + "}";
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

}
