package com.janboerman.invsee.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.Map.Entry;

/**
 * <p>
 *  This class represents a Trie, specialised for minecraft usernames.
 * </p>
 * <p>
 *  A Trie&ltV&gt is a datastructure much like Map&ltK, V&gt, but optimised for string keys.
 * </p>
 * <p>
 *  This Trie implementation does not support {@code null} keys.
 * </p>
 * @param <V> the value type
 */
public class UsernameTrie<V> {

    //TODO we might want to be case in-sensitive, but goes at the cost of tab-completing names in their right casing.
    //TODO can we make this a lock-free thread-safe data structure?

    private final Node<V> root;

    /**
     * Constructs a UsernameTrie with value {@code rootValue} for the empty string key.
     * @param rootValue the value for the empty key
     */
    public UsernameTrie(V rootValue) {
        this.root = new Node<>(new char[0], Maybe.just(rootValue), null);
    }

    public UsernameTrie() {
        this.root = new Node<>(new char[0], Maybe.nothing(), null);
    }

    public Maybe<V> insert(String username, V value) {
        return insert(username.toCharArray(), value);
    }

    public synchronized Maybe<V> insert(char[] username, V value) {
        Node<V> node = root.lookup(username);
        Maybe<V> oldValue = node.value;
        node.value = Maybe.just(value);
        return oldValue;
    }

    public Maybe<V> delete(String username) {
        return delete(username.toCharArray());
    }

    public synchronized Maybe<V> delete(char[] username) {
        Node<V> node = root.lookup(username);
        Maybe<V> oldValue = node.value;
        node.value = Maybe.nothing();
        node.cleanUp();
        return oldValue;
    }

    public Maybe<V> get(String username) {
        return get(username.toCharArray());
    }

    public synchronized Maybe<V> get(char[] username) {
        Node<V> node = root.lookup(username);
        Maybe<V> value = node.value;
        node.cleanUp();
        return value;
    }

    public void traverse(String prefix, BiConsumer<String, ? super V> consumer) {
        traverse(prefix.toCharArray(), (chars, v) -> consumer.accept(new String(chars), v));
    }

    public synchronized void traverse(char[] prefix, BiConsumer<char[], ? super V> consumer) {
        Node<V> node = root.lookup(prefix);
        node.traverse(consumer);
        node.cleanUp();
    }

    private static class Node<V> {

        //comparator that ignores case, except when the characters are equalsIgnoreCase
        private static final Comparator<Character> CHAR_COMPARATOR = (Character character1, Character character2) -> {
            char c1 = character1.charValue(), c2 = character2.charValue();
            if (Character.isLetter(c1) && Character.isLetter(c2)) {
                //both are letters
                char l1 = Character.toLowerCase(c1), l2 = Character.toLowerCase(c2);
                if (l1 == l2) {
                    //equalsIgnoreCase -> compare normally!
                    return Character.compare(c1, c2);
                } else {
                    //unequal characters -> compare ignoring case!
                    return Character.compare(l1, l2);
                }
            } else if (Character.isLetter(c1)) {
                //letters come after other characters
                return 1;
            } else if (Character.isLetter(c2)) {
                //non-letters come before letters
                return -1;
            } else {
                //both are non-letters
                return Character.compare(c1, c2);
            }
        };

        /*not-null, except for the root node*/
        private final char[] segment;
        /*not null*/
        private Maybe<V> value;

        /*nullable*/
        private TreeMap<Character, Node<V>> children;
        /*not-null, except for the root node*/
        private Node<V> parent;

        private Node(char[] segment, Maybe<V> value, Node<V> parent) {
            this(segment, value, null, parent);
        }

        private Node(char[] segment, Maybe<V> value, TreeMap<Character, Node<V>> children, Node<V> parent) {
            assert segment != null : "segment cannot be null";
            assert value != null : "value cannot be null";

            this.segment = segment;
            this.value = value;
            this.children = children;
            this.parent = parent;
        }

        private boolean isEmpty() {
            // do we have a value?
            if (value.isPresent())
                return false;
            // are all our children empty?
            if (children == null)
                return true;
            for (Node<V> child : children.values())
                if (child != null && !child.isEmpty())
                    return false;
            // if the answers to both questions are 'yes', then we are empty!
            return true;
        }

        private void cleanUp() {
            if (parent != null) {
                if (isEmpty()) {
                    //we are empty
                    //remove `this` from parent:

                    parent.children.remove(segment[0]);
                    parent.cleanUp();
                } else if (!value.isPresent()) {
                    //we have no value
                    //if we are the single node between to others, make our child a direct child of our parent:

                    for (Character sibling : parent.children.keySet()) {
                        if (!sibling.equals(segment[0])) return; //we are in fact not the only child.
                    }

                    if (children == null || children.isEmpty()) return; //we don't in fact have any children
                    if (children.size() > 1) return; //we have more than one child.

                    final Entry<Character, Node<V>> entry = children.firstEntry(); //do we want to poll?
                    final Node<V> theNode = entry.getValue();

                    final char[] newSegment = ArrayHelper.concat(segment, theNode.segment);
                    final Node<V> longNode = new Node<>(newSegment, theNode.value, theNode.children, parent);
                    parent.children.put(newSegment[0], longNode); //replace ourselves
                    parent.cleanUp();
                }
                //else: we do have a value, so don't perform any clean-up.
            }
            //else: there is no parent, we are the root!
        }

