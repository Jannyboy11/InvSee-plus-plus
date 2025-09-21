package com.janboerman.invsee.spigot.addon.give.impl_1_21_7_R5;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;

final class ItemParser {

    private static final CommandBuildContext COMMAND_BUILD_CONTEXT = getContext();

    private ItemParser() {
    }

    static ItemInput parseItemType(String text) throws CommandSyntaxException {
        // Could be using CraftItemFactory to create an ItemStack from a string directly?
        ItemArgument argument = new ItemArgument(COMMAND_BUILD_CONTEXT);
        ItemInput itemInput = argument.parse(new StringReader(text));
        return itemInput;
    }

    private static CommandBuildContext getContext() {
        HolderLookup.Provider provider = PaperSupport.getDefaultRegistry();
        return Commands.createValidationContext(provider);
    }
}
