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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Compat {

    private Compat() {}

    static void checkFromToIndex(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex must be 0 or greater.");
        if (toIndex > size) throw new IndexOutOfBoundsException("toIndex must not be greater than size.");
        if (fromIndex > toIndex) throw new IndexOutOfBoundsException("fromIndex must be smaller than toIndex.");
    }

    static void checkIndex(int index, int size) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index out of range: " + index + ", range: [0-" + size + ")");
    }

    public static <K, V> Entry<K, V> mapEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public static <K, V> Map<K, V> mapOfEntries(Entry<? extends K, ? extends V>... entries) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        for (Entry<? extends K, ? extends V> entry : entries) map.put(entry.getKey(), entry.getValue());
        return Collections.unmodifiableMap(map);
    }

    public static Stream<String> lines(String string) {
        return new BufferedReader(new StringReader(string)).lines();
    }

    public static <T> List<T> listCopy(Collection<T> coll) {
        return new ArrayList<>(coll);
    }

    public static <T> List<T> listOf(T... items) {
        return Collections.unmodifiableList(Arrays.asList(items));
    }

    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    public static <T> List<T> singletonList(T item) {
        return Collections.singletonList(item);
    }

    public static <T> void ifPresentOrElse(Optional<T> optional, Consumer<? super T> action, Runnable emptyAction) {
        if (optional.isPresent()) {
            action.accept(optional.get());
        } else {
            emptyAction.run();
        }
    }
}
