package com.janboerman.invsee.spigot.addon.give.cmd;

public enum ArgType {

    TARGET("target player"),
    ITEM_TYPE("item type"),
    AMOUNT("amount"),
    NBT_TAG("nbt tag");

    private final String formatted;

    private ArgType(String formatString) {
        this.formatted = formatString;
    }

    @Override
    public String toString() {
        return "<" + formatted + ">";
    }

}
