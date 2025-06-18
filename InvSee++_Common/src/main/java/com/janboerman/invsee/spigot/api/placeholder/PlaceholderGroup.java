package com.janboerman.invsee.spigot.api.placeholder;

public enum PlaceholderGroup {

    INACCESSIBLE,
    ARMOUR,
    OFFHAND,
    BODY,
    SADDLE,
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
            case CRAFTING:
            case ANVIL:
            case MERCHANT:
            case CARTOGRAPHY:
            case ENCHANTING:
            case GRINDSTONE:
            case LOOM:
            case SMITHING:
            case STONECUTTER:
                return true;
            default:
                return false;
        }
    }
}
