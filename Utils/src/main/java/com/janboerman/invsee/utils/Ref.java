package com.janboerman.invsee.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

// For you FP nerds out there: this is much like a 'Lens'.
public interface Ref<T> {

    public void set(T item);

    public T get();

    public static <T> Ref<T> of(Supplier<? extends T> get, Consumer<? super T> set) {
        return new Ref<T>() {
            @Override public T get() { return get.get(); }
            @Override public void set(T item) { set.accept(item); }
        };
    }

    public static <T> Ref<T> ofArray(int index, T[] array) {
        return new Ref<T>() {
            @Override public T get() { return array[index]; }
            @Override public void set(T item) { array[index] = item; }
        };
    }

    public static <T> Ref<T> ofList(int index, List<T> list) {
        return new Ref<T>() {
            @Override public T get() { return list.get(index); }
            @Override public void set(T item) { list.set(index, item); }
        };
    }

    public static <K, V> Ref<V> ofMap(K key, Map<K, V> map) {
        return new Ref<V>() {
            @Override public V get() { return map.get(key); }
            @Override public void set(V value) { map.put(key, value); }
        };
    }

}
