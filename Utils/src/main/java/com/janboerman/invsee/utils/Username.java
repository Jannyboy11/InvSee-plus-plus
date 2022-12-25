package com.janboerman.invsee.utils;

import java.util.Arrays;

public class Username {

    //https://help.minecraft.net/hc/en-us/articles/4408950195341-Minecraft-Java-Edition-Username-VS-Gamertag-FAQ#:~:text=Accepted%20characters%3A,character%20accepted%20is%20_%20(underscore)

    private Username() {
    }

    public static boolean isValidCharacter(char c) {
        return ('a' <= c && c <= 'z')
                || ('A' <= c && c <= 'Z')
                || ('0' <= c && c <= '9')
                || ('_' == c);
    }

    public static void assertValidUsername(char[] username) {
        assert username != null : "Username cannot be null";
        assert username.length >= 3 : "Username must be at least 3 characters long, got: " + Arrays.toString(username);
        assert username.length <= 16 : "Username must be at most 16 characters long, got: " + Arrays.toString(username);
        for (int i = 0; i < username.length; i++) {
            assert isValidCharacter(username[i]) : "Found invalid character at position " + i + ": " + username[i] + " in username " + Arrays.toString(username);
        }
    }

    public static boolean isValidCharacters(char[] username) {
        for (int i = 0; i < username.length; i++) {
            if (!isValidCharacter(username[i])) return false;
        }
        return true;
    }

    public static boolean isValidUsername(char[] username) {
        return username != null && 3 <= username.length && username.length <= 16 && isValidCharacters(username);
    }


    // === package-private methods ===

    static int toIndex(char c) {

        //order:
        //[_,
        // 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        // a, A, b, B, c, C, d, D, e, E, f, F, g, G, h, H, i, I, j, J, k, K, l, L, m, M, n, N, o, O, p, P, q, Q, r, R, s, S, t, T, u, U, v, V, w, W, x, X, y, Y, z, Z]

        if (c == '_') {
            return 0;
        } else if ('0' <= c && c <= '9') {
            return 1 + c - '0';
        } else if ('a' <= c && c <= 'z') {
            return 1 + 10 + (c - 'a') * 2;
        } else if ('A' <= c && c <= 'Z') {
            return 1 + 10 + (c - 'A') * 2 + 1;
        } else {
            assert false : "invalid input character: " + c + ", expected: [a-zA-Z_0-9]";
            return -1;
        }
    }

    static int lookupTableSize() {
        return 1 + 10 + 26 + 26;
    }

}
