package com.janboerman.invsee.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, bytesRead);
            }
            outputStream.flush();
            return outputStream.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> optionalOr(Optional<T> optional, Supplier<? extends Optional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (optional.isPresent()) {
            return optional;
        } else {
            return Objects.requireNonNull((Optional<T>) supplier.get());
        }
    }

    public static <T> Set<T> setCopy(Collection<? extends T> coll) {
        return new HashSet<>(coll);
    }

    public static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    public static String stringRepeat(String base, int repeat) {
        if (base == null) return null;
        StringJoiner stringJoiner = new StringJoiner("");
        for (int i = 0; i < repeat; ++i) {
            stringJoiner.add(base);
        }
        return stringJoiner.toString();
    }

    public static int majorJavaVersion() {
        String version = System.getProperty("java.version");

        if (version.startsWith("1.")) {
            // Java 8 or lower
            return Integer.parseInt(version.substring(2, 3));
        } else {
            // Java 9 or higher
            int dot = version.indexOf(".");
            return dot != -1 ? Integer.parseInt(version.substring(0, dot)) : Integer.parseInt(version);
        }
    }
}
