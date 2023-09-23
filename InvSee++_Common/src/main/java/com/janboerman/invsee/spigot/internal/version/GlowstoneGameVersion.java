package com.janboerman.invsee.spigot.internal.version;

import java.lang.reflect.Field;

public class GlowstoneGameVersion {

    private static final String GLOWSERVER_CLASS_NAME = "org.glowstone.GlowServer";
    private static final String GAME_VERSION_FIELD_NAME = "GAME_VERSION";

    static final String _1_8_8 = "1.8.8";
    static final String _1_8_9 = "1.8.9";
    static final String _1_12_2 = "1.12.2";

    private GlowstoneGameVersion() {
    }

    public static String getGameVersion() {
        try {
            Class<?> glowServerClass = Class.forName(GLOWSERVER_CLASS_NAME);
            Field gameVersionField = glowServerClass.getField(GAME_VERSION_FIELD_NAME);
            return String.class.cast(gameVersionField.get(null));
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            assert false : "Not running on GlowStone.";
            return null;
        }
    }
}
