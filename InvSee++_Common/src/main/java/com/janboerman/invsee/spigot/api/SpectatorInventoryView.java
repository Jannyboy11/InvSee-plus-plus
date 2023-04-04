package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;
import org.bukkit.inventory.InventoryView;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class SpectatorInventoryView<Slot> extends InventoryView {

    private final CreationOptions<Slot> creationOptions;
    protected Target target;

    protected SpectatorInventoryView(CreationOptions<Slot> creationOptions) {
        this.creationOptions = Objects.requireNonNull(creationOptions, "creation options cannot be null");
    }

    @Override
    public abstract SpectatorInventory<Slot> getTopInventory();

    public abstract @Nullable Difference getTrackedDifference();

    public CreationOptions<Slot> getCreationOptions() {
        return creationOptions.clone();
    }

    //can't override getTitle because that does not work on 1.12 (the method is final :/)
    //can't override title() either because of Adventure Text (because I can't have return type String)
    //ooh the burden of supporting multiple versions!

    public Mirror<Slot> getMirror() {
        return getCreationOptions().getMirror();
    }

    public Target getTarget() {
        SpectatorInventory<Slot> top = getTopInventory();
        return target == null ? target = Target.byGameProfile(top.getSpectatedPlayerId(), top.getSpectatedPlayerName()) : target;
    }

}
