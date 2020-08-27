package com.janboerman.invsee.utils;

import java.lang.ref.WeakReference;
import java.util.*;

public class WeakBiMap<K, V> extends AbstractMap<K, V> {

    private final WeakHashMap<K, WeakReference<V>> normal = new WeakHashMap<>();
    private final WeakHashMap<V, WeakReference<K>> inverse = new WeakHashMap<>();

    public WeakBiMap() {}

    public WeakBiMap(Map<? extends K, ? extends V> provider) {
        this();
        putAll(provider);
    }

    public Reversed swap() {
        return new Reversed();
    }

    @Override
    public int size() {
        return normal.size();
    }

    @Override
    public boolean isEmpty() {
        return normal.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return normal.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inverse.containsKey(value);
    }

    @Override
    public V get(Object key) {
        WeakReference<V> ref = normal.get(key);
        return ref == null ? null : ref.get();
    }

    @Override
    public V put(K key, V value) {
        WeakReference<V> oldRef = normal.put(key, new WeakReference<>(value));
        V oldValue = oldRef == null ? null : oldRef.get();
        if (oldValue != null) inverse.remove(oldValue);         //return value is unused but should equal new WeakReference<>(key)
        inverse.put(value, new WeakReference<>(key));
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        WeakReference<V> oldRef = normal.remove(key);
        if (oldRef != null) {
            V oldValue = oldRef.get();
            if (oldValue != null) {
                inverse.remove(oldValue);                       //return value is unused but should equal new WeakReference<>(key)
            }
            return oldValue;
        } else {
            return null;
        }
    }

    @Override
    public void clear() {
        normal.clear();
        inverse.clear();
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = normal.keySet();
        return new AbstractSet<K>() {
            @Override
            public int size() {
                return WeakBiMap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return WeakBiMap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return WeakBiMap.this.containsKey(o);
            }

            @Override
            public Iterator<K> iterator() {
                Iterator<K> normalIterator = keySet.iterator();
                return new Iterator<K>() {
                    K lastKey = null;
                    @Override
                    public boolean hasNext() {
                        return normalIterator.hasNext();
                    }

                    @Override
                    public K next() {
                        return lastKey = normalIterator.next();
                    }

                    @Override
                    public void remove() {
                        normalIterator.remove();
                        inverse.values().remove(new WeakReference<>(lastKey)); //definitely do not like this but oh well.
                    }
                };
            }

            @Override
            public Object[] toArray() {
                return keySet.toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return keySet.toArray(a);
            }

            @Override
            public boolean add(K k) {
                throw new UnsupportedOperationException("Can't add a value to a key set view");
            }

            @Override
            public boolean remove(Object o) {
                return WeakBiMap.this.remove(o) != null;
            }

            @Override
            public boolean addAll(Collection<? extends K> c) {
                throw new UnsupportedOperationException("Cant add values to a key set view");
            }

            @Override
            public void clear() {
                WeakBiMap.this.clear();
            }
        };
    }

    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public int size() {
                return WeakBiMap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return WeakBiMap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return inverse.containsKey(o);
            }

            @Override
            public Iterator<V> iterator() {
                Iterator<WeakReference<V>> normalIt = normal.values().iterator();
                return new Iterator<V>() {
                    V lastValue = null;
                    @Override
                    public boolean hasNext() {
                        return normalIt.hasNext();
                    }

                    @Override
                    public V next() {
                        WeakReference<V> nextRef = normalIt.next();
                        lastValue = nextRef == null ? null : nextRef.get();
                        return lastValue;
                    }

                    @Override
                    public void remove() {
                        if (lastValue != null) {
                            inverse.remove(lastValue);
                        }
                        normalIt.remove();
                    }
                };
            }

            @Override
            public boolean add(V v) {
                throw new UnsupportedOperationException("Can't add a value to a value collection view");
            }

