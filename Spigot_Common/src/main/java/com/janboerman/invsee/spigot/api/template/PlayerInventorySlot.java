package com.janboerman.invsee.spigot.api.template;

public enum PlayerInventorySlot {

    CONTAINER_00,
    CONTAINER_01,
    CONTAINER_02,
    CONTAINER_03,
    CONTAINER_04,
    CONTAINER_05,
    CONTAINER_06,
    CONTAINER_07,
    CONTAINER_08,
    CONTAINER_09,
    CONTAINER_10,
    CONTAINER_11,
    CONTAINER_12,
    CONTAINER_13,
    CONTAINER_14,
    CONTAINER_15,
    CONTAINER_16,
    CONTAINER_17,
    CONTAINER_18,
    CONTAINER_19,
    CONTAINER_20,
    CONTAINER_21,
    CONTAINER_22,
    CONTAINER_23,
    CONTAINER_24,
    CONTAINER_25,
    CONTAINER_26,
    CONTAINER_27,
    CONTAINER_28,
    CONTAINER_29,
    CONTAINER_30,
    CONTAINER_31,
    CONTAINER_32,
    CONTAINER_33,
    CONTAINER_34,
    CONTAINER_35,

    ARMOUR_BOOTS,
    ARMOUR_LEGGINGS,
    ARMOUR_CHESTPLATE,
    ARMOUR_HELMET,

    OFFHAND,

    CURSOR,

    PERSONAL_00,
    PERSONAL_01,
    PERSONAL_02,
    PERSONAL_03,
    PERSONAL_04,
    PERSONAL_05,
    PERSONAL_06,
    PERSONAL_07,
    PERSONAL_08;

    private static final PlayerInventorySlot[] VALUES = values();

    public boolean isContainer() {
        switch (this) {
            case CONTAINER_00:
            case CONTAINER_01:
            case CONTAINER_02:
            case CONTAINER_03:
            case CONTAINER_04:
            case CONTAINER_05:
            case CONTAINER_06:
            case CONTAINER_07:
            case CONTAINER_08:
            case CONTAINER_09:
            case CONTAINER_10:
            case CONTAINER_11:
            case CONTAINER_12:
            case CONTAINER_13:
            case CONTAINER_14:
            case CONTAINER_15:
            case CONTAINER_16:
            case CONTAINER_17:
            case CONTAINER_18:
            case CONTAINER_19:
            case CONTAINER_20:
            case CONTAINER_21:
            case CONTAINER_22:
            case CONTAINER_23:
            case CONTAINER_24:
            case CONTAINER_25:
            case CONTAINER_26:
            case CONTAINER_27:
            case CONTAINER_28:
            case CONTAINER_29:
            case CONTAINER_30:
            case CONTAINER_31:
            case CONTAINER_32:
            case CONTAINER_33:
            case CONTAINER_34:
            case CONTAINER_35:
                return true;
            default:
                return false;
        }
    }

    public boolean isArmour() {
        switch (this) {
            case ARMOUR_BOOTS:
            case ARMOUR_LEGGINGS:
            case ARMOUR_CHESTPLATE:
            case ARMOUR_HELMET:
                return true;
            default:
                return false;
        }
    }

    public boolean isPersonal() {
        switch (this) {
            case PERSONAL_00:
            case PERSONAL_01:
            case PERSONAL_02:
            case PERSONAL_03:
            case PERSONAL_04:
            case PERSONAL_05:
            case PERSONAL_06:
            case PERSONAL_07:
            case PERSONAL_08:
                return true;
            default:
                return false;
        }
    }

    public boolean isOffHand() {
        return this == OFFHAND;
    }

    public boolean isCursor() {
        return this == CURSOR;
    }

    public int defaultIndex() {
        if (isContainer()) {
            return 0 + ordinal() - CONTAINER_00.ordinal();
        } else if (isArmour()) {
            return 36 + ordinal() - ARMOUR_BOOTS.ordinal();
        } else if (isOffHand()) {
            return 40;
        } else if (isCursor()) {
            return 41;
        } else {
            return 45 + ordinal() - PERSONAL_00.ordinal();
        }
    }

    public static PlayerInventorySlot byDefaultIndex(int index) {
        if (0 <= index && index <= 41) {
            return VALUES[index]; //storage, armour, offhand, cursor
        } else if (45 <= index && index < 54) {
            return VALUES[index + PERSONAL_00.ordinal() - 45]; //personal
        } else {
            return null; //unused slots, or out of bounds
        }
    }

}
