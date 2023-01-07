package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A spectator inventory that contains all the items of the target player's 'normal' inventory.
 * This includes the player's armour, items in his crafting grid, and even the item on his cursor!
 */
public interface MainSpectatorInventory extends SpectatorInventory<PlayerInventorySlot> {

    /** Get the items in the player's armour slots. */
    ItemStack[] getArmourContents();

    /** Change the items in the player's armour slots. */
    void setArmourContents(ItemStack[] armourContents);

    /** Get the items in the player's offhand. The length of array is usually 1.*/
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
        return Mirror.defaultPlayerInventory();
    }

    /** Set the contents of this inventory based on the contents from the provided inventory */
    public default void setContents(MainSpectatorInventory newContents) {
        setStorageContents(newContents.getStorageContents());
        setArmourContents(newContents.getArmourContents());
        setOffHandContents(newContents.getOffHandContents());
        setCursorContents(newContents.getCursorContents());
        setPersonalContents(newContents.getPersonalContents());
    }


    public default ItemStack addItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getAmount() == 0) return null;

        ItemStack[] storageContents = getStorageContents();
        addItem(storageContents, itemStack, getMaxStackSize());
        setStorageContents(storageContents);

        if (itemStack.getAmount() == 0) return null;

        ItemStack[] armourContents = getArmourContents();
        addItem(armourContents, itemStack, getMaxStackSize());
        setArmourContents(armourContents);

        if (itemStack.getAmount() == 0) return null;

        ItemStack[] offHand = getOffHandContents();
        addItem(offHand, itemStack, getMaxStackSize());
        setOffHandContents(offHand);

        return itemStack;
    }

    private static void addItem(final ItemStack[] contents, final ItemStack itemStack, final int maxStackSize) {
        assert contents != null && itemStack != null;

        //merge with existing similar item stacks
        for (int i = 0; i < contents.length; i++) {
            ItemStack existingStack = contents[i];
            if (existingStack != null) {
                if (existingStack.isSimilar(itemStack) && existingStack.getAmount() < maxStackSize) {
                    //how many can we merge (at most)?
                    int maxMergeAmount = Math.min(maxStackSize - existingStack.getAmount(), itemStack.getAmount());
                    if (maxMergeAmount > 0) {
                        if (itemStack.getAmount() <= maxMergeAmount) {
                            //full merge
                            existingStack.setAmount(existingStack.getAmount() + itemStack.getAmount());
                            itemStack.setAmount(0);
                        } else {
                            //partial merge
                            existingStack.setAmount(maxStackSize);
                            itemStack.setAmount(itemStack.getAmount() - maxMergeAmount);
                        }
                    }
                }
            }

            if (itemStack.getAmount() == 0) break;
        }

        //merge with empty slots
        if (itemStack.getAmount() > 0) {
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] == null || contents[i].getAmount() == 0 || contents[i].getType() == Material.AIR) {
                    if (itemStack.getAmount() <= maxStackSize) {
                        //full merge
                        contents[i] = itemStack.clone();
                        itemStack.setAmount(0);
                    } else {
                        //partial merge
                        ItemStack clone = itemStack.clone(); clone.setAmount(maxStackSize);
                        contents[i] = clone;
                        itemStack.setAmount(itemStack.getAmount() - maxStackSize);
                    }
                }

                if (itemStack.getAmount() == 0) break;
            }
        }
    }

}
