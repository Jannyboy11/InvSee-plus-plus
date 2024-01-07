package com.janboerman.invsee.spigot.perworldinventory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FakeInventory implements Inventory {

    protected final InventoryType type;
    protected final ItemStack[] items;
    protected final InventoryHolder holder;

    private int maxStack = 64;
    private List<HumanEntity> viewers;

    public FakeInventory(InventoryType type, ItemStack[] items, InventoryHolder holder) {
        this.type = type;
        this.items = items;
        this.holder = holder;
    }

    @Override
    public int getSize() {
        return items.length;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int i) {
        this.maxStack = i;
    }

    @Nullable
    @Override
    public ItemStack getItem(int i) {
        return items[i];
    }

    @Override
    public void setItem(int i, @Nullable ItemStack itemStack) {
        items[i] = itemStack;
    }

    @NotNull
    @Override
    public HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... itemStacks) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> result = new HashMap<>();

        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack toAdd = itemStacks[i];

            if (toAdd != null && !toAdd.getType().isAir()) {

                for (int j = 0; j < getStorageContents().length; j++) {
                    ItemStack inSlot = items[j];

                    if (inSlot == null) {
                        //empty slot, just put it!
                        getStorageContents()[j] = toAdd.clone();
                        toAdd = null;
                        break;
                    } else {
                        if (inSlot.isSimilar(toAdd)) {
                            //merge
                            int transferAmount = Math.min(inSlot.getMaxStackSize() - inSlot.getAmount(), toAdd.getAmount());
                            toAdd.setAmount(toAdd.getAmount() - transferAmount);
                            inSlot.setAmount(inSlot.getAmount() + transferAmount);
                            if (toAdd.getAmount() <= 0) {
                                toAdd = null;
                                break;
                            }
                        } //else: just continue the inner loop - try remaining slots
                    }
                } //end looping over this inventory's contents

                //check whether the stack was completely transferred
                if (toAdd != null) {
                    result.put(i, toAdd);
                }
            }
        } //end loop over the items to add

        return result;
    }

    @NotNull
    @Override
    public HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... itemStacks) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> result = new HashMap<>();

        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack toRemove = itemStacks[i];

            if (toRemove != null && !toRemove.getType().isAir()) {

                for (int j = 0; j < getStorageContents().length; j++) {
                    ItemStack inSlot = getStorageContents()[j];

                    if (inSlot != null) {
                        if (inSlot.isSimilar(toRemove)) {
                            //remove
                            int transferAmount = Math.min(inSlot.getAmount(), toRemove.getAmount());
                            toRemove.setAmount(toRemove.getAmount() - transferAmount);
                            inSlot.setAmount(inSlot.getAmount() - transferAmount);
                            if (inSlot.getAmount() <= 0) {
                                getStorageContents()[j] = null;
                            }
                            if (toRemove.getAmount() <= 0) {
                                toRemove = null;
                                break;
                            }
                        } //else: just continue the inner loop - try remaining slots
                    }
                } //end looping over this inventory's contents

                //check whether the stack was completely transferred
                if (toRemove != null) {
                    result.put(i, toRemove);
                }
            }
        } //end loop over the items to add

        return result;
    }

    @NotNull
    @Override
    public ItemStack[] getContents() {
        return items;
    }

    @Override
    public void setContents(@NotNull ItemStack[] itemStacks) throws IllegalArgumentException {
        System.arraycopy(itemStacks, 0, items, 0, items.length);
    }

    @NotNull
    @Override
    public ItemStack[] getStorageContents() {
        return items;
    }

    @Override
    public void setStorageContents(@NotNull ItemStack[] itemStacks) throws IllegalArgumentException {
        System.arraycopy(itemStacks, 0, items, 0, items.length);
    }

    @Override
    public boolean contains(@NotNull Material material) throws IllegalArgumentException {
        return Arrays.stream(getStorageContents()).anyMatch(i -> material == i.getType());
    }

    @Override
    public boolean contains(@Nullable ItemStack itemStack) {
        return Arrays.stream(getStorageContents()).anyMatch(i -> Objects.equals(i, itemStack));
    }

    @Override
    public boolean contains(@NotNull Material material, int i) throws IllegalArgumentException {
        if (i < 1) return true;
        int sum = 0;
        for (ItemStack stack : getStorageContents()) {
            if (stack != null && stack.getType() == material) {
                sum += stack.getAmount();
                if (sum >= i) return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(@Nullable ItemStack itemStack, int i) {
        if (i < 1 || itemStack == null) return true;
        int sum = 0;
        for (ItemStack stack : getStorageContents()) {
            if (stack != null && stack.isSimilar(itemStack)) {
                sum += stack.getAmount();
            }
        }
        return sum == i;
    }

    @Override
    public boolean containsAtLeast(@Nullable ItemStack itemStack, int i) {
        if (i < 1 || itemStack == null) return true;
        int sum = 0;
        for (ItemStack stack : getStorageContents()) {
            if (stack != null && stack.isSimilar(itemStack)) {
                sum += stack.getAmount();
                if (sum >= i) return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        for (int i = 0; i < getStorageContents().length; i++) {
            ItemStack stack = getStorageContents()[i];
            if (stack != null && stack.getType() == material) {
                result.put(i, stack);
            }
        }
        return result;
    }

    @NotNull
    @Override
    public HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack itemStack) {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        for (int i = 0; i < getStorageContents().length; i++) {
            ItemStack stack = getStorageContents()[i];
            if (Objects.equals(stack, itemStack)) {
                result.put(i, stack);
            }
        }
        return result;
    }

    @Override
    public int first(@NotNull Material material) throws IllegalArgumentException {
        for (int i = 0; i < getStorageContents().length; i++) {
            ItemStack stack = getStorageContents()[i];
            if (stack != null && stack.getType() == material) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int first(@NotNull ItemStack itemStack) {
        for (int i = 0; i < getStorageContents().length; i++) {
            ItemStack stack = getStorageContents()[i];
            if (Objects.equals(stack, itemStack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int firstEmpty() {
        for (int i = 0; i < getStorageContents().length; i++) {
            ItemStack stack = getStorageContents()[i];
            if (stack == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return firstEmpty() == -1;
    }

    @Override
    public void remove(@NotNull Material material) throws IllegalArgumentException {
        for (int i = 0; i < getStorageContents().length; i++) {
            ItemStack stack = getStorageContents()[i];
            if (stack.getType() == material) {
                items[i] = null;
            }
        }
    }

    @Override
    public void remove(@NotNull ItemStack itemStack) {
        for (int i = 0; i < getStorageContents().length; i++) {
            ItemStack stack = getStorageContents()[i];
            if (Objects.equals(itemStack, stack)) {
                items[i] = null;
            }
        }
    }

    @Override
    public void clear(int i) {
        getStorageContents()[i] = null;
    }

    @Override
    public void clear() {
        Arrays.fill(getStorageContents(), null);
    }

    @NotNull
    @Override
    public List<HumanEntity> getViewers() {
        if (viewers == null) viewers = new ArrayList<>(1);
        return viewers;
    }

    @NotNull
    @Override
    public InventoryType getType() {
        return type;
    }

    @Nullable
    @Override
    public InventoryHolder getHolder() {
        return holder;
    }

    @NotNull
    @Override
    public ListIterator<ItemStack> iterator() {
        return iterator(0);
    }

    @NotNull
    @Override
    public ListIterator<ItemStack> iterator(int i) {
        return new ListIterator<ItemStack>() {
            int cursor = i;

            @Override
            public boolean hasNext() {
                return cursor < items.length;
            }

            @Override
            public ItemStack next() {
                return items[cursor++];
            }

            @Override
            public boolean hasPrevious() {
                return cursor > 0;
            }

            @Override
            public ItemStack previous() {
                return items[--cursor];
            }

            @Override
            public int nextIndex() {
                return cursor;
            }

            @Override
            public int previousIndex() {
                return cursor - 1;
            }

            @Override
            public void remove() {
                items[cursor] = null;
            }

            @Override
            public void set(ItemStack itemStack) {
                items[cursor] = itemStack;
            }

            @Override
            public void add(ItemStack itemStack) {
                throw new UnsupportedOperationException("cannot add items to an inventory iterator");
            }
        };
    }

    @Nullable
    @Override
    public Location getLocation() {
        InventoryHolder inventoryHolder = getHolder();
        if (inventoryHolder instanceof Entity) {
            return ((Entity) inventoryHolder).getLocation();
        } else if (inventoryHolder instanceof BlockState) {
            return ((BlockState) inventoryHolder).getLocation();
        } else {
            //I don't think there are any more options.
            return null;
        }
    }
}
