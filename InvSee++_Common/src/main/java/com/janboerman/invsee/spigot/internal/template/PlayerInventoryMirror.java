package com.janboerman.invsee.spigot.internal.template;

import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.utils.Compat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class PlayerInventoryMirror implements Mirror<PlayerInventorySlot> {

    public static final String DEFAULT_TEMPLATE =
            "i_00 i_01 i_02 i_03 i_04 i_05 i_06 i_07 i_08\n" +
            "i_09 i_10 i_11 i_12 i_13 i_14 i_15 i_16 i_17\n" +
            "i_18 i_19 i_20 i_21 i_22 i_23 i_24 i_25 i_26\n" +
            "i_27 i_28 i_29 i_30 i_31 i_32 i_33 i_34 i_35\n" +
            "a_b  a_l  a_c  a_h  oh   b    s    c    _   \n" +
            "p_00 p_01 p_02 p_03 p_04 p_05 p_06 p_07 p_08";

    public static PlayerInventoryMirror DEFAULT = new PlayerInventoryMirror(DEFAULT_TEMPLATE);

    private PlayerInventorySlot[] slots;
    private Map<PlayerInventorySlot, Integer> indices;

    /** @deprecated Use {@link #ofTemplate(String)} instead. */
    @Deprecated
    public PlayerInventoryMirror(String template) {
        slots = Compat.lines(template)
                .flatMap(line -> IntStream.range(0, 9).mapToObj(i -> convert(line.substring(i*5, i*5 + 4))))
                .toArray(PlayerInventorySlot[]::new);
        indices = new HashMap<>();
        for (int i = 0; i < slots.length; i++) {
            PlayerInventorySlot slot = slots[i];
            if (slot != null) indices.put(slots[i], i);
        }
    }

    public static PlayerInventoryMirror ofTemplate(String template) {
        if (template == null) throw new IllegalArgumentException("template cannot be null");
        if (template == DEFAULT_TEMPLATE) return DEFAULT;
        return new PlayerInventoryMirror(template);
    }

    private static PlayerInventorySlot convert(String symbol) {
        switch (symbol) {
            case "a_b ": return PlayerInventorySlot.ARMOUR_BOOTS;
            case "a_l ": return PlayerInventorySlot.ARMOUR_LEGGINGS;
            case "a_c ": return PlayerInventorySlot.ARMOUR_CHESTPLATE;
            case "a_h ": return PlayerInventorySlot.ARMOUR_HELMET;

            case "oh  ": return PlayerInventorySlot.OFFHAND;
            case "b   ": return PlayerInventorySlot.BODY;
            case "s   ": return PlayerInventorySlot.SADDLE;

            case "c   ": return PlayerInventorySlot.CURSOR;

            default:
                final String prefix = symbol.substring(0, 2);
                if ("i_".equals(prefix)) {
                    return PlayerInventorySlot.valueOf("CONTAINER_" + symbol.substring(2, 4));
                } else if ("p_".equals(prefix)) {
                    return PlayerInventorySlot.valueOf("PERSONAL_" + symbol.substring(2, 4));
                } else {
                    return null;
                }
        }
    }

    @Override
    public Integer getIndex(PlayerInventorySlot slot) {
        if (slot == null) return null;
        return indices.get(slot);
    }

    @Override
    public PlayerInventorySlot getSlot(int index) {
        return slots[index];
    }

    //for testing purposes
    public PlayerInventorySlot[] getSlots() {
        return Arrays.copyOf(slots, slots.length);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ", "PlayerInventoryTemplate(", ")");
        for (PlayerInventorySlot slot : slots) sj.add(slot.toString());
        return sj.toString();
    }


    // Mirror -> template

    public static String toTemplate(Mirror<PlayerInventorySlot> mirror) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 54; i++) {
            PlayerInventorySlot slot = mirror.getSlot(i);
            if (slot != null) {
                switch (slot) {
                    case ARMOUR_BOOTS: stringBuilder.append("a_b "); break;
                    case ARMOUR_LEGGINGS: stringBuilder.append("a_l "); break;
                    case ARMOUR_CHESTPLATE: stringBuilder.append("a_c "); break;
                    case ARMOUR_HELMET: stringBuilder.append("a_h "); break;

                    case OFFHAND: stringBuilder.append("oh  "); break;
                    case BODY: stringBuilder.append("b   "); break;
                    case SADDLE: stringBuilder.append("s   "); break;

                    case CURSOR: stringBuilder.append("c   "); break;

                    default:
                        String slotName = slot.name();
                        if (slot.isContainer()) {
                            stringBuilder.append("i_" + slotName.substring(CONTAINER_LENGTH, slotName.length()));
                        } else if (slot.isPersonal()) {
                            stringBuilder.append("p_" + slotName.substring(PERSONAL_LENGTH, slotName.length()));
                        } else {
                            stringBuilder.append("_   ");
                        }
                }
            } else {
                stringBuilder.append("_   ");
            }

            if (i % 9 == 0) {
                if (i != 0) stringBuilder.append(System.lineSeparator());
            } else {
                stringBuilder.append(' ');
            }
        }

        return stringBuilder.toString();
    }

    private static final int CONTAINER_LENGTH = "CONTAINER_".length();
    private static final int PERSONAL_LENGTH = "PERSONAL_".length();

}