            @Override
            public boolean remove(Object o) {
                WeakReference<K> oldKeyRef = inverse.remove(o);
                K oldKey = oldKeyRef == null ? null : oldKeyRef.get();
                if (oldKey != null) {
                    return normal.remove(oldKey) != null;
                } else {
                    return false;
                }
            }

            @Override
            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException("Can't add values to a value collection view");
            }

            @Override
            public void clear() {
                WeakBiMap.this.clear();
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public int size() {
                return WeakBiMap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return WeakBiMap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                if (o instanceof Map.Entry) {
                    Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
                    return WeakBiMap.this.containsKey(entry.getKey())
                            && WeakBiMap.this.containsValue(entry.getValue());
                } else {
                    return false;
                }
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    //dirty trick - clone the entry set
                    Iterator<Entry<K, WeakReference<V>>> entryIterator = new ArrayList<>(WeakBiMap.this.normal.entrySet()).iterator();
                    BiEntry<K, V> lastEntry = null;

                    @Override
                    public boolean hasNext() {
                        return entryIterator.hasNext();
                    }

                    @Override
                    public BiEntry<K, V> next() {
                        Entry<K, WeakReference<V>> next = entryIterator.next();
                        return lastEntry = new BiEntry<K, V>() {
                            K resetKey = null;
                            V resetValue = null;

                            @Override
                            public K getKey() {
                                if (resetKey == null) {
                                    resetKey = next.getKey();
                                }
                                return resetKey;
                            }

                            @Override
                            public V getValue() {
                                if (resetValue == null) {
                                    WeakReference<V> valueRef = next.getValue();
                                    resetValue = valueRef == null ? null : valueRef.get();
                                }
                                return resetValue;
                            }

                            @Override
                            public V setValue(V value) {
                                resetValue = value;
                                K key = getKey();
                                V oldValue = getValue();
                                WeakBiMap.this.put(key, value);
                                return oldValue;
                            }

                            @Override
                            public K setKey(K key) {
                                resetKey = key;
                                V value = getValue();
                                K oldKey = getKey();
                                WeakBiMap.this.put(key, value);
                                return oldKey;
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        if (lastEntry != null) {
                            WeakBiMap.this.remove(lastEntry.getKey(), lastEntry.getValue());
                        }
                    }
                };
            }

            @Override
            public boolean add(Entry<K, V> entry) {
                boolean contains = WeakBiMap.this.containsKey(entry.getKey()) && Objects.equals(WeakBiMap.this.get(entry.getKey()), entry.getValue());
                WeakBiMap.this.put(entry.getKey(), entry.getValue());
                return !contains;
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry)) return false;
                Map.Entry entry = (Map.Entry) o;
                return WeakBiMap.this.remove(entry.getKey(), entry.getValue());
            }

            @Override
            public void clear() {
                WeakBiMap.this.clear();
            }
        };
    }

    public class Reversed extends AbstractMap<V, K> {
        private Reversed() {}

        public WeakBiMap<K, V> swap() {
            return WeakBiMap.this;
        }

        @Override
        public int size() {
            return WeakBiMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return WeakBiMap.this.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return WeakBiMap.this.containsValue(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return WeakBiMap.this.containsKey(value);
        }

        @Override
        public K get(Object key) {
            WeakReference<K> keyRef = inverse.get(key);
            return keyRef == null ? null : keyRef.get();
        }

        @Override
        public K put(V key, K value) {
            K oldValue = get(key);
            WeakBiMap.this.put(value, key);
            return oldValue;
        }

        @Override
        public K remove(Object key) {
            WeakReference<K> keyRef = inverse.remove(key);
            if (keyRef != null) {
                K value = keyRef.get();
                if (value != null) {
                    normal.remove(value);
                }
                return value;
            } else {
                return null;
            }
        }

        @Override
        public void clear() {
            WeakBiMap.this.clear();
        }

        @Override
        public Set<V> keySet() {
            Collection<V> values = WeakBiMap.this.values();

            return new AbstractSet<V>() {
                @Override
                public int size() {
                    return values.size();
                }

                @Override
                public boolean isEmpty() {
                    return values.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    return values.contains(o);
                }

                @Override
                public Iterator<V> iterator() {
                    Iterator<V> valueIterator = values.iterator();
                    return new Iterator<V>() {
                        @Override
                        public boolean hasNext() {
                            return valueIterator.hasNext();
                        }

                        @Override
                        public V next() {
                            return valueIterator.next();
                        }

                        @Override
                        public void remove() {
                            valueIterator.remove();
                        }
                    };
                }

                @Override
                public Object[] toArray() {
                    return values.toArray();
                }

                @Override
                public <T> T[] toArray(T[] a) {
                    return values.toArray(a);
                }

                @Override
                public boolean add(V v) {
                    throw new UnsupportedOperationException("Can't add a value to a key set view");
                }

                @Override
                public boolean remove(Object o) {
                    return values.remove(o);
                }

                @Override
                public boolean containsAll(Collection<?> c) {
                    return values.containsAll(c);
                }

                @Override
                public boolean addAll(Collection<? extends V> c) {
                    throw new UnsupportedOperationException("Can't add values to a key set view");
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    return values.retainAll(c);
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    return values.removeAll(c);
                }

                @Override
                public void clear() {
                    values.clear();
                }
            };
        }

        @Override
        public Collection<K> values() {
            return WeakBiMap.this.keySet();
        }

        @Override
        public Set<Entry<V, K>> entrySet() {
            Set<Entry<K, V>> entrySet = WeakBiMap.this.entrySet();

            return new AbstractSet<Entry<V, K>> () {

                @Override
                public int size() {
                    return entrySet.size();
                }

                @Override
                public boolean isEmpty() {
                    return entrySet.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    if (o instanceof Map.Entry) {
                        Map.Entry<V, K> entry = (Map.Entry<V, K>) o;
                        return entrySet.contains(Map.entry(entry.getValue(), entry.getKey()));
                    } else {
                        return false;
                    }
                }

                @Override
                public Iterator<Entry<V, K>> iterator() {
                    Iterator<Entry<K, V>> iterator = entrySet.iterator();

                    return new Iterator<Entry<V, K>>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public BiEntry<V, K> next() {
                            var entry = (BiEntry<K, V>) iterator.next();
                            return new BiEntry<>() {
                                @Override
                                public V getKey() {
                                    return entry.getValue();
                                }

                                @Override
                                public K getValue() {
                                    return entry.getKey();
                                }

                                @Override
                                public V setKey(V newKey) {
                                    return entry.setValue(newKey);
                                }

                                @Override
                                public K setValue(K newValue) {
                                    return entry.setKey(newValue);
                                }
                            };
                        }

                        @Override
                        public void remove() {
                            iterator.remove();
                        }
                    };
                }

                @Override
                public boolean add(Entry<V, K> vkEntry) {
                    V key = vkEntry.getKey();
                    K value = vkEntry.getValue();
                    boolean contains = Reversed.this.containsKey(key) && Objects.equals(Reversed.this.get(key), value);
                    Reversed.this.put(key, value);
                    return !contains;
                }

                @Override
                public boolean remove(Object o) {
                    if (o instanceof Map.Entry) {
                        Map.Entry entry = (Map.Entry) o;
                        return entrySet.contains(Map.entry(entry.getValue(), entry.getKey()));
                    } else {
                        return false;
                    }
                }

                @Override
                public void clear() {
                    entrySet.clear();
                }
            };
        }
    };

    public static class BiEntry<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;

        private BiEntry() {
        }

        public BiEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

        public K setKey(K newKey) {
            K old = getKey();
            this.key = newKey;
            return old;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            return Objects.equals(this.getKey(), e.getKey())
                    && Objects.equals(this.getValue(), e.getValue());
        }

        public int hashCode() {
            return Objects.hashCode(getKey()) ^ (Objects.hashCode(getValue()));
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
