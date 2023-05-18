package com.janboerman.invsee.spigot.addon.give;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

interface ItemType {

    public static ItemType plain(Material material) {
        return new Plain(material);
    }

    public static ItemType withData(Material material, byte data) {
        return new WithData(material, data);
    }

    //

    ItemStack toItemStack(int amount);


    //

    static class Plain implements ItemType {
        private final Material material;

        Plain(Material material) {
            this.material = material;
        }

        @Override
        public ItemStack toItemStack(int amount) {
            return new ItemStack(material, amount);
        }
    }

    static class WithData implements ItemType {
        private final Material material;
        private final byte data;

        WithData(Material material, byte data) {
            this.material = material;
            this.data = data;
        }

        @Override
        public ItemStack toItemStack(int amount) {
            return new ItemStack(material, amount, /* damage= */ (short) 0, data);
        }
    }

}
