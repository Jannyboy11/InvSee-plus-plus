package com.janboerman.invsee.spigot.addon.give.common;

import com.janboerman.invsee.utils.Either;

import org.bukkit.inventory.ItemStack;

public interface GiveApi {

    public ItemStack applyTag(ItemStack stack, String tag);
    
    public Either<String, ItemType> parseItemType(String itemType);

    public int maxStackSize();

}
