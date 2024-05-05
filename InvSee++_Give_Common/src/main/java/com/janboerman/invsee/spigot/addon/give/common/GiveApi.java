package com.janboerman.invsee.spigot.addon.give.common;

import com.janboerman.invsee.utils.Either;

import org.bukkit.inventory.ItemStack;

public interface GiveApi {

    public ItemStack applyTag(ItemStack stack, String tag);

    // TODO 1.20.5:
    // TODO in nedit impls, implement parseItemStack using Nedit. applyTag can still exist as an abstractMethod in neditImpl
    // TODO the nedit impl will implement parseItemStack in terms of applyTag.
    
    public Either<String, ItemType> parseItemType(String itemType);

}
