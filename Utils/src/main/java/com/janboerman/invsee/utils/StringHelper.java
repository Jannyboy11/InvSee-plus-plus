package com.janboerman.invsee.utils;

import java.util.StringJoiner;

public class StringHelper {

    private StringHelper() {
    }

    public static boolean startsWithIgnoreCase(String string, String prefix) {
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static String joinArray(String delimiter, int fromIndex, String[] source) {
        assert fromIndex >= 0 : "fromIndex must be non-negative, got: " + fromIndex;

        StringJoiner stringJoiner = new StringJoiner(delimiter);
        for (int i = fromIndex; i < source.length; i++) {
            stringJoiner.add(source[i]);
        }
        return stringJoiner.toString();
    }

}
