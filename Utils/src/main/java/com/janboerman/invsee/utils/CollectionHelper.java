package com.janboerman.invsee.utils;

import java.util.Collection;
import java.util.function.Predicate;

public class CollectionHelper {

    private CollectionHelper() {}

    public static <T> T firstOrNull(Collection<? extends T> collection, Predicate<T> requirement) {
        for (T item : collection)
            if (requirement.test(item))
                return item;

        return null;
    }

}
