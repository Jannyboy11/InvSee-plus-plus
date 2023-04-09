package com.janboerman.invsee.spigot.api.template;

/** Represents a slot of an {@link com.janboerman.invsee.spigot.api.EnderSpectatorInventory}. */
public enum EnderChestSlot {

    //count until 54 because of Purpur!

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
    CONTAINER_36,
    CONTAINER_37,
    CONTAINER_38,
    CONTAINER_39,
    CONTAINER_40,
    CONTAINER_41,
    CONTAINER_42,
    CONTAINER_43,
    CONTAINER_44,
    CONTAINER_45,
    CONTAINER_46,
    CONTAINER_47,
    CONTAINER_48,
    CONTAINER_49,
    CONTAINER_50,
    CONTAINER_51,
    CONTAINER_52,
    CONTAINER_53;

    private static final EnderChestSlot[] VALUES = values();

    /** Get the index of the {@link com.janboerman.invsee.spigot.api.EnderSpectatorInventory} at which this slot resides. */
    public int defaultIndex() {
       return ordinal();
    }

    /** Get the slot given its index in the {@link com.janboerman.invsee.spigot.api.EnderSpectatorInventory}. */
    public static EnderChestSlot byDefaultIndex(int index) {
        if (0 <= index && index < 54) {
            return VALUES[index];
        } else {
            return null;
        }
    }

}
