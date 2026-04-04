package dev.faststats.core;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of the MurmurHash3 128-bit hash algorithm.
 * <p>
 * MurmurHash is a non-cryptographic hash function suitable for general hash-based lookup.
 * It provides excellent distribution and performance while minimizing collisions.
 * </p>
 * <p>
 * This implementation follows the MurmurHash3_x64_128 variant as described at:
 * <a href="https://en.wikipedia.org/wiki/MurmurHash">https://en.wikipedia.org/wiki/MurmurHash</a>
 * </p>
 * <p>
 * Original algorithm by Austin Appleby. The name comes from the two elementary operations
 * it uses: multiply (MU) and rotate (R).
 * </p>
 */
final class MurmurHash3 {
    public static String hash(final JsonObject object) {
        final long[] hash = MurmurHash3.hash(object.toString());
        return Long.toHexString(hash[0]) + Long.toHexString(hash[1]);
    }

    /**
     * Computes the 128-bit MurmurHash3 hash of the input string.
     * <p>
     * The string is encoded to UTF-8 bytes before hashing. The result is returned
     * as an array of two long values (64 bits each), combined they form a 128-bit hash.
     * </p>
     *
     * @param data the input string to hash
     * @return a 2-element array containing the lower 64 bits at index 0 and upper 64 bits at index 1
     * @see <a href="https://en.wikipedia.org/wiki/MurmurHash">MurmurHash on Wikipedia</a>
     */
    @Contract(value = "_ -> new", pure = true)
    private static long[] hash(final String data) {
        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        long h1 = 0L;
        long h2 = 0L;
        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;
        final int length = bytes.length;
        final int blocks = length / 16;

        // Process 128-bit blocks
        for (int i = 0; i < blocks; i++) {
            int k1 = getInt(bytes, i * 16);
            int k2 = getInt(bytes, i * 16 + 4);
            final int k3 = getInt(bytes, i * 16 + 8);
            final int k4 = getInt(bytes, i * 16 + 12);

            k1 *= (int) c1;
            k1 = Integer.rotateLeft(k1, 31);
            k1 *= (int) c2;
            h1 ^= k1;

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= (int) c2;
            k2 = Integer.rotateLeft(k2, 33);
            k2 *= (int) c1;
            h2 ^= k2;

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        // Tail
        int k1 = 0;
        int k2 = 0;
        int k3 = 0;
        int k4 = 0;
        final int tail = blocks * 16;

        switch (length & 15) {
            case 15:
                k4 ^= (bytes[tail + 14] & 0xff) << 16;
            case 14:
                k4 ^= (bytes[tail + 13] & 0xff) << 8;
            case 13:
                k4 ^= (bytes[tail + 12] & 0xff);
                k4 *= (int) c2;
                k4 = Integer.rotateLeft(k4, 33);
                k4 *= (int) c1;
                h2 ^= k4;
            case 12:
                k3 ^= (bytes[tail + 11] & 0xff) << 24;
            case 11:
                k3 ^= (bytes[tail + 10] & 0xff) << 16;
            case 10:
                k3 ^= (bytes[tail + 9] & 0xff) << 8;
            case 9:
                k3 ^= (bytes[tail + 8] & 0xff);
                k3 *= (int) c1;
                k3 = Integer.rotateLeft(k3, 31);
                k3 *= (int) c2;
                h1 ^= k3;
            case 8:
                k2 ^= (bytes[tail + 7] & 0xff) << 24;
            case 7:
                k2 ^= (bytes[tail + 6] & 0xff) << 16;
            case 6:
                k2 ^= (bytes[tail + 5] & 0xff) << 8;
            case 5:
                k2 ^= (bytes[tail + 4] & 0xff);
                k2 *= (int) c2;
                k2 = Integer.rotateLeft(k2, 33);
                k2 *= (int) c1;
                h2 ^= k2;
            case 4:
                k1 ^= (bytes[tail + 3] & 0xff) << 24;
            case 3:
                k1 ^= (bytes[tail + 2] & 0xff) << 16;
            case 2:
                k1 ^= (bytes[tail + 1] & 0xff) << 8;
            case 1:
                k1 ^= (bytes[tail] & 0xff);
                k1 *= (int) c1;
                k1 = Integer.rotateLeft(k1, 31);
                k1 *= (int) c2;
                h1 ^= k1;
        }

        // Finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[]{h1, h2};
    }

    /**
     * Finalization mix function to avalanche the bits in the hash.
     * <p>
     * This function improves the distribution of the hash by XORing and multiplying
     * with carefully chosen constants, ensuring that similar inputs produce very
     * different outputs (avalanche effect).
     * </p>
     *
     * @param k the 64-bit value to mix
     * @return the mixed 64-bit value
     * @see <a href="https://en.wikipedia.org/wiki/MurmurHash#Algorithm">MurmurHash Algorithm on Wikipedia</a>
     */
    @Contract(pure = true)
    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    /**
     * Reads a 32-bit little-endian integer from the byte array at the specified offset.
     * <p>
     * This helper method extracts four consecutive bytes and combines them into a
     * single integer using little-endian byte order.
     * </p>
     *
     * @param bytes  the byte array to read from
     * @param offset the starting index in the byte array (must have at least 4 bytes from offset)
     * @return the 32-bit integer value read in little-endian order
     */
    @Contract(pure = true)
    private static int getInt(final byte[] bytes, final int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8) |
                ((bytes[offset + 2] & 0xff) << 16) |
                ((bytes[offset + 3] & 0xff) << 24);
    }
}
