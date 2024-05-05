package com.janboerman.invsee.spigot.addon.give.common;

import com.janboerman.invsee.utils.Either;
import org.bukkit.Material;

import java.util.UUID;

public class Convert {

    private Convert() {}

    public static Either<UUID, String> convertPlayer(String input) {
        assert input != null;

        try {
            UUID uuid = UUID.fromString(input);
            return Either.left(uuid);
        } catch (IllegalArgumentException e) {
        }

        return Either.right(input);
    }

    public static Either<String, ItemType> convertItemType(String input) {
        assert input != null;

        String materialName;
        Byte dataValue;

        final int colonIndex = input.indexOf(':');
        if (colonIndex != -1) {
            materialName = input.substring(0, colonIndex);
            try {
                dataValue = Byte.parseByte(input.substring(colonIndex + 1));
            } catch (NumberFormatException e) {
                return Either.left(e.getMessage());
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
            return Either.left("Material " + input + " does not exist.");
        }
    }

    public static Either<String, Integer> convertAmount(String input) {
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
