package com.janboerman.invsee.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO optimise this using multi-release jar file
public final class ListHelper {

    private ListHelper() {
    }

    public static <T> List<T> copy(List<T> list) {
        // TODO Java 11 and later: List.copyOf(list)
        return new ArrayList<>(list);
    }

    public static <T> List<T> of(T... items) {
        // TODO Java 11 and later: List.of(items)
        return Arrays.asList(items);
    }
}
