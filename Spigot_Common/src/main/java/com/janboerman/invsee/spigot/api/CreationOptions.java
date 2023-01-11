package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;

import java.util.Objects;

public class CreationOptions<Slot> implements Cloneable {

    private Target target;
    private Title title;
    private boolean offlineSupport = true;
    private Mirror<Slot> mirror;
    private boolean unknownPlayerSupport = true;

    //TODO logging options

    public CreationOptions(Target target) {
        this.target = Objects.requireNonNull(target, "target cannot be null");
    }

    CreationOptions(Target target, Title title, boolean offlineSupport, Mirror<Slot> mirror, boolean newPlayerSupport) {
        this.target = Objects.requireNonNull(target);
        this.title = Objects.requireNonNull(title);
        this.offlineSupport = offlineSupport;
        this.mirror = Objects.requireNonNull(mirror);
        this.unknownPlayerSupport = newPlayerSupport;
    }

    @Override
    public CreationOptions<Slot> clone() {
        return new CreationOptions<>(target, title, offlineSupport, mirror, unknownPlayerSupport);
    }

    public CreationOptions<Slot> withTitle(Title title) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        return this;
    }

    public CreationOptions<Slot> withTitle(String title) {
        return withTitle(Title.constant(title));
    }

    public CreationOptions<Slot> withOfflineSupport(boolean offlineSupport) {
        this.offlineSupport = offlineSupport;
        return this;
    }

    public CreationOptions<Slot> withMirror(Mirror<Slot> mirror) {
        this.mirror = Objects.requireNonNull(mirror, "mirror cannot be null");
        return this;
    }

    public CreationOptions<Slot> allowSaveFileCreation(boolean createSaveFileIfAbsent) {
        this.unknownPlayerSupport = createSaveFileIfAbsent;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CreationOptions)) return false;

        CreationOptions<?> that = (CreationOptions<?>) o;
        return this.getTarget().equals(that.getTarget())
                && this.getTitle().equals(that.getTitle())
                && this.isOfflineSupported() == that.isOfflineSupported()
                && this.getMirror() == that.getMirror()
                && this.isUnknownPlayerSupported() == that.isUnknownPlayerSupported();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTarget(), getTitle(), isOfflineSupported(), getMirror(), isUnknownPlayerSupported());
    }

    @Override
    public String toString() {
        return "CreationOptions"
                + "{target=" + getTarget()
                + ",title=" + getTitle()
                + ",offlineSupport=" + isOfflineSupported()
                + ",mirror=" + getMirror()
                + ",unknownPlayerSupport=" + isUnknownPlayerSupported()
                + "}";
    }

    public Target getTarget() {
        return target;
    }

    public Title getTitle() {
        return title;
    }

    public boolean isOfflineSupported() {
        return offlineSupport;
    }

    public Mirror<Slot> getMirror() {
        return mirror;
    }

    public boolean isUnknownPlayerSupported() {
        return unknownPlayerSupport;
    }

    //this is not a true getter.
    public String getTitleString() {
        return getTitle().titleFor(getTarget());
    }

}
