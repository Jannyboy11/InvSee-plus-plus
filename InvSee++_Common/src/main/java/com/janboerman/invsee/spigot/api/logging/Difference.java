package com.janboerman.invsee.spigot.api.logging;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The Difference of a {@link com.janboerman.invsee.spigot.api.SpectatorInventoryView} is the set of items that were added and removed the {@link com.janboerman.invsee.spigot.api.SpectatorInventory}.
 */
public class Difference {

    //positive values: items were ADDED to the spectator inventory.
    //negative values: items were REMOVED from the spectator inventory.
    private Map<ItemType, Integer> diffs;

    public Difference() {
        this.diffs = new LinkedHashMap<>();
    }

    /**
     * Get the difference map. Keys represent items, values represent how many items were added to the {@Linkplain SpectatorInventory}. Negative values represent removed items.
     * @return the difference map
     */
    public Map<ItemType, Integer> getDifference() {
        return Collections.unmodifiableMap(diffs);
    }

    /**
     * Get whether the difference is zero (no items added or removed).
     * @return true if the difference is zero
     */
    public boolean isEmpty() {
        return diffs == null || diffs.isEmpty() || diffs.values().stream().allMatch(Objects::isNull);
    }

    /**
     * Merge this difference with another: add all differences from the other Difference to this Difference.
     * @param other the other difference
     */
    public void merge(Difference other) {
        for (Map.Entry<ItemType, Integer> entry : other.diffs.entrySet()) {
            accumulate(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add an item to this difference.
     * @param stack the item
     */
    public void accumulate(ItemStack stack) {
        if (stack == null) return;

        accumulate(ItemType.of(stack), stack.getAmount());
    }

    /**
     * Add an item to this Difference.
     * @param itemType the item
     * @param amount how many of the item
     */
    public void accumulate(ItemType itemType, int amount) {
        if (itemType == null || amount == 0) return;

        diffs.merge(itemType, amount, (x, y) -> {
            int sum = x + y;
            return sum == 0 ? null : sum;
        });
    }

    /**
     * Calculate the Difference from a 'before' and 'after' view of a list of items.
     * @param before the 'before' view
     * @param after the 'after' view
     * @return the difference
     */
    public static Difference calculate(List<ItemStack> before, List<ItemStack> after) {
        if (before.size() != after.size())
            throw new IllegalArgumentException("'before' and 'after' lists must have the same sizes");

        final Difference res = new Difference();

        final Iterator<ItemStack> one = before.iterator();
        final Iterator<ItemStack> two = after.iterator();

        while (one.hasNext()) {
            assert two.hasNext();

            final ItemStack oldStack = one.next();
            final ItemStack newStack = two.next();

            if (!Objects.equals(oldStack, newStack)) {
                res.accumulate(ItemType.of(newStack), newStack.getAmount());
                res.accumulate(ItemType.of(oldStack), -oldStack.getAmount());
            }
        }

        return res;
    }


}
