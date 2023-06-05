package com.janboerman.invsee.spigot.addon.give.glowstone;

import com.janboerman.invsee.spigot.addon.give.common.GiveApi;

import org.bukkit.inventory.ItemStack;

public class GiveImpl implements GiveApi /*TODO extends NeditImpl*/ {

    public static final GiveImpl INSTANCE = new GiveImpl();

    private GiveImpl() {
    }

    @Override
    public ItemStack applyTag(ItemStack stack, String tag) {
        return stack; //TODO convert NBT tag to ItemMeta, set on stack.
        //TODO Glowstone does not have this functionality implemented, so I will have to write my own.
    }

}
