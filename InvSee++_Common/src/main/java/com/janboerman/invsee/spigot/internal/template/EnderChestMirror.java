package com.janboerman.invsee.spigot.internal.template;

import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class EnderChestMirror implements Mirror<EnderChestSlot> {

    public static final String DEFAULT_TEMPLATE =
            "e_00 e_01 e_02 e_03 e_04 e_05 e_06 e_07 e_08\n" +
            "e_09 e_10 e_11 e_12 e_13 e_14 e_15 e_16 e_17\n" +
            "e_18 e_19 e_20 e_21 e_22 e_23 e_24 e_25 e_26\n" +
            "e_27 e_28 e_29 e_30 e_31 e_32 e_33 e_34 e_35\n" +
            "e_36 e_37 e_38 e_39 e_40 e_41 e_42 e_43 e_44\n" +
            "e_45 e_46 e_47 e_48 e_49 e_50 e_51 e_52 e_53";

    public static final EnderChestMirror DEFAULT = new EnderChestMirror(DEFAULT_TEMPLATE);

    private EnderChestSlot[] slots;
    private Map<EnderChestSlot, Integer> indices;

    /** @deprecated Use {@link #ofTemplate(String)} instead. */
    @Deprecated
    public EnderChestMirror(String template) {

        slots = template.lines()
                .flatMap(line -> IntStream.range(0, 9).mapToObj(i -> convert(line.substring(i*5, i*5 + 4))))
                .toArray(EnderChestSlot[]::new);
        indices = new HashMap<>();
        for (int i = 0; i < slots.length; i++) {
            var slot = slots[i];
            if (slot != null) indices.put(slots[i], i);
        }
    }

    public static EnderChestMirror ofTemplate(String template) {
        if (template == null) throw new IllegalArgumentException("template cannot be null");
        if (template == DEFAULT_TEMPLATE) return DEFAULT;
        return new EnderChestMirror(template);
    }

    private static EnderChestSlot convert(String symbol) {
        if ("e_".equals(symbol.substring(0, 2))) {
            return EnderChestSlot.valueOf("CONTAINER_" + symbol.substring(2, 4));
        } else {
            return null;
        }
    }

    @Override
    public Integer getIndex(EnderChestSlot slot) {
        if (slot == null) return null;
        return indices.get(slot);
    }

    @Override
    public EnderChestSlot getSlot(int index) {
        return slots[index];
    }

    //for testing purposes
    public EnderChestSlot[] getSlots() {
        return Arrays.copyOf(slots, slots.length);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ", "EnderChestTemplate(", ")");
        for (var slot : slots) sj.add(slot.toString());
        return sj.toString();
    }


    // Mirror -> template

    public static String toTemplate(Mirror<EnderChestSlot> mirror) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 54; i++) {
            EnderChestSlot slot = mirror.getSlot(i);
            if (slot != null) {
                String slotName = slot.name();
                stringBuilder.append("e_" + slotName.substring(CONTAINER_LENGTH, slotName.length()));
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

}
