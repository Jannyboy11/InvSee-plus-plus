package com.janboerman.invsee.utils;

import java.util.List;

public class ListHelper {

    private ListHelper() {
    }

    //TODO according to IntelliJ, this is no longer called. why do we have this?
    @Deprecated
    public static <T> List<T> padTo(int size, T with, List<T> list) {
        final int listSize = list.size();
        assert listSize <= size : "Illegal argument: list is larger than size!";

        if (listSize >= size)
            return list;

        int gap = size - list.size();
        ConstantList<T> padding = new ConstantList<>(gap, with);
        return new ConcatList<>(list, padding);
    }

}
