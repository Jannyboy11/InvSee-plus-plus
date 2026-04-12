package com.janboerman.invsee.utils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.io.InputStream;
import java.io.IOException;

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

    public static <T> List<T> listCopy(Collection<? extends T> coll) {
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

    public static <K, V> Map<K, V> emptyMap() {
        return Map.of();
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;
        return inputStream.readAllBytes();
    }

    public static <T> Optional<T> optionalOr(Optional<T> optional, Supplier<? extends Optional<? extends T>> supplier) {
        return optional.or(supplier);
    }

    public static <T> Set<T> setCopy(Collection<? extends T> coll) {
        return Set.copyOf(coll);
    }

    public static <T> Set<T> emptySet() {
        return Set.of();
    }

    public static String stringRepeat(String toBeRepeated, int count) {
        return toBeRepeated.repeat(count);
    }

    public static int majorJavaVersion() {
        return Runtime.version().feature();
    }
}
