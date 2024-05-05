package com.janboerman.invsee.spigot.addon.give.common;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import com.janboerman.invsee.utils.Either;

public interface ItemType {

    // TODO convert from Spigot's 'native' https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemType.html

    public static ItemType plain(Material material) {
        return new Plain(material);
    }

    public static ItemType withData(Material material, byte data) {
        return new WithData(material, data);
    }

    public Either<String, ItemStack> toItemStack(int amount);


    //

    static class Plain implements ItemType {
        private final Material material;

        Plain(Material material) {
            this.material = Objects.requireNonNull(material);
        }

        @Override
        public Either<String, ItemStack> toItemStack(int amount) {
            return Either.right(new ItemStack(material, amount));
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
        public Either<String, ItemStack> toItemStack(int amount) {
            return Either.right(new ItemStack(material, amount, /* damage= */ (short) 0, data));
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

}
