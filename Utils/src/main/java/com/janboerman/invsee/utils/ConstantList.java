package com.janboerman.invsee.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** @deprecated Use {@link java.util.Collections#nCopies(int, Object)} instead. */
@Deprecated //forRemoval = true
public final class ConstantList<T> implements java.util.List<T> {

    private final int size;
    private final T constant;

    public ConstantList(int size, T constant) {
        this.size = size;
        this.constant = constant;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return Objects.equals(o, constant);
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < size; i++)
            action.accept(constant);
    }

    @Override
    public Object[] toArray() {
        Object[] res = new Object[size];
        Arrays.fill(res, constant);
        return res;
    }

    @Override
    public <U> U[] toArray(U[] array) {
        if (array.length >= size) {
            Arrays.fill(array, 0, size, constant);
            //weird specification from the java.util.List javadoc!
            if (array.length > size) array[size] = null;
        } else {
            array = (U[]) Array.newInstance(array.getClass().getComponentType(), size);
            Arrays.fill(array, constant);
        }
        return array;
    }

    //@Override
    public <U> U[] toArray(IntFunction<U[]> generator) {
        U[] arr = generator.apply(size);
        Arrays.fill(arr, constant);
        return arr;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(o -> Objects.equals(o, constant));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public T get(int index) {
        Compat.checkIndex(index, size);
        return constant;
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("Can't modify ConstantList");
    }

    @Override
    public int indexOf(Object o) {
        if (Objects.equals(o, constant)) return 0;
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (Objects.equals(o, constant)) return size - 1;
        return -1;
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
                return cursor < size;
            }

            @Override
            public T next() {
                cursor += 1;
                return ConstantList.this.constant;
                //normally we would move the cursor *after* the next call, but since our element is constant, we can just do it *before*! :)
            }

            @Override
            public boolean hasPrevious() {
                return cursor > 0 && !ConstantList.this.isEmpty();
            }

            @Override
            public T previous() {
                cursor -= 1;
                return ConstantList.this.constant;
            }

            @Override
            public int nextIndex() {
                return cursor;
            }

            @Override
            public int previousIndex() {
                return cursor - 1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Can't modify ConstantList");
            }

            @Override
            public void set(T t) {
                throw new UnsupportedOperationException("Can't modify ConstantList");
            }

            @Override
            public void add(T t) {
                throw new UnsupportedOperationException("Can't modify ConstantList");
            }
        };
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        Compat.checkFromToIndex(fromIndex, toIndex, size);

        if (fromIndex == 0 && toIndex == size)
            return this;

        int newSize = toIndex - fromIndex;
        return new ConstantList<T>(newSize, constant);
    }

    @Override
    public Spliterator<T> spliterator() {
        return new ConstantSpliterator(size);
    }

    @Override
    public Stream<T> stream() {
        return Stream.generate(() -> constant).limit(size);
    }

    @Override
    public Stream<T> parallelStream() {
        return StreamSupport.stream(this::spliterator, Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED | Spliterator.SORTED, true);
    }

    private class ConstantSpliterator implements Spliterator<T> {
        private int remaining;

        private ConstantSpliterator(int remaining) {
            this.remaining = remaining;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (remaining > 0) {
                action.accept(constant);
                remaining -= 1;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            int half = remaining / 2;
            this.remaining -= half;
            return new ConstantSpliterator(half);
        }

        @Override
        public long estimateSize() {
            return remaining;
        }

        @Override
        public int characteristics() {
            return Spliterator.IMMUTABLE
                    | Spliterator.SORTED
                    | Spliterator.ORDERED
                    | Spliterator.SIZED
                    | Spliterator.SUBSIZED;
        }

        @Override
        public Comparator<? super T> getComparator() {
            //sorted by natural order
            return null;
        }
    }
}
