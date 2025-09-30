package com.janboerman.invsee.spigot.addon.give.impl_1_21_3_R2;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;

final class ItemParser {

    private static final CommandBuildContext COMMAND_BUILD_CONTEXT = getContext();

    private ItemParser() {
    }

    static ItemInput parseItemType(String text) throws CommandSyntaxException {
        ItemArgument argument = new ItemArgument(COMMAND_BUILD_CONTEXT);
        ItemInput itemInput = argument.parse(new StringReader(text));
        return itemInput;
    }

    private static CommandBuildContext getContext() {
        // HolderLookup.Provider provider = VanillaRegistries.createLookup(); //does not allow access to item components? should I get another one?
        HolderLookup.Provider provider = MinecraftServer.getDefaultRegistryAccess(); //can we avoid this deprecated method?
        return Commands.createValidationContext(provider);
    }
}
