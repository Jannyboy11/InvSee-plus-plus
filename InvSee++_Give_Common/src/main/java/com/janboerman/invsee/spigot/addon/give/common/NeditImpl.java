package com.janboerman.invsee.spigot.addon.give.common;

import me.nullicorn.nedit.SNBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

import com.janboerman.invsee.utils.Either;

public abstract class NeditImpl implements GiveApi {

    protected NeditImpl() {
    }

    @Override
    public final ItemStack applyTag(ItemStack stack, String tag) {
        try {
            return applyTag(stack, SNBTReader.readCompound(tag));
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid nbt: " + tag);
        }
    }

    @Override
    public final Either<String, ItemType> parseItemType(String itemType) {
        return Convert.convertItemType(itemType);
    }

    protected abstract ItemStack applyTag(ItemStack stack, NBTCompound tag);

}
