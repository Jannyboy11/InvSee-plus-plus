package com.janboerman.invsee.spigot.internal.version;

public enum MinecraftPlatform {

    CRAFTBUKKIT("CraftBukkit"),
    GLOWSTONE("Glowstone"),
    PAPER("Paper");
    //TODO Folia

    private final String name;

    private MinecraftPlatform(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
