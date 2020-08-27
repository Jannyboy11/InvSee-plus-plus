package com.janboerman.invsee.utils;

import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class Either<L, R> {

    private Either() {}

    public static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    public abstract boolean isLeft();
    public abstract boolean isRight();
    public abstract L getLeft();
    public abstract R getRight();

    private static final class Left<L, R> extends Either<L, R> {
        private final L value;
        private Left(L value) {
            this.value = value;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public L getLeft() {
            return value;
        }

        @Override
        public R getRight() {
            throw new NoSuchElementException("left");
        }

        @Override
        public String toString() {
            return "Left[" + value + ']';
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Left)) return false;

            Left<?, ?> that = (Left<?, ?>) o;
            return Objects.equals(value, that.value);
        }
    }

    private static final class Right<L, R> extends Either<L, R> {
        private final R value;
        private Right(R value) {
            this.value = value;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public L getLeft() {
            throw new NoSuchElementException("right");
        }

        @Override
        public R getRight() {
            return value;
        }

        @Override
        public String toString() {
            return "Right[" + value + ']';
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value) + 1;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Right)) return false;

            Right<?, ?> that = (Right<?, ?>) o;
            return Objects.equals(value, that.value);
        }
    }
}
