package com.janboerman.invsee.utils;

import java.util.Iterator;
import java.util.Objects;

public class SynchronizedIterator<T> {

    private final Iterator<T> iterator;

    public SynchronizedIterator(Iterator<T> iterator) {
        this.iterator = Objects.requireNonNull(iterator);
    }

    public Maybe<T> moveNext() {
        synchronized (iterator) {
            if (iterator.hasNext()) {
                return Maybe.just(iterator.next());
            } else {
                return Maybe.nothing();
            }
        }
    }

}
