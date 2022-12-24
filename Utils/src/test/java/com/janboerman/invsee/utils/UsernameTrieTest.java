package com.janboerman.invsee.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class UsernameTrieTest {

    @Test
    public void testDisjunct() {
        UsernameTrie<Integer> trie = new UsernameTrie<>();
        trie.insert("Yolo", 0);
        trie.insert("Swag", 1);
        trie.insert("Lmao", 2);
        trie.insert("wpimp", 3);

        StringJoiner sj = new StringJoiner(" ");
        trie.traverse("", (string, idx) -> sj.add(string + ":" + idx));
        assertEquals("wpimp:3 Lmao:2 Swag:1 Yolo:0", sj.toString());

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


}
