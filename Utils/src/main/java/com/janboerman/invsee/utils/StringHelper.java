package com.janboerman.invsee.utils;

public class StringHelper {

    private StringHelper() {
    }

    public static boolean startsWithIgnoreCase(String string, String prefix) {
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

}
