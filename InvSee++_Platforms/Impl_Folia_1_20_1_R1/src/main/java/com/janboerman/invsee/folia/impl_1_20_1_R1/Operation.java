package com.janboerman.invsee.folia.impl_1_20_1_R1;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;

sealed interface Operation permits Edit, ResetAt, ResetAll {

    //TODO add operation id



}

//TODO record ?
final class Edit implements Operation {
    final Collection<Entry> edits;

    Edit(Collection<Entry> edits) { this.edits = edits; }

    static record Entry(int index, ItemStack from, ItemStack to) {}
}

final class ResetAt implements Operation {
    final Map<Integer, ItemStack> newValues;

    ResetAt(Map<Integer, ItemStack> newValues) { this.newValues = newValues; }
}

//TODO record ?
final class ResetAll implements Operation {
    final List<ItemStack> newValues;

    ResetAll(List<ItemStack> newValues) { this.newValues = newValues; }
}