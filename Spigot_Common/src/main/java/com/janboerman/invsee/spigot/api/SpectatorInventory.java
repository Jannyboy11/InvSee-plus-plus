package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.Mirror;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

/**
 * Represents a spectator-inventory, i.e. the inventory that an admin player sees when editing a player's inventory or enderchest content.
 */
public interface SpectatorInventory<Slot> extends Inventory {

    /** get the username of the spectated player */
    public String getSpectatedPlayerName();

    /** get the unique id of the spectated player */
    public UUID getSpectatedPlayerId();

    /** get the title of this inventory */
    public String getTitle();

    /** get the default Mirror this inventory is viewed through */
    public Mirror<Slot> getMirror();

    /** get the options this inventory was created with */
    public CreationOptions<Slot> getCreationOptions();

}