        private void ensureChildrenNotNull() {
            if (children == null) children = new TreeMap<Character, Node<V>>(CHAR_COMPARATOR);
        }

        private Node<V> lookup(final char[] segment) {
            assert segment != null : "lookup segment cannot be null";

            if (segment.length == 0) return this;

            ensureChildrenNotNull();
            //final int atIndex = Username.toIndex(segment[0]);
            final char childKey = segment[0];
            Node<V> child = children.get(childKey);
            if (child == null) {
                //the child does not exist!
                //create a new one and return it!
                child = new Node<>(segment, Maybe.nothing(), this);
                this.children.put(childKey, child);
                return child;
            } else {
                //the child does exist!
                //we need to find the common prefix

                final char[] childSegment = child.segment;
                int i;
                for (i = 0; i < childSegment.length && i < segment.length && childSegment[i] == segment[i]; i += 1);

                //i is the index at which they differ.
                //if i is equal to the lowest length of the two segments, then the segements didn't differ!

                if (i == Math.min(childSegment.length, segment.length)) {
                    //case distinction:
                    //  |childSegment| == |segment| ==> replace value!
                    //  |childSegment| > |segment| ==> 'segment' becomes the new child of `this`, and 'childSegment' gets its tail as a child of 'segment'
                    //  |childSegment| < |segment| ==> suffix of 'segment' becomes a child of 'childSegment'.

                    if (childSegment.length == segment.length) {
                        //the segments are the same!
                        return child;
                    } else if (childSegment.length < segment.length) {
                        // lookup the suffix of 'segment' for the child
                        char[] suffix = Arrays.copyOfRange(segment, childSegment.length, segment.length);
                        return child.lookup(suffix);
                    } else {
                        assert childSegment.length > segment.length;
                        // insert 'segment' in between
                        char[] suffix = Arrays.copyOfRange(childSegment, segment.length, childSegment.length);
                        Node<V> replacingChild = new Node<>(segment, Maybe.nothing(), this);
                        Node<V> grandChild = new Node<>(suffix, child.value, child.children, replacingChild);
                        replacingChild.ensureChildrenNotNull();
                        replacingChild.children.put(suffix[0], grandChild);
                        this.children.put(childKey, replacingChild);

                        return replacingChild;
                    }
                } else {
                    // childSegment and segment have a common prefix, and one does not subsume the other!

                    // we need to split up childSegment to the common prefix
                    // then, create three nodes:
                    //  - replacingChild which replaces the current child.
                    //  - grandChild1 which becomes a child of replacingChild (suffix from childSegment)
                    //  - grandChild2 which becomes a child of replacingChild (suffix from segment)
                    final char[] commonPrefix = Arrays.copyOfRange(childSegment, 0, i);
                    final char[] suffixChildSegment = Arrays.copyOfRange(childSegment, i, childSegment.length);
                    final char[] suffixSegment = Arrays.copyOfRange(segment, i, segment.length);

                    final Node<V> replacingChild = new Node<>(commonPrefix, Maybe.nothing(), new TreeMap<>(CHAR_COMPARATOR) /*fill later*/, this);
                    final Node<V> grandChild1 = new Node<>(suffixChildSegment, child.value, child.children, replacingChild);
                    final Node<V> grandChild2 = new Node<>(suffixSegment, Maybe.nothing(), null, replacingChild);
                    replacingChild.children.put(suffixChildSegment[0], grandChild1);
                    replacingChild.children.put(suffixSegment[0], grandChild2);

                    this.children.put(childKey, replacingChild);
                    return grandChild2;
                }
            }
        }

        private int length() {
            return (parent == null ? 0 : parent.length()) + segment.length;
        }

        private char[] fullString() {
            int last = length();
            final char[] result = new char[last];
            Node<V> node = this;
            while (node != null) {
                final char[] segment = node.segment;
                final int segmentLength = segment.length;
                System.arraycopy(segment, 0, result, last - segmentLength, segmentLength);
                last -= segmentLength;
                node = node.parent;
            }
            return result;
        }

        private void traverse(BiConsumer<char[], ? super V> consumer) {
            //when we get here - we are guaranteed that 'this' is a node

            //only accept nodes whose value are Just(something)
            if (value.isPresent()) {
                consumer.accept(fullString(), value.get());
            }

            if (children != null) {
                for (Node<V> child : children.values()) {
                    if (child != null) {
                        child.traverse(consumer);
                    }
                }
            }
        }
    }

}

