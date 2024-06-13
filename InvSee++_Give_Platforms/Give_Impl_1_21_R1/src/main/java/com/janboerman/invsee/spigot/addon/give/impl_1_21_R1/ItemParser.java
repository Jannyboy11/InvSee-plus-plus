package com.janboerman.invsee.spigot.addon.give.impl_1_21_R1;

import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;

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
        HolderLookup.Provider provider = VanillaRegistries.createLookup();

        return new CommandBuildContext() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
                return provider.listRegistries();
            }

            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return provider.lookup(resourceKey);
            }
        };
    }

}
