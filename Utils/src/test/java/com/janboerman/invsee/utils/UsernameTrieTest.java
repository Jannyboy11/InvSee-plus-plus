package com.janboerman.invsee.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class UsernameTrieTest {

    private static final char[] valid_chars = new char[] {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '_'
    };

    @Test
    public void testDisjunct() {
        UsernameTrie<Integer> trie = new UsernameTrie<>();
        trie.insert("Yolo", 0);
        trie.insert("Swag", 1);
        trie.insert("Lmao", 2);
        trie.insert("wpimp", 3);

        StringJoiner sj = new StringJoiner(" ");
        trie.traverse("", (string, idx) -> sj.add(string + ":" + idx));
        assertEquals("Lmao:2 Swag:1 wpimp:3 Yolo:0", sj.toString());

        StringJoiner tabComplete = new StringJoiner(" ");
        trie.traverse("wp", (string, idx) -> tabComplete.add(string + ":" + idx));
        assertEquals("wpimp:3", tabComplete.toString());
    }

    @Test
    public void testOverlapping() {
        final UsernameTrie<Integer> trie = new UsernameTrie<>();
        trie.insert("Jannyboy11", 0);
        trie.insert("Jan", 1);
        trie.insert("Jankoekenpan", 2);
        trie.insert("Janko", 3);
        trie.insert("Jankebal", 4);

        final StringJoiner sj1 = new StringJoiner(" ");
        trie.traverse("", (string, idx) -> sj1.add(string + ":" + idx));        //empty string is prefix of all strings
        assertEquals("Jan:1 Jankebal:4 Janko:3 Jankoekenpan:2 Jannyboy11:0", sj1.toString());

        final StringJoiner sj2 = new StringJoiner(" ");
        trie.traverse("Jank", (string, idx) -> sj2.add(string + ":" + idx));    //non-existing word
        assertEquals("Jankebal:4 Janko:3 Jankoekenpan:2", sj2.toString());

        final StringJoiner sj3 = new StringJoiner(" ");
        trie.traverse("Jan", (string, idx) -> sj3.add(string + ":" + idx));     //existing word that is non-empty
        assertEquals("Jan:1 Jankebal:4 Janko:3 Jankoekenpan:2 Jannyboy11:0", sj3.toString());

        final StringJoiner sj4 = new StringJoiner(" ");
        trie.traverse("Janko", (string, idx) -> sj4.add(string + ":" + idx));     //existing word that is non-empty that is not a prefix of everything
        assertEquals("Janko:3 Jankoekenpan:2", sj4.toString());
    }

    @Test
    public void testInserts() {
        //example from Wikipedia: https://en.wikipedia.org/wiki/Radix_tree
        final String[] words = new String[] {"romane", "romanus", "romulus", "rubens", "ruber", "rubicon", "rubicundus"};

        UsernameTrie<Integer> trie = new UsernameTrie<>();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            trie.insert(word, i);
        }

        Set<Integer> remainingIndices = IntStream.range(0, words.length).collect(HashSet::new, Set::add, Set::addAll);
        trie.traverse("", (string, idx) -> {
            assert words[idx].equals(string);
            remainingIndices.remove(idx);
        });
        assert remainingIndices.isEmpty();
    }

    @Test
    public void testCasingDifferences() {
        final String[] words = new String[] {"Piet", "Pizza", "PiEs"};

        UsernameTrie<Void> trie = new UsernameTrie<>();
        for (String word : words) {
            trie.insert(word, null);
        }

        final StringJoiner sj = new StringJoiner(" ");
        trie.traverse("", (str, v) -> sj.add(str));
        assertEquals("PiEs Piet Pizza", sj.toString());
    }

    @Test
    public void testOrder() {
        UsernameTrie<Void> trie = new UsernameTrie<>();
        for (char c : valid_chars) {
            trie.insert(new char[] { c }, null);
        }
        StringJoiner sj = new StringJoiner("");
        trie.traverse("", (k, v) -> sj.add(k));
        assertEquals("0123456789_AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz", sj.toString());
    }

    @Test
    public void testDeletes() {
        UsernameTrie<Integer> trie = new UsernameTrie<>();
        trie.insert("Jannyboy11", 0);
        trie.insert("Jan", 1);
        trie.insert("Jankoekenpan", 2);
        trie.insert("Janko", 3);
        trie.insert("Jankebal", 4);

        trie.delete("Janko");

        final StringJoiner sj1 = new StringJoiner(" ");
        trie.traverse("", (string, idx) -> sj1.add(string + ":" + idx));
        assertEquals("Jan:1 Jankebal:4 Jankoekenpan:2 Jannyboy11:0", sj1.toString());

        trie.delete("Jan");

        final StringJoiner sj2 = new StringJoiner(" ");
        trie.traverse("", (string, idx) -> sj2.add(string + ":" + idx));
        assertEquals("Jankebal:4 Jankoekenpan:2 Jannyboy11:0", sj2.toString());

        trie.delete("Ja"); //non-existing key

        final StringJoiner sj3 = new StringJoiner(" ");
        trie.traverse("", (string, idx) -> sj3.add(string + ":" + idx));
        assertEquals("Jankebal:4 Jankoekenpan:2 Jannyboy11:0", sj3.toString());

        trie.delete("Jannyboy11");

        final StringJoiner sj4 = new StringJoiner(" ");
        trie.traverse("", (string, idx) -> sj4.add(string + ":" + idx));
        assertEquals("Jankebal:4 Jankoekenpan:2", sj4.toString());
    }

    @Test
    public void testCleanup() {
        UsernameTrie<Void> trie = new UsernameTrie<>();
        for (int i = 0; i < 1_000_000; i += 1) {
            trie.traverse(randomName(), (s, _void) -> {});
        }

        List<String> retrieved = new ArrayList<>();
        trie.traverse("", (s, _void) -> retrieved.add(s));

        assertEquals(List.of(), retrieved);
    }

    @Test
    public void time100k() {
        Set<String> set = new HashSet<>();
        UsernameTrie<Void> trie = new UsernameTrie<>();
        for (int i = 0; i < 100_000; i += 1) {
            char[] randomName = randomName();
            trie.insert(randomName, null);
            set.add(new String(randomName));
        }

        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);
        long begin = System.nanoTime();
        trie.traverse("", (string, v) -> { /*System.out.println(string);*/ counter.incrementAndGet(); });
        long end = System.nanoTime();
        assertEquals(set.size(), counter.get());
        //System.out.println("Printing took " + ((end - begin) / 1000) + " µs");
        //takes about 0.8 seconds on my laptop! :)
    }

    private final java.util.Random random = new java.util.Random();
    private char[] randomName() {
        int length = random.nextInt(16-3) + 3;
        char[] name = new char[length];
        for (int i = 0; i < length; i++) {
            name[i] = valid_chars[random.nextInt(valid_chars.length)];
        }
        return name;
    }

}
