package com.janboerman.invsee.utils;

public abstract class Either<L, R> {

    public static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    public static class Left<L, R> extends Either<L, R> {
        private final L value;
        private Left(L value) {
            this.value = value;
        }
    }

    public static class Right<L, R> extends Either<L, R> {
        private final R value;
        private Right(R value) {
            this.value = value;
        }
    }
}
