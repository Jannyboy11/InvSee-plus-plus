package com.janboerman.invsee.spigot.api.placeholder;

public enum PlaceholderGroup {

    INACCESSIBLE,
    ARMOUR,
    OFFHAND,
    CURSOR,
    CRAFTING,
    ANVIL,
    MERCHANT,
    CARTOGRAPHY,
    ENCHANTING,
    GRINDSTONE,
    LOOM,
    SMITHING,
    STONECUTTER;

    public boolean isPersonal() {
        switch (this) {
            case INACCESSIBLE:
            case ARMOUR:
            case OFFHAND:
            case CURSOR:
                return false;
            default:
                return true;
        }
    }
}
