package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.inventory.ItemStack;

/**
 * A spectator inventory that contains all the items of the target player's 'normal' inventory.
 * This includes the player's armour, items in his crafting grid, and even the item on his cursor!
 */
public interface MainSpectatorInventory extends SpectatorInventory<PlayerInventorySlot> {

    /** Get the items in the player's storage slots. */
    ItemStack[] getStorageContents();

    /** Set the items in the player's storage slots. */
    void setStorageContents(ItemStack[] storageContents);

    /** Get the items in the player's armour slots. */
    ItemStack[] getArmourContents();

    /** Change the items in the player's armour slots. */
    void setArmourContents(ItemStack[] armourContents);

    /** Get the items in the player's offhand. As per MC 1.21.6, this array includes the player's BODY and SADDLE items.*/
    ItemStack[] getOffHandContents();

    /** Set the items in the player's offhand. The length of this array must be equal to {@code getOffHandContents().length}. */
    void setOffHandContents(ItemStack[] offHand);

    /** Set the item that is on the player's cursor. */
    void setCursorContents(ItemStack cursor);

    /** Get the item that is on the player's cursor. */
    ItemStack getCursorContents();

    /** Set the items that are in the player's crafting slots.
     * If the target player has an anvil/crafting table/enchanting table/villager workstation opened,
     * then the contents of that temporary container will be updated instead. */
    void setPersonalContents(ItemStack[] craftingContents);

    /** Get the items that are in the player's crafting slots.
     * If the target player has an anvil/crafting/table/enchanting table/villager workstation opened,
     * then the contents of that temporary container will be retrieved instead.
     */
    ItemStack[] getPersonalContents();

    /** Get the length of the {@link #getPersonalContents()} array. */
    int getPersonalContentsSize();

    /** Get the mirror this inventory is viewed through. */
    public default Mirror<PlayerInventorySlot> getMirror() {
        return getCreationOptions().getMirror();
    }

    /** Set the contents of this inventory based on the contents from the provided inventory. */
    public default void setContents(MainSpectatorInventory newContents) {
        setStorageContents(newContents.getStorageContents());
        setArmourContents(newContents.getArmourContents());
        setOffHandContents(newContents.getOffHandContents());
        setCursorContents(newContents.getCursorContents());
        setPersonalContents(newContents.getPersonalContents());
    }

}
