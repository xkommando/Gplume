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
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    public static final class MurmurHash {

        public static int hash32(byte[] data, int offset, int length, int seed) {
            int m = 0x5bd1e995;
            int r = 24;

            int h = seed ^ length;

            int len_4 = length >> 2;

            for (int i = 0; i < len_4; i++) {
                int i_4 = i << 2;
                int k = data[offset + i_4 + 3];
                k = k << 8;
                k = k | (data[offset + i_4 + 2] & 0xff);
                k = k << 8;
                k = k | (data[offset + i_4 + 1] & 0xff);
                k = k << 8;
                k = k | (data[offset + i_4 + 0] & 0xff);
                k *= m;
                k ^= k >>> r;
                k *= m;
                h *= m;
                h ^= k;
            }

            // avoid calculating modulo
            int len_m = len_4 << 2;
            int left = length - len_m;

            if (left != 0) {
                if (left >= 3) {
                    h ^= (int) data[offset + length - 3] << 16;
                }
                if (left >= 2) {
                    h ^= (int) data[offset + length - 2] << 8;
                }
                if (left >= 1) {
                    h ^= (int) data[offset + length - 1];
                }

                h *= m;
            }

            h ^= h >>> 13;
            h *= m;
            h ^= h >>> 15;

            return h;
        }

        public static long hash64(byte[] key, int offset, int length, long seed) {
            long m64 = 0xc6a4a7935bd1e995L;
            int r64 = 47;

            long h64 = (seed & 0xffffffffL) ^ (m64 * length);

            int lenLongs = length >> 3;

            for (int i = 0; i < lenLongs; ++i) {
                int i_8 = i << 3;

                long k64 = ((long) key[offset + i_8 + 0] & 0xff) + (((long) key[offset + i_8 + 1] & 0xff) << 8) +
                        (((long) key[offset + i_8 + 2] & 0xff) << 16) + (((long) key[offset + i_8 + 3] & 0xff) << 24) +
                        (((long) key[offset + i_8 + 4] & 0xff) << 32) + (((long) key[offset + i_8 + 5] & 0xff) << 40) +
                        (((long) key[offset + i_8 + 6] & 0xff) << 48) + (((long) key[offset + i_8 + 7] & 0xff) << 56);

                k64 *= m64;
                k64 ^= k64 >>> r64;
                k64 *= m64;

                h64 ^= k64;
                h64 *= m64;
            }

            int rem = length & 0x7;

            switch (rem) {
                case 0:
                    break;
                case 7:
                    h64 ^= (long) key[offset + length - rem + 6] << 48;
                case 6:
                    h64 ^= (long) key[offset + length - rem + 5] << 40;
                case 5:
                    h64 ^= (long) key[offset + length - rem + 4] << 32;
                case 4:
                    h64 ^= (long) key[offset + length - rem + 3] << 24;
                case 3:
                    h64 ^= (long) key[offset + length - rem + 2] << 16;
                case 2:
                    h64 ^= (long) key[offset + length - rem + 1] << 8;
                case 1:
                    h64 ^= (long) key[offset + length - rem];
                    h64 *= m64;
            }

            h64 ^= h64 >>> r64;
            h64 *= m64;
            h64 ^= h64 >>> r64;

            return h64;
        }

        protected static long getblock(byte[] key, int offset, int index) {
            int i_8 = index << 3;
            return ((long) key[offset + i_8 + 0] & 0xff) + (((long) key[offset + i_8 + 1] & 0xff) << 8) +
                    (((long) key[offset + i_8 + 2] & 0xff) << 16) + (((long) key[offset + i_8 + 3] & 0xff) << 24) +
                    (((long) key[offset + i_8 + 4] & 0xff) << 32) + (((long) key[offset + i_8 + 5] & 0xff) << 40) +
                    (((long) key[offset + i_8 + 6] & 0xff) << 48) + (((long) key[offset + i_8 + 7] & 0xff) << 56);
        }

        protected static long rotl64(long v, int n) {
            return ((v << n) | (v >>> (64 - n)));
        }

        protected static long fmix(long k) {
            k ^= k >>> 33;
            k *= 0xff51afd7ed558ccdL;
            k ^= k >>> 33;
            k *= 0xc4ceb9fe1a85ec53L;
            k ^= k >>> 33;

            return k;
        }

        public static long[] hash3_x64_128(byte[] key, int offset, int length, long seed) {
            final int nblocks = length >> 4; // Process as 128-bit blocks.

            long h1 = seed;
            long h2 = seed;

            long c1 = 0x87c37b91114253d5L;
            long c2 = 0x4cf5ad432745937fL;

            //----------
            // body

            for (int i = 0; i < nblocks; i++) {

                long k1 = getblock(key, offset, i * 2 + 0);
                long k2 = getblock(key, offset, i * 2 + 1);

                k1 *= c1;
                k1 = rotl64(k1, 31);
                k1 *= c2;
                h1 ^= k1;

                h1 = rotl64(h1, 27);
                h1 += h2;
                h1 = h1 * 5 + 0x52dce729;

                k2 *= c2;
                k2 = rotl64(k2, 33);
                k2 *= c1;
                h2 ^= k2;

                h2 = rotl64(h2, 31);
                h2 += h1;
                h2 = h2 * 5 + 0x38495ab5;
            }

            //----------
            // tail

            // Advance offset to the unprocessed tail of the data.
            offset += nblocks * 16;

            long k1 = 0;
            long k2 = 0;

            switch (length & 15) {
                case 15:
                    k2 ^= ((long) key[offset + 14]) << 48;
                case 14:
                    k2 ^= ((long) key[offset + 13]) << 40;
                case 13:
                    k2 ^= ((long) key[offset + 12]) << 32;
                case 12:
                    k2 ^= ((long) key[offset + 11]) << 24;
                case 11:
                    k2 ^= ((long) key[offset + 10]) << 16;
                case 10:
                    k2 ^= ((long) key[offset + 9]) << 8;
                case 9:
                    k2 ^= ((long) key[offset + 8]) << 0;
                    k2 *= c2;
                    k2 = rotl64(k2, 33);
                    k2 *= c1;
                    h2 ^= k2;

                case 8:
                    k1 ^= ((long) key[offset + 7]) << 56;
                case 7:
                    k1 ^= ((long) key[offset + 6]) << 48;
                case 6:
                    k1 ^= ((long) key[offset + 5]) << 40;
                case 5:
                    k1 ^= ((long) key[offset + 4]) << 32;
                case 4:
                    k1 ^= ((long) key[offset + 3]) << 24;
                case 3:
                    k1 ^= ((long) key[offset + 2]) << 16;
                case 2:
                    k1 ^= ((long) key[offset + 1]) << 8;
                case 1:
                    k1 ^= ((long) key[offset]);
                    k1 *= c1;
                    k1 = rotl64(k1, 31);
                    k1 *= c2;
                    h1 ^= k1;
            }
            ;

            //----------
            // finalization

            h1 ^= length;
            h2 ^= length;

            h1 += h2;
            h2 += h1;

            h1 = fmix(h1);
            h2 = fmix(h2);

            h1 += h2;
            h2 += h1;

            return (new long[]{h1, h2});
        }
    }
}
