package com.janboerman.invsee.spigot.addon.clear;

import com.janboerman.invsee.utils.Either;

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

}
