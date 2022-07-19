package com.janboerman.invsee.spigot.addon.give.common;

import me.nullicorn.nedit.SNBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public abstract class NeditImpl implements GiveApi {

    protected NeditImpl() {
    }

    protected abstract ItemStack applyTag(ItemStack stack, NBTCompound tag);

    @Override
    public final ItemStack applyTag(ItemStack stack, String tag) {
        try {
            return applyTag(stack, SNBTReader.readCompound(tag));
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid nbt: " + tag);
        }
    }

}
