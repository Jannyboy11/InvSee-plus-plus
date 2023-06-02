package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;
import org.bukkit.inventory.InventoryView;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents an open window for a {@link SpectatorInventory}.
 * @param <Slot> the inventory's slot type.
 */
public abstract class SpectatorInventoryView<Slot> extends InventoryView {

    private final CreationOptions<Slot> creationOptions;
    protected Target target;

    protected SpectatorInventoryView(CreationOptions<Slot> creationOptions) {
        this.creationOptions = Objects.requireNonNull(creationOptions, "creation options cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    public abstract SpectatorInventory<Slot> getTopInventory();

    /**
     * Get the difference tracked by this window.
     * @return the difference
     */
    public abstract @Nullable Difference getTrackedDifference();

    /**
     * Get the options this window was created with.
     * @return a copy of the creation options
     */
    public CreationOptions<Slot> getCreationOptions() {
        return creationOptions.clone();
    }

    //can't override getTitle because that does not work on 1.12 (the method is final :/)
    //can't override title() either because of Adventure Text (because I can't have return type String)
    //ooh the burden of supporting multiple versions!

    /**
     * Get the mirror the {@link SpectatorInventory} is viewed through.
     * @return the mirror
     * @see <a href="https://github.com/Jannyboy11/InvSee-plus-plus/wiki/Customising-spectator-inventories#mirror">Mirror explanation in the InvSee++ wiki</a>
     */
    public Mirror<Slot> getMirror() {
        return creationOptions.getMirror();
    }

    /**
     * Get the target of the {@link SpectatorInventory}.
     * @return the target
     */
    public Target getTarget() {
        SpectatorInventory<Slot> top = getTopInventory();
        return target == null ? target = Target.byGameProfile(top.getSpectatedPlayerId(), top.getSpectatedPlayerName()) : target;
    }

}
