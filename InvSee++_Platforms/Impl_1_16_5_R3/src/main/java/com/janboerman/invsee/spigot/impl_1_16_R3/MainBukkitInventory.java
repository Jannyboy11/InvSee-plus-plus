package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderGroup;
import com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.inventory.Personal;
import com.janboerman.invsee.spigot.internal.inventory.Wrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class MainBukkitInventory extends CraftInventory implements MainSpectatorInventory, Personal, Wrapper<MainNmsInventory, MainBukkitInventory> {

    protected MainBukkitInventory(MainNmsInventory nmsInventory) {
        super(nmsInventory);
    }

    @Override
    public MainNmsInventory getInventory() {
        return (MainNmsInventory) super.getInventory();
    }

    @Override
    public String getSpectatedPlayerName() {
        return getInventory().spectatedPlayerName;
    }

    @Override
    public UUID getSpectatedPlayerId() {
        return getInventory().spectatedPlayerUuid;
    }

    @Override
    public String getTitle() {
        return getInventory().creationOptions.getTitle().titleFor(Target.byGameProfile(getSpectatedPlayerId(), getSpectatedPlayerName()));
    }

    @Override
    public ItemStack[] getStorageContents() {
        return getInventory().storageContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setStorageContents(ItemStack[] storageContents) {
        Objects.requireNonNull(storageContents, "storageContents cannot be null");
        int storageContentsSize = getInventory().storageContents.size();
        if (storageContents.length != storageContentsSize)
            throw new IllegalArgumentException("storageContents must be of length " + storageContentsSize);

        for (int i = 0; i < storageContentsSize; i++) {
            getInventory().storageContents.set(i, CraftItemStack.asNMSCopy(storageContents[i]));
        }
    }

    @Override
    public void watch(InventoryView targetPlayerView) {
        Objects.requireNonNull(targetPlayerView, "targetPlayerView cannot be null");

        PlaceholderGroup placeholderGroup = null;

        MainNmsInventory nms = getInventory();
        Inventory top = targetPlayerView.getTopInventory();
        if (top instanceof CraftInventoryCrafting /*does not extend CraftResultInventory for some reason.*/) {
            //includes a player's own crafting slots
            InventoryCrafting targetCrafting = (InventoryCrafting) ((CraftInventoryCrafting) top).getInventory();
            nms.personalContents = targetCrafting.getContents(); //luckily, this does not create a copy.
            placeholderGroup = PlaceholderGroup.CRAFTING;
        } else if (top instanceof CraftResultInventory) {
            //anvil, grindstone, loom, smithing table, cartography table, stone cutter
            IInventory repairItems = ((CraftResultInventory) top).getInventory();
            nms.personalContents = repairItems.getContents();
            switch (top.getType()) {
                case ANVIL: placeholderGroup = PlaceholderGroup.ANVIL; break;
                case CARTOGRAPHY: placeholderGroup = PlaceholderGroup.CARTOGRAPHY; break;
                case GRINDSTONE: placeholderGroup = PlaceholderGroup.GRINDSTONE; break;
                case LOOM: placeholderGroup = PlaceholderGroup.LOOM; break;
                case SMITHING: placeholderGroup = PlaceholderGroup.SMITHING; break;
                case STONECUTTER: placeholderGroup = PlaceholderGroup.STONECUTTER; break;
            };
        } else if (top instanceof CraftInventoryEnchanting /*does not extend CraftResultInventory for some reason.*/) {
            IInventory enchantItems = ((CraftInventoryEnchanting) top).getInventory();
            nms.personalContents = enchantItems.getContents();
            placeholderGroup = PlaceholderGroup.ENCHANTING;
        } else if (top instanceof CraftInventoryMerchant /*does not extend CraftResultInventory for some reason.*/) {
            InventoryMerchant merchantItems = ((CraftInventoryMerchant) top).getInventory();
            nms.personalContents = merchantItems.getContents().subList(0, 2); //only payment slots
            placeholderGroup = PlaceholderGroup.MERCHANT;
        }

        //send personal slots changes
        for (HumanEntity viewer : getViewers()) {
            CraftPlayer spectator;
            MainBukkitInventoryView view;
            if (viewer instanceof CraftPlayer && (spectator = (CraftPlayer) viewer).getOpenInventory() instanceof MainBukkitInventoryView) {
                view = (MainBukkitInventoryView) spectator.getOpenInventory();
                CreationOptions<PlayerInventorySlot> creationOptions = view.nms.creationOptions;
                Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
                com.janboerman.invsee.spigot.api.placeholder.PlaceholderPalette palette = creationOptions.getPlaceholderPalette();

                for (int i = PlayerInventorySlot.PERSONAL_00.defaultIndex(); i <= PlayerInventorySlot.PERSONAL_08.defaultIndex(); i++) {
                    Integer rawIndex = mirror.getIndex(PlayerInventorySlot.byDefaultIndex(i));
                    if (rawIndex != null) { // null rawIndex does not happen if the server admin configured the template correctly.
                        net.minecraft.server.v1_16_R3.ItemStack stack = InvseeImpl.getItemOrPlaceholder(palette, view, rawIndex, placeholderGroup);
                        InvseeImpl.sendItemChange(spectator.getHandle(), rawIndex, stack);
                    } else {
                        InvseeImpl.sendItemChange(spectator.getHandle(), i, CraftItemStack.asNMSCopy(palette.inaccessible()));
                    }
                }
            }
        }
    }

    @Override
    public void unwatch() {
        MainNmsInventory nms = getInventory();
        nms.personalContents = nms.playerCraftingContents;

        //send personal slots changes
        for (HumanEntity viewer : getViewers()) {
            CraftPlayer spectator;
            MainBukkitInventoryView view;
            if (viewer instanceof CraftPlayer && (spectator = (CraftPlayer) viewer).getOpenInventory() instanceof MainBukkitInventoryView) {
                view = (MainBukkitInventoryView) spectator.getOpenInventory();
                CreationOptions<PlayerInventorySlot> creationOptions = view.nms.creationOptions;
                Mirror<PlayerInventorySlot> mirror = creationOptions.getMirror();
                PlaceholderPalette palette = creationOptions.getPlaceholderPalette();

                for (int i = PlayerInventorySlot.PERSONAL_00.defaultIndex(); i <= PlayerInventorySlot.PERSONAL_08.defaultIndex(); i++) {
                    Integer rawIndex = mirror.getIndex(PlayerInventorySlot.byDefaultIndex(i));
                    if (rawIndex != null) { // null rawIndex does not happen if the server admin configured the template correctly.
                        net.minecraft.server.v1_16_R3.ItemStack stack = InvseeImpl.getItemOrPlaceholder(palette, view, rawIndex, PlaceholderGroup.CRAFTING);
                        InvseeImpl.sendItemChange(spectator.getHandle(), rawIndex, stack);
                    } else {
                        InvseeImpl.sendItemChange(spectator.getHandle(), i, CraftItemStack.asNMSCopy(palette.inaccessible()));
                    }
                }
            }
        }
    }

    @Override
    public ItemStack[] getArmourContents() {
        return getInventory().armourContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setArmourContents(ItemStack[] armourContents) {
        Objects.requireNonNull(armourContents, "armourContents cannot be null");
        int armourContententsSize = getInventory().armourContents.size();
        if (armourContents.length != armourContententsSize)
            throw new IllegalArgumentException("armourContents must be of length " + armourContententsSize);

        for (int i = 0; i < armourContententsSize; i++) {
            getInventory().armourContents.set(i, CraftItemStack.asNMSCopy(armourContents[i]));
        }
    }

    @Override
    public ItemStack[] getOffHandContents() {
        return getInventory().offHand.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setOffHandContents(ItemStack[] offHand){
        Objects.requireNonNull(offHand, "offHand cannot be null");
        int offHandContentsSize = getInventory().offHand.size();
        if (offHand.length != offHandContentsSize)
            throw new IllegalArgumentException("offHand must be of length " + offHandContentsSize);

        for (int i = 0; i < offHandContentsSize; i++) {
            getInventory().offHand.set(i, CraftItemStack.asNMSCopy(offHand[i]));
        }
    }

    @Override
    public void setPersonalContents(ItemStack[] craftingContents) {
        Objects.requireNonNull(craftingContents, "craftingContents cannot be null");

        MainNmsInventory nms = getInventory();
        var nmsCraftingItems = nms.personalContents;
        if (nmsCraftingItems != null) {
            int craftingContentsSize = nmsCraftingItems.size();
            if (craftingContents.length != craftingContentsSize)
                throw new IllegalArgumentException("craftingContents must be of length " + craftingContentsSize);

            for (int i = 0; i < craftingContentsSize; i++) {
                nmsCraftingItems.set(i, CraftItemStack.asNMSCopy(craftingContents[i]));
            }
        }
    }

    @Override
    public ItemStack[] getPersonalContents() {
        var nmsCraftingItems = getInventory().personalContents;
        if (nmsCraftingItems != null) {
            int craftingContentsSize = nmsCraftingItems.size();
            ItemStack[] result = new ItemStack[craftingContentsSize];
            for (int i = 0; i < craftingContentsSize; i++) {
                result[i] = CraftItemStack.asCraftMirror(nmsCraftingItems.get(i));
            }
            return result;
        } else {
            return new ItemStack[0];
        }
    }

    @Override
    public int getPersonalContentsSize() {
        var nmsCraftingItems = getInventory().personalContents;
        if (nmsCraftingItems != null) {
            return nmsCraftingItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public void setCursorContents(ItemStack cursor) {
        var onCursor = getInventory().onCursor;
        if (onCursor != null) {
            onCursor.set(CraftItemStack.asNMSCopy(cursor));
        }
    }

    @Override
    public ItemStack getCursorContents() {
        var onCursor = getInventory().onCursor;
        if (onCursor != null) {
            return CraftItemStack.asCraftMirror(onCursor.get());
        } else {
            return null;
        }
    }

    @Override
    public Mirror<PlayerInventorySlot> getMirror() {
        return getInventory().creationOptions.getMirror();
    }

    @Override
    public CreationOptions<PlayerInventorySlot> getCreationOptions() {
        return getInventory().creationOptions.clone();
    }

    // org.bukkit.inventory.Inventory overrides
    // lookups

    @Override
    public int first(ItemStack stack) {
        return first(stack, true);
    }

    public int first(ItemStack stack, boolean withAmount) {
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null) {
                if (withAmount) {
                    if (item.equals(stack))
                        return slot;
                } else {
                    if (item.isSimilar(stack))
                        return slot;
                }
            } else {
                if (stack == null)
                    return slot;
            }
        }

        return -1;
    }

    @Override
    public int first(Material material) {
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if ((item == null && material == null) || (item != null && item.getType() == material))
                return slot;
        }

        return -1;
    }

    @Override
    public int firstEmpty() {
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item == null || item.getAmount() == 0 || item.getType() == Material.AIR)
                return slot;
        }

        return -1;
    }

    @Override
    public int firstPartial(Material material) {
        if (material == null)
            return -1;

        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.getType() == material && item.getAmount() < item.getMaxStackSize())
                return slot;
        }

        return -1;
    }

    public int firstPartial(ItemStack item) {
        if (item == null)
            return -1;

        item = CraftItemStack.asCraftCopy(item);
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack cItem = getItem(slot);
            if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item))
                return slot;
        }

        return -1;
    }

    @Override
    public boolean contains(Material material) {
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if ((item == null && material == null) || (item != null && item.getType() == material))
                return true;
        }
        return false;
    }

    @Override
    public boolean contains(ItemStack stack) {
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (Objects.equals(item, stack))
                return true;
        }
        return false;
    }

    @Override
    public boolean contains(Material material, int amount) {
        if (amount <= 0)
            return true;

        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.getType() == material && (amount -= item.getAmount()) <= 0)
                return true;
        }

        return false;
    }

    @Override
    public boolean contains(ItemStack stack, int count) {
        if (count <= 0)
            return true;

        int encountered = 0;
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (Objects.equals(item, stack)) {
                encountered += 1;
                if (encountered >= count)
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack stack, int amount) {
        if (amount <= 0)
            return true;
        if (stack == null)
            return false; //this is a bit weird, but this is what CraftInventory does.

        int encountered = 0;
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.isSimilar(stack)) {
                encountered += item.getAmount();
                if (encountered >= amount)
                    return true;
            }
        }

        return false;
    }

    @Override
    public HashMap<Integer, ItemStack> all(Material material) {
        HashMap<Integer, ItemStack> slots = new HashMap<>();

        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if ((item == null && material == null) || (item != null && item.getType() == material))
                slots.put(slot, item);
        }

        return slots;
    }

    @Override
    public HashMap<Integer, ItemStack> all(ItemStack stack) {
        HashMap<Integer, ItemStack> slots = new HashMap<>();

        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (Objects.equals(item, stack))
                slots.put(slot, item);
        }

        return slots;
    }

    // adding

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack[] items) {
        assert items != null;

        HashMap<Integer, ItemStack> leftOvers = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack leftOver = addItem(items[i]);
            if (leftOver != null && leftOver.getAmount() > 0) {
                leftOvers.put(i, leftOver);
            }
        }

        return leftOvers;
    }

    private ItemStack addItem(ItemStack itemStack) {
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

        return itemStack; //leftover (couldn't be added)
    }

    private static void addItem(final ItemStack[] contents, final ItemStack add, final int maxStackSize) {
        assert contents != null && add != null;

        //merge with existing similar item stacks
        for (int i = 0; i < contents.length && add.getAmount() > 0; i++) {
            final ItemStack existingStack = contents[i];
            if (existingStack != null) {
                final int maxStackSizeForThisItem = Math.min(maxStackSize, existingStack.getMaxStackSize());
                if (existingStack.isSimilar(add) && existingStack.getAmount() < maxStackSizeForThisItem) {
                    //how many can we merge (at most)?
                    final int maxMergeAmount = Math.min(maxStackSizeForThisItem - existingStack.getAmount(), add.getAmount());
                    if (maxMergeAmount > 0) {
                        if (add.getAmount() <= maxMergeAmount) {
                            //full merge
                            existingStack.setAmount(existingStack.getAmount() + add.getAmount());
                            add.setAmount(0);
                        } else {
                            //partial merge
                            existingStack.setAmount(maxStackSizeForThisItem);
                            add.setAmount(add.getAmount() - maxMergeAmount);
                        }
                    }
                }
            }
        }

        //merge with empty slots
        final int maxStackSizeForThisItem = Math.min(maxStackSize, add.getMaxStackSize());
        for (int i = 0; i < contents.length && add.getAmount() > 0; i++) {
            if (contents[i] == null || contents[i].getAmount() == 0 || contents[i].getType() == Material.AIR) {
                if (add.getAmount() <= maxStackSizeForThisItem) {
                    //full merge
                    contents[i] = add.clone();
                    add.setAmount(0);
                } else {
                    //partial merge
                    ItemStack clone = add.clone(); clone.setAmount(maxStackSize);
                    contents[i] = clone;
                    add.setAmount(add.getAmount() - maxStackSize);
                }
            }
        }
    }

    // removing

    @Override
    public void remove(Material material) {
        if (material == null) return;

        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.getType() == material)
                clear(slot);
        }
    }

    @Override
    public void remove(ItemStack stack) {
        if (stack == null) return;

        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.equals(stack))
                clear(slot);
        }
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
        assert items != null;

        HashMap<Integer, ItemStack> leftOvers = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack leftOver = removeItem(items[i]);
            if (leftOver != null && leftOver.getAmount() > 0) {
                leftOvers.put(i, leftOver);
            }
        }

        return leftOvers;
    }

    private ItemStack removeItem(ItemStack remove) {
        if (remove == null || remove.getAmount() == 0) return null;

        for (int slot = 0; slot < getSize() && remove.getAmount() > 0; slot++) {
            final ItemStack existingStack = getItem(slot);
            if (existingStack != null) {
                if (existingStack.isSimilar(remove)) {
                    //how many can we remove (at most)?
                    final int maxRemoveAmount = Math.min(existingStack.getAmount(), remove.getAmount());
                    //subtract the amount from both item stacks
                    existingStack.setAmount(existingStack.getAmount() - maxRemoveAmount);
                    remove.setAmount(remove.getAmount() - maxRemoveAmount);
                }
            }
        }

        return remove; //leftover (couldn't be removed)
    }

}
