package com.janboerman.invsee.spigot.addon.clear;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

interface ItemType {

    public static ItemType plain(Material material) {
        return new Plain(material);
    }

    public static ItemType withData(Material material, byte data) {
        return new WithData(material, data);
    }

    //

    public void removeAllFrom(Inventory inventory);

    public int removeAtMostFrom(Inventory inventory, int atMost);

    //

    static class Plain implements ItemType {
        private final Material material;

        Plain(Material material) {
            this.material = material;
        }

        @Override
        public void removeAllFrom(Inventory inventory) {
            inventory.remove(material);
        }

        @Override
        public int removeAtMostFrom(Inventory inventory, int atMost) {
            return RemoveUtil.removeAtMost(inventory, material, atMost);
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
        public void removeAllFrom(Inventory inventory) {
            RemoveUtil.removeIf(inventory, stack -> stack.getType() == material && stack.getDurability() == (short) data);
        }

        @Override
        public int removeAtMostFrom(Inventory inventory, int atMost) {
            return RemoveUtil.removeIfAtMost(inventory, stack-> stack.getType() == material && stack.getDurability() == (short) data, atMost);
        }
    }

}
