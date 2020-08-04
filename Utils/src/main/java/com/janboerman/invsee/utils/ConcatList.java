package com.janboerman.invsee.utils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ConcatList<T> extends AbstractList<T> {

    private final List<T> first, second;

    public ConcatList(List<T> first, List<T> second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public T get(int index) {
        if (index < first.size()) {
            return first.get(index);
        } else {
            return second.get(index);
        }
    }

    @Override
    public int size() {
        return first.size() + second.size();
    }

    @Override
    public T set(int index, T element) {
        if (index < first.size()) {
            return first.set(index, element);
        } else {
            return second.set(index, element);
        }
    }

    @Override
    public int indexOf(Object o) {
        int index = first.indexOf(o);
        if (index != -1) {
            return index;
        } else {
            index = second.indexOf(o);
            if (index != -1) {
                index += first.size();
            }
            return index;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = second.lastIndexOf(o);
        if (index != -1) {
            return index + first.size();
        } else {
            return first.lastIndexOf(o);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ListIterator<T>() {
            boolean isFirst = index < first.size();
            ListIterator<T> running = isFirst ? first.listIterator(index) : second.listIterator(index - first.size());

            @Override
            public boolean hasNext() {
                if (isFirst) {
                    return running.hasNext() || !second.isEmpty();
                } else {
                    return running.hasNext();
                }
            }

            @Override
            public T next() {
                T next;
                if (isFirst && running.hasNext()) {
                    next = running.next();
                } else {
                    isFirst = false;
                    running = second.listIterator(0);
                    next = running.next();
                }
                return next;
            }

            @Override
            public boolean hasPrevious() {
                if (isFirst) {
                    return running.hasPrevious();
                } else {
                    return running.hasPrevious() || !first.isEmpty();
                }
            }

            @Override
            public T previous() {
                T previous;
                if (!isFirst && running.hasPrevious()) {
                    previous = running.previous();
                } else {
                    isFirst = true;
                    running = first.listIterator(first.size());
                    previous = running.previous();
                }
                return previous;
            }

            @Override
            public int nextIndex() {
                if (isFirst) {
                    return running.nextIndex();
                } else {
                    return running.nextIndex() + first.size();
                }
            }

            @Override
            public int previousIndex() {
                if (isFirst) {
                    return running.previousIndex();
                } else {
                    return running.previousIndex() + first.size();
                }
            }

            @Override
            public void remove() {
                running.remove();
            }

            @Override
            public void set(T t) {
                running.set(t);
            }

            @Override
            public void add(T t) {
                running.add(t);
            }
        };
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex >= toIndex)
            throw new IllegalArgumentException("fromIndex (" + fromIndex + ") must be strictly smaller than toIndex (" + toIndex + ")");

        if (fromIndex == 0 && toIndex == first.size()) {
            return first;
        } else if (fromIndex == first.size() && toIndex == size()) {
            return second;
        } else if (toIndex < first.size()) {
            return first.subList(fromIndex, toIndex);
        } else if (fromIndex >= first.size()) {
            return second.subList(fromIndex - first.size(), toIndex - first.size());
        } else {
            return super.subList(fromIndex, toIndex);
        }
    }

    @Override
    public boolean isEmpty() {
        return first.isEmpty() && second.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return first.contains(o) || second.contains(o);
    }

    @Override
    public Object[] toArray() {

        Object[] firstArray = first.toArray();
        Object[] secondArray = second.toArray();
        Object[] dest = new Object[firstArray.length + secondArray.length];

        System.arraycopy(firstArray, 0, dest, 0, firstArray.length);
        System.arraycopy(secondArray, 0, dest, firstArray.length, secondArray.length);

        return dest;
    }

    @Override
    public <E> E[] toArray(E[] array) {
        int size = size();
        Class<?> componentType = array.getClass().getComponentType();
        E[] r = array.length >= size ? array : (E[]) Array.newInstance(componentType, size);

        E[] resultArray = first.toArray(r);

        E[] secondPart = second.toArray((E[]) Array.newInstance(componentType, 0));
        System.arraycopy(secondPart, 0, resultArray, first.size(), second.size());

        return resultArray;
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        first.replaceAll(operator);
        second.replaceAll(operator);
    }

    @Override
    public Spliterator<T> spliterator() {
        return new Spliterator<T>() {
            private boolean isFirst = true;
            private Spliterator<T> running = first.spliterator();

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                boolean advanced = running.tryAdvance(consumer);
                if (!advanced && isFirst) {
                    isFirst = false;
                    running = second.spliterator();
                    advanced = running.tryAdvance(consumer);
                }
                return advanced;
            }

            @Override
            public Spliterator<T> trySplit() {
                return running.trySplit();
            }

            @Override
            public long estimateSize() {
                return (long) first.size() + (long) second.size();
            }

            @Override
            public int characteristics() {
                return SIZED | SUBSIZED | ORDERED;
            }
        };
    }

    @Override
    public Stream<T> stream() {
        return Stream.concat(first.stream(), second.stream());
    }

    @Override
    public Stream<T> parallelStream() {
        return Stream.concat(first.parallelStream(), second.parallelStream());
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        first.forEach(action);
        second.forEach(action);
    }

    @Override
    public boolean add(T t) {
        return second.add(t);
    }

    @Override
    public void add(int index, T element) {
        if (index <= first.size()) {
            first.add(index, element);
        } else {
            second.add(index - first.size(), element);
        }
    }

    @Override
    public T remove(int index) {
        if (index <= first.size()) {
            return first.remove(index);
        } else {
            return second.remove(index - first.size());
        }
    }

    @Override
    public void clear() {
        first.clear();
        second.clear();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (index <= first.size()) {
            return first.addAll(index, c);
        } else {
            return second.addAll(index - first.size(),c);
        }
    }

    @Override
    public boolean remove(Object o) {
        return first.remove(o) || second.remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return second.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return first.removeAll(c) & second.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return first.retainAll(c) & second.retainAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return first.removeIf(filter) & second.removeIf(filter);
    }


}
