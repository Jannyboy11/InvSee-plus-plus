package com.janboerman.invsee.spigot.api.logging;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Difference {

    //positive values: items were ADDED to the spectator inventory.
    //negative values: items were REMOVED from the spectator inventory.
    private Map<ItemType, Integer> diffs;

    public Difference() {
        this.diffs = new LinkedHashMap<>();
    }

    public Map<ItemType, Integer> getDifference() {
        return Collections.unmodifiableMap(diffs);
    }

    public boolean isEmpty() {
        return diffs == null || diffs.isEmpty() || diffs.values().stream().allMatch(Objects::isNull);
    }

    public void merge(Difference other) {
        for (var entry : other.diffs.entrySet()) {
            accumulate(entry.getKey(), entry.getValue());
        }
    }

    public void accumulate(ItemStack stack) {
        if (stack == null) return;

        accumulate(ItemType.of(stack), stack.getAmount());
    }

    public void accumulate(ItemType itemType, int amount) {
        if (itemType == null || amount == 0) return;

        diffs.merge(itemType, amount, (x, y) -> {
            int sum = x + y;
            return sum == 0 ? null : sum;
        });
    }

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
                res.accumulate(newStack);
                res.accumulate(oldStack);
            }
        }

        return res;
    }


}
