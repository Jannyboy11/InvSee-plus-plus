package com.janboerman.invsee.utils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Compat {

    private Compat() {}

    static void checkFromToIndex(int fromIndex, int toIndex, int size) {
        Objects.checkFromToIndex(fromIndex, toIndex, size);
    }

    static void checkIndex(int index, int size) {
        Objects.checkIndex(index, size);
    }

    public static <K, V> Entry<K, V> mapEntry(K key, V value) {
        return Map.entry(key, value);
    }

    public static <K, V> Map<K, V> mapOfEntries(Entry<? extends K, ? extends V>... entries) {
        return Map.ofEntries(entries);
    }

    public static Stream<String> lines(String string) {
        return string.lines();
    }

    public static <T> List<T> listCopy(Collection<T> coll) {
        return List.copyOf(coll);
    }

    public static <T> List<T> listOf(T... items) {
        return List.of(items);
    }

    public static <T> List<T> emptyList() {
        return List.of();
    }

    public static <T> List<T> singletonList(T item) {
        return List.of(item);
    }

    public static <T> void ifPresentOrElse(Optional<T> optional, Consumer<? super T> action, Runnable emptyAction) {
        optional.ifPresentOrElse(action, emptyAction);
    }
}
