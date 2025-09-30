package com.janboerman.invsee.spigot.addon.give.impl_1_21_9_R6;

import com.janboerman.invsee.spigot.addon.give.common.ItemType;
import com.janboerman.invsee.utils.Either;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.bukkit.craftbukkit.v1_21_R6.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.minecraft.commands.arguments.item.ItemInput;

final class WithComponents implements ItemType {

    private final ItemInput input;

    WithComponents(ItemInput input) {
        this.input = input;
    }

    @Override
    public Either<String, ItemStack> toItemStack(int amount) {
        try {
            return Either.right(CraftItemStack.asCraftMirror(input.createItemStack(amount, false/*don't check stack size*/)));
        } catch (CommandSyntaxException e) {
            // This shouldn't really ever happen since we never check the count against the max stack size.
            return Either.left(e.getMessage());
        }
    }
}
