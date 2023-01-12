package com.janboerman.invsee.utils;

@Deprecated(forRemoval = true)
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    public R apply(T one, U two, V three);

}
