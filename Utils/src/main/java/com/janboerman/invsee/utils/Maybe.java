package com.janboerman.invsee.utils;

import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class Maybe<T> {

    private Maybe() {}

    public static <T> Maybe<T> just(T value) {
        return new Just<>(value);
    }

    public static <T> Maybe<T> nothing() {
        return Nothing.INSTANCE;
    }

    public abstract boolean isPresent();
    public abstract T get() throws NoSuchElementException;

    private static class Just<T> extends Maybe<T> {
        private final T value;
        Just(T value) {
            this.value = value; //null is allowed!
        }

        public T get() {
            return value;
        }

        public boolean isPresent() {
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Just)) return false;

            Just that = (Just) obj;
            return Objects.equals(this.get(), that.get());
        }

        @Override
        public String toString() {
            return "Just(" + value + ")";
        }
    }

    private static class Nothing extends Maybe {
        static Nothing INSTANCE = new Nothing();

        private Nothing() {}

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public Object get() throws NoSuchElementException {
            throw new NoSuchElementException("Nothing");
        }

        @Override
        public String toString() {
            return "Nothing";
        }
    }
}
