package com.janboerman.invsee.spigot.addon.clear;

import com.janboerman.invsee.utils.Either;
import org.bukkit.Material;

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

    static Either<String, Material> convertItemType(String input) {
        assert input != null;

        Material material = Material.matchMaterial(input);
        if (material != null) {
            return Either.right(material);
        } else {
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
