package com.janboerman.invsee.utils;

public final class ArrayHelper {

    private ArrayHelper() {
    }

    public static char[] concat(char[] one, char[] two) {
        char[] result = new char[one.length + two.length];
        System.arraycopy(one, 0, result, 0, one.length);
        System.arraycopy(two, 0, result, one.length, two.length);
        return result;
    }

}
