package com.janboerman.invsee.spigot.addon.clear;

import com.janboerman.invsee.utils.Either;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import java.util.UUID;

class Convert {

    static Either<UUID, String> convertPlayer(String input) {
        assert input != null;

        try {
            UUID uuid = UUID.fromString(input);
            return Either.left(uuid);
        } catch (IllegalArgumentException e) {
        }

        return Either.right(input);
    }

    static Either<String, ItemType> convertItemType(String input) {
        assert input != null;

        String materialName;
        Byte dataValue;

        final int colonIndex = input.indexOf(':');
        if (colonIndex != -1) {
            materialName = input.substring(0, colonIndex);
            try {
                dataValue = Byte.parseByte(input.substring(colonIndex + 1));
            } catch (NumberFormatException ignored) {
                dataValue = null;
            }
        } else {
            materialName = input;
            dataValue = null;
        }

        Material material = Material.matchMaterial(materialName);
        if (material != null) {
            if (dataValue != null) {
                return Either.right(ItemType.withData(material, dataValue));
            } else {
                return Either.right(ItemType.plain(material));
            }
        } else {
            try {
                NamespacedKey tagKey = NamespacedKey.fromString(materialName, null);
                Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, tagKey, Material.class);
                if (tag != null) {
                    return Either.right(ItemType.fromTag(tag));
                }
                tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, tagKey, Material.class);
                if (tag != null) {
                    return Either.right(ItemType.fromTag(tag));
                }
            } catch (NoClassDefFoundError | NoSuchMethodError legacyServerIgnored) {
            }

            return Either.left("Material " + input + " does not exist.");
        }
    }

    static Either<String, Integer> convertAmount(String input) {
        assert input != null;

        try {
            int value = Integer.parseInt(input);
            if (value > 0) {
                return Either.right(value);
            } else {
                return Either.left(input + " is not a positive integer.");
            }
        } catch (IllegalArgumentException e) {
            return Either.left(input + " is not a positive integer.");
        }
    }

}
