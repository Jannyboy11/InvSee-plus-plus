package com.janboerman.invsee.spigot.template;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.internal.template.PlayerInventoryMirror;
import com.janboerman.invsee.spigot.internal.template.EnderChestMirror;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MirrorTest {

    // Never mind these red squiggly underlines: This is a bug in IntelliJ!
    private static final String INVENTORY_TEMPLATE = """
        i_00 i_01 i_02 i_03 i_04 i_05 i_06 i_07 i_08
        i_09 i_10 i_11 i_12 i_13 i_14 i_15 i_16 i_17
        i_18 i_19 i_20 i_21 i_22 i_23 i_24 i_25 i_26
        i_27 i_28 i_29 i_30 i_31 i_32 i_33 i_34 i_35
        a_b  a_l  a_c  a_h  oh   b    s    c    _  \s
        p_00 p_01 p_02 p_03 p_04 p_05 p_06 p_07 p_08
        """;

    private static final String ENDERCHEST_TEMPLATE = """
        e_00 e_01 e_02 e_03 e_04 e_05 e_06 e_07 e_08
        e_09 e_10 e_11 e_12 e_13 e_14 e_15 e_16 e_17
        e_18 e_19 e_20 e_21 e_22 e_23 e_24 e_25 e_26
        e_27 e_28 e_29 e_30 e_31 e_32 e_33 e_34 e_35
        e_36 e_37 e_38 e_39 e_40 e_41 e_42 e_43 e_44
        e_45 e_46 e_47 e_48 e_49 e_50 e_51 e_52 e_53
        """;

    private static final PlayerInventorySlot[] DEFAULT_PLAYER_INVENTORY_TEMPLATE = new PlayerInventorySlot[] {
        PlayerInventorySlot.CONTAINER_00, PlayerInventorySlot.CONTAINER_01, PlayerInventorySlot.CONTAINER_02, PlayerInventorySlot.CONTAINER_03, PlayerInventorySlot.CONTAINER_04, PlayerInventorySlot.CONTAINER_05, PlayerInventorySlot.CONTAINER_06, PlayerInventorySlot.CONTAINER_07, PlayerInventorySlot.CONTAINER_08,
        PlayerInventorySlot.CONTAINER_09, PlayerInventorySlot.CONTAINER_10, PlayerInventorySlot.CONTAINER_11, PlayerInventorySlot.CONTAINER_12, PlayerInventorySlot.CONTAINER_13, PlayerInventorySlot.CONTAINER_14, PlayerInventorySlot.CONTAINER_15, PlayerInventorySlot.CONTAINER_16, PlayerInventorySlot.CONTAINER_17,
        PlayerInventorySlot.CONTAINER_18, PlayerInventorySlot.CONTAINER_19, PlayerInventorySlot.CONTAINER_20, PlayerInventorySlot.CONTAINER_21, PlayerInventorySlot.CONTAINER_22, PlayerInventorySlot.CONTAINER_23, PlayerInventorySlot.CONTAINER_24, PlayerInventorySlot.CONTAINER_25, PlayerInventorySlot.CONTAINER_26,
        PlayerInventorySlot.CONTAINER_27, PlayerInventorySlot.CONTAINER_28, PlayerInventorySlot.CONTAINER_29, PlayerInventorySlot.CONTAINER_30, PlayerInventorySlot.CONTAINER_31, PlayerInventorySlot.CONTAINER_32, PlayerInventorySlot.CONTAINER_33, PlayerInventorySlot.CONTAINER_34, PlayerInventorySlot.CONTAINER_35,
        PlayerInventorySlot.ARMOUR_BOOTS, PlayerInventorySlot.ARMOUR_LEGGINGS, PlayerInventorySlot.ARMOUR_CHESTPLATE, PlayerInventorySlot.ARMOUR_HELMET, PlayerInventorySlot.OFFHAND, PlayerInventorySlot.BODY, PlayerInventorySlot.SADDLE, PlayerInventorySlot.CURSOR, null,
        PlayerInventorySlot.PERSONAL_00, PlayerInventorySlot.PERSONAL_01, PlayerInventorySlot.PERSONAL_02, PlayerInventorySlot.PERSONAL_03, PlayerInventorySlot.PERSONAL_04, PlayerInventorySlot.PERSONAL_05, PlayerInventorySlot.PERSONAL_06, PlayerInventorySlot.PERSONAL_07, PlayerInventorySlot.PERSONAL_08
    };

    private static final EnderChestSlot[] DEFAULT_ENDER_CHEST_TEMPLATE = new EnderChestSlot[] {
        EnderChestSlot.CONTAINER_00, EnderChestSlot.CONTAINER_01, EnderChestSlot.CONTAINER_02, EnderChestSlot.CONTAINER_03, EnderChestSlot.CONTAINER_04, EnderChestSlot.CONTAINER_05, EnderChestSlot.CONTAINER_06, EnderChestSlot.CONTAINER_07, EnderChestSlot.CONTAINER_08,
        EnderChestSlot.CONTAINER_09, EnderChestSlot.CONTAINER_10, EnderChestSlot.CONTAINER_11, EnderChestSlot.CONTAINER_12, EnderChestSlot.CONTAINER_13, EnderChestSlot.CONTAINER_14, EnderChestSlot.CONTAINER_15, EnderChestSlot.CONTAINER_16, EnderChestSlot.CONTAINER_17,
        EnderChestSlot.CONTAINER_18, EnderChestSlot.CONTAINER_19, EnderChestSlot.CONTAINER_20, EnderChestSlot.CONTAINER_21, EnderChestSlot.CONTAINER_22, EnderChestSlot.CONTAINER_23, EnderChestSlot.CONTAINER_24, EnderChestSlot.CONTAINER_25, EnderChestSlot.CONTAINER_26,
        EnderChestSlot.CONTAINER_27, EnderChestSlot.CONTAINER_28, EnderChestSlot.CONTAINER_29, EnderChestSlot.CONTAINER_30, EnderChestSlot.CONTAINER_31, EnderChestSlot.CONTAINER_32, EnderChestSlot.CONTAINER_33, EnderChestSlot.CONTAINER_34, EnderChestSlot.CONTAINER_35,
        EnderChestSlot.CONTAINER_36, EnderChestSlot.CONTAINER_37, EnderChestSlot.CONTAINER_38, EnderChestSlot.CONTAINER_39, EnderChestSlot.CONTAINER_40, EnderChestSlot.CONTAINER_41, EnderChestSlot.CONTAINER_42, EnderChestSlot.CONTAINER_43, EnderChestSlot.CONTAINER_44,
        EnderChestSlot.CONTAINER_45, EnderChestSlot.CONTAINER_46, EnderChestSlot.CONTAINER_47, EnderChestSlot.CONTAINER_48, EnderChestSlot.CONTAINER_49, EnderChestSlot.CONTAINER_50, EnderChestSlot.CONTAINER_51, EnderChestSlot.CONTAINER_52, EnderChestSlot.CONTAINER_53
    };

    @Test
    public void testInventory() {
        PlayerInventoryMirror template = new PlayerInventoryMirror(INVENTORY_TEMPLATE);
        assertArrayEquals(DEFAULT_PLAYER_INVENTORY_TEMPLATE, template.getSlots());
    }

    @Test
    public void testEnderChest() {
        EnderChestMirror template = new EnderChestMirror(ENDERCHEST_TEMPLATE);
        assertArrayEquals(DEFAULT_ENDER_CHEST_TEMPLATE, template.getSlots());
    }

    @Test
    public void testInventoryDefaultIndices() {
        for (PlayerInventorySlot slot : PlayerInventorySlot.values()) {
            assertEquals(slot, PlayerInventorySlot.byDefaultIndex(slot.defaultIndex()));
        }
    }

    @Test
    public void testEnderChestDefaultIndices() {
        for (EnderChestSlot slot : EnderChestSlot.values()) {
            assertEquals(slot, EnderChestSlot.byDefaultIndex(slot.defaultIndex()));
        }
    }

    private final Mirror<PlayerInventorySlot> reverse1st4Rows = new Mirror<PlayerInventorySlot>() {
        @Override
        public Integer getIndex(PlayerInventorySlot slot) {
            int defaultIndex = slot.defaultIndex();
            if (0 <= defaultIndex && defaultIndex < 9) {
                return defaultIndex + 27;
            } else if (9 <= defaultIndex && defaultIndex < 18) {
                return defaultIndex + 9;
            } else if (18 <= defaultIndex && defaultIndex < 27) {
                return defaultIndex - 9;
            } else if (27 <= defaultIndex && defaultIndex < 36) {
                return defaultIndex - 27;
            } else {
                return defaultIndex;
            }
        }

        @Override
        public PlayerInventorySlot getSlot(int index) { //index is the 'warped' index.
            if (0 <= index && index < 9) {
                return PlayerInventorySlot.byDefaultIndex(index + 27);
            } else if (9 <= index && index < 18) {
                return PlayerInventorySlot.byDefaultIndex(index + 9);
            } else if (18 <= index && index < 27) {
                return PlayerInventorySlot.byDefaultIndex(index - 9);
            } else if (27 <= index && index < 36) {
                return PlayerInventorySlot.byDefaultIndex(index - 27);
            } else if (36 <= index && index < 54) {
                return PlayerInventorySlot.byDefaultIndex(index);
            } else {
                return null;
            }
        }
    };

    @Test
    public void testReverse1st4RowsSound() {
        for (PlayerInventorySlot slot : PlayerInventorySlot.values()) {
            assertEquals(slot, reverse1st4Rows.getSlot(reverse1st4Rows.getIndex(slot)));
        }
    }

    @Test
    public void testReverse1st4RowsCorrectReversal() {
        final PlayerInventorySlot[] expected = new PlayerInventorySlot[] {
                PlayerInventorySlot.CONTAINER_27, PlayerInventorySlot.CONTAINER_28, PlayerInventorySlot.CONTAINER_29, PlayerInventorySlot.CONTAINER_30, PlayerInventorySlot.CONTAINER_31, PlayerInventorySlot.CONTAINER_32, PlayerInventorySlot.CONTAINER_33, PlayerInventorySlot.CONTAINER_34, PlayerInventorySlot.CONTAINER_35,
                PlayerInventorySlot.CONTAINER_18, PlayerInventorySlot.CONTAINER_19, PlayerInventorySlot.CONTAINER_20, PlayerInventorySlot.CONTAINER_21, PlayerInventorySlot.CONTAINER_22, PlayerInventorySlot.CONTAINER_23, PlayerInventorySlot.CONTAINER_24, PlayerInventorySlot.CONTAINER_25, PlayerInventorySlot.CONTAINER_26,
                PlayerInventorySlot.CONTAINER_09, PlayerInventorySlot.CONTAINER_10, PlayerInventorySlot.CONTAINER_11, PlayerInventorySlot.CONTAINER_12, PlayerInventorySlot.CONTAINER_13, PlayerInventorySlot.CONTAINER_14, PlayerInventorySlot.CONTAINER_15, PlayerInventorySlot.CONTAINER_16, PlayerInventorySlot.CONTAINER_17,
                PlayerInventorySlot.CONTAINER_00, PlayerInventorySlot.CONTAINER_01, PlayerInventorySlot.CONTAINER_02, PlayerInventorySlot.CONTAINER_03, PlayerInventorySlot.CONTAINER_04, PlayerInventorySlot.CONTAINER_05, PlayerInventorySlot.CONTAINER_06, PlayerInventorySlot.CONTAINER_07, PlayerInventorySlot.CONTAINER_08,
                PlayerInventorySlot.ARMOUR_BOOTS, PlayerInventorySlot.ARMOUR_LEGGINGS, PlayerInventorySlot.ARMOUR_CHESTPLATE, PlayerInventorySlot.ARMOUR_HELMET, PlayerInventorySlot.OFFHAND, PlayerInventorySlot.BODY, PlayerInventorySlot.SADDLE, PlayerInventorySlot.CURSOR, null,
                PlayerInventorySlot.PERSONAL_00, PlayerInventorySlot.PERSONAL_01, PlayerInventorySlot.PERSONAL_02, PlayerInventorySlot.PERSONAL_03, PlayerInventorySlot.PERSONAL_04, PlayerInventorySlot.PERSONAL_05, PlayerInventorySlot.PERSONAL_06, PlayerInventorySlot.PERSONAL_07, PlayerInventorySlot.PERSONAL_08
        };

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], reverse1st4Rows.getSlot(i));
        }
    }
}
