package com.janboerman.invsee.utils;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    public R apply(T one, U two, V three);

}
