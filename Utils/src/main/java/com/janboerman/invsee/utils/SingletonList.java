package com.janboerman.invsee.utils;

import java.util.*;

public class SingletonList<T> implements List<T>, RandomAccess {

    private final Ref<T> ref;

    public SingletonList(Ref<T> ref) {
        this.ref = Objects.requireNonNull(ref);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return Objects.equals(ref.get(), o);
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[] { ref.get() };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R[] toArray(R[] a) {
        Objects.requireNonNull(a);
        if (a.length == 0) {
            a = (R[]) new Object[1];
        }
        a[0] = (R) ref.get();
        return a;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("cannot add items to a SingletonList");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("cannot remove items from a SingletonList");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(item -> Objects.equals(ref.get(), item));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("cannot add items to a SingletonList");
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("cannot add items to a SingletonList");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot remove items from a SingletonList");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot remove items from a SingletonList");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("cannot clear singleton list");
    }

    @Override
    public T get(int index) {
        if (index != 0) throw new IllegalArgumentException(new IndexOutOfBoundsException(index));
        return ref.get();
    }

    @Override
    public T set(int index, T element) {
        if (index != 0) throw new IllegalArgumentException(new IndexOutOfBoundsException(index));
        T old = ref.get();
        ref.set(element);
        return old;
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException("cannot add items to a SingletonList");
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("cannot remove items from a SingletonList");
    }

    @Override
    public int indexOf(Object o) {
        if (contains(o)) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ListIterator<T>() {
            int cursor = index;

            @Override
            public boolean hasNext() {
                return cursor == 0;
            }

            @Override
            public T next() {
                if (cursor == 0) {
                    cursor = 1;
                    return ref.get();
                } else {
                    throw new NoSuchElementException("SingletonList iterator is already done");
                }
            }

            @Override
            public boolean hasPrevious() {
                return cursor == 1;
            }

            @Override
            public T previous() {
                if (cursor == 1) {
                    cursor = 0;
                    return ref.get();
                } else {
                    throw new NoSuchElementException("SingletonList iterator is already done");
                }
            }

            @Override
            public int nextIndex() {
                return 0;
            }

            @Override
            public int previousIndex() {
                return 0;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("cannot remove items from a SingletonList");
            }

            @Override
            public void set(T t) {
                if (cursor == 0) {
                    ref.set(t);
                }
            }

            @Override
            public void add(T t) {
                throw new UnsupportedOperationException("cannot add items to a SingletonList");
            }
        };
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex != 0)
            throw new IllegalArgumentException("fromIndex must be 0", new IndexOutOfBoundsException(fromIndex));
        if (!(toIndex == 0 || toIndex == 1))
            throw new IllegalArgumentException("toIndex must be 0 or 1", new IndexOutOfBoundsException(toIndex));

        if (toIndex == 0)
            return List.of();
        else
            return this;
    }
}
