package com.janboerman.invsee.paper.addon.give.impl_1_21_11;

import com.janboerman.invsee.spigot.addon.give.common.ItemType;
import com.janboerman.invsee.utils.Either;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

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
