package com.janboerman.invsee.utils;

public final class Rethrow {

    private Rethrow() {}

    public static <T extends Throwable, R> R unchecked(Throwable t) throws T {
        throw (T) t;
    }
}
