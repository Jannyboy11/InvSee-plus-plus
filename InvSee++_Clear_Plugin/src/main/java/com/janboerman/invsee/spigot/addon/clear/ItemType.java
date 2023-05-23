package com.janboerman.invsee.spigot.addon.clear;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

interface ItemType {

    public static ItemType plain(Material material) {
        return new Plain(material);
    }

    public static ItemType withData(Material material, byte data) {
        return new WithData(material, data);
    }

    public static ItemType fromTag(Tag<Material> tag) {
        return new FromTag(tag);
    }

    //

    public void removeAllFrom(Inventory inventory);

    public int removeAtMostFrom(Inventory inventory, int atMost);

    //

    static class Plain implements ItemType {
        private final Material material;

        Plain(Material material) {
            this.material = Objects.requireNonNull(material);
        }

        @Override
        public void removeAllFrom(Inventory inventory) {
            inventory.remove(material);
        }

        @Override
        public int removeAtMostFrom(Inventory inventory, int atMost) {
            return RemoveUtil.removeAtMost(inventory, material, atMost);
        }

        @Override
        public String toString() {
            return material.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Plain)) return false;

            Plain that = (Plain) o;
            return this.material == that.material;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(material);
        }
    }

    static class WithData implements ItemType {
        private final Material material;
        private final byte data;

        WithData(Material material, byte data) {
            this.material = Objects.requireNonNull(material);
            this.data = data;
        }

        @Override
        public void removeAllFrom(Inventory inventory) {
            RemoveUtil.removeIf(inventory, stack -> stack.getType() == material && stack.getDurability() == (short) data);
        }

        @Override
        public int removeAtMostFrom(Inventory inventory, int atMost) {
            return RemoveUtil.removeIfAtMost(inventory, stack -> stack.getType() == material && stack.getDurability() == (short) data, atMost);
        }

        @Override
        public String toString() {
            return material.toString() + ":" + data;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof WithData)) return false;

            WithData that = (WithData) o;
            return this.material == that.material && this.data == that.data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(material, data);
        }
    }

    static class FromTag implements ItemType {
        private final Tag<Material> tag;

        FromTag(Tag<Material> tag) {
            this.tag = tag;
        }


        @Override
        public void removeAllFrom(Inventory inventory) {
            RemoveUtil.removeIf(inventory, stack -> tag.isTagged(stack.getType()));
        }

        @Override
        public int removeAtMostFrom(Inventory inventory, int atMost) {
            return RemoveUtil.removeIfAtMost(inventory, stack -> tag.isTagged(stack.getType()), atMost);
        }

        @Override
        public String toString() {
            return tag.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof FromTag)) return false;

            FromTag that = (FromTag) o;
            return Objects.equals(this.tag, that.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(tag);
        }
    }

}
