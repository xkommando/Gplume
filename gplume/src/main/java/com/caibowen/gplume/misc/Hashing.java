package com.caibowen.gplume.misc;

/**
 * @author BowenCai
 * @since 22-8-2013.
 */
public class Hashing {


    public static long hash128To64(long upper, long lower) {
        // Murmur-inspired hashing.
        long kMul = 0x9ddfea08eb382d69L;
        long a = (lower ^ upper) * kMul;
        a ^= (a >> 47);
        long b = (upper ^ a) * kMul;
        b ^= (b >> 47);
        b *= kMul;
        return b;
    }

    public static long twang_mix64(long key) {
        key = (~key) + (key << 21);  // key *= (1 << 21) - 1; key -= 1;
        key = key ^ (key >> 24);
        key = key + (key << 3) + (key << 8);  // key *= 1 + (1 << 3) + (1 << 8)
        key = key ^ (key >> 14);
        key = key + (key << 2) + (key << 4);  // key *= 1 + (1 << 2) + (1 << 4)
        key = key ^ (key >> 28);
        key = key + (key << 31);  // key *= 1 + (1 << 31)
        return key;
    }
    public static long strLong(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();
        for (int i = 0; i < len; i++) {
            h = 31*h + string.charAt(i);
        }
        return h;
    }
}
