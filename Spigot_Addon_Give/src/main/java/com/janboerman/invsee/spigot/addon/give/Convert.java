package com.janboerman.invsee.spigot.addon.give;

import com.janboerman.invsee.utils.Either;
import org.bukkit.Material;

import java.util.UUID;

class Convert {

    private Convert() {}

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

    /*
    static Either<String, ItemStack[]> convertItems(Material itemType, String input) {
        assert itemType != null && input != null;

        Either<String, Integer> eitherAmount = convertAmount(input);
        if (eitherAmount.isLeft()) return (Either<String, ItemStack[]>) (Either) eitherAmount;
        assert eitherAmount.isRight() && eitherAmount.getRight() != null;
        int amount = eitherAmount.getRight();

        int maxStackSizeForMaterial = itemType.getMaxStackSize();
        int amountOfStacks = amount / maxStackSizeForMaterial;
        int remainder = amount % maxStackSizeForMaterial;
        if (remainder != 0) {
            amountOfStacks += 1;
        }
        ItemStack[] stacks = new ItemStack[amountOfStacks];
        for (int i = 0; i < amountOfStacks; i++) {
            stacks[i] = new ItemStack(itemType, maxStackSizeForMaterial);
        }
        if (remainder != 0) {
            stacks[stacks.length - 1] = new ItemStack(itemType, remainder);
        }
        return Either.right(stacks);
    }
     */

}
