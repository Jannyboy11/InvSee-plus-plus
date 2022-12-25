package com.janboerman.invsee.spigot.internal.template;

import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import com.janboerman.invsee.spigot.api.template.Mirror;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class PlayerInventoryMirror implements Mirror<PlayerInventorySlot> {

    public static PlayerInventoryMirror DEFAULT = new PlayerInventoryMirror(
            "i_00 i_01 i_02 i_03 i_04 i_05 i_06 i_07 i_08\n" +
            "i_09 i_10 i_11 i_12 i_13 i_14 i_15 i_16 i_17\n" +
            "i_18 i_19 i_20 i_21 i_22 i_23 i_24 i_25 i_26\n" +
            "i_27 i_28 i_29 i_30 i_31 i_32 i_33 i_34 i_35\n" +
            "a_b  a_l  a_c  a_h  oh   c    _    _    _   \n" +
            "p_00 p_01 p_02 p_03 p_04 p_05 p_06 p_07 p_08");

    private PlayerInventorySlot[] slots;
    private Map<PlayerInventorySlot, Integer> indices;

    public PlayerInventoryMirror(String template) {
        if (template == null) throw new IllegalArgumentException("template cannot be null");

        slots = template.lines()
                .flatMap(line -> IntStream.range(0, 9).mapToObj(i -> convert(line.substring(i*5, i*5 + 4))))
                .toArray(PlayerInventorySlot[]::new);
        indices = new HashMap<>();
        for (int i = 0; i < slots.length; i++) {
            var slot = slots[i];
            if (slot != null) indices.put(slots[i], i);
        }
    }

    private static PlayerInventorySlot convert(String symbol) {
        switch (symbol) {
            case "a_b ": return PlayerInventorySlot.ARMOUR_BOOTS;
            case "a_l ": return PlayerInventorySlot.ARMOUR_LEGGINGS;
            case "a_c ": return PlayerInventorySlot.ARMOUR_CHESTPLATE;
            case "a_h ": return PlayerInventorySlot.ARMOUR_HELMET;

            case "oh  ": return PlayerInventorySlot.OFFHAND;
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
        for (var slot : slots) sj.add(slot.toString());
        return sj.toString();
    }

}
