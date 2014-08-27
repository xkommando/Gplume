/*
 * *****************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.misc;

/**
 * @author bowen.cbw
 * @since 8/26/2014.
 */
public final class Bits {

    private Bits(){}

    public static final class shorts {
        public static final short add(byte a, byte b) {
            return (short)(((short)a << 8) | (short)b & 0xff );
        }

        public static final byte part1(short c) {
            return (byte)((c >> 8) & 0xff);
        }

        public static final byte part2(short c) {
            return (byte)(c & 0xff);
        }


        public static final void fill(byte[] arr, int start, short c) {
            arr[start++] = part1(c);
            arr[1] = part2(c);
        }

        public static final short add(byte[] arr, int start) {
            return add(arr[start++], arr[start]);
        }
    }

    public static final class ints {
        public static final int add(short a, short b) {
            return ((int)a << 16) | ((int)b & 0xFFFF);
        }

        public static final short part1(int c) {
            return (short)(c >> 16);
        }

        public static final short part2(int c) {
            return (short)c;
        }

        public static final void fill(byte[] arr, int start, int c) {
            arr[start++] = shorts.part1(part1(c));
            arr[start++] = shorts.part2(part1(c));
            arr[start++] = shorts.part1(part2(c));
            arr[start++] = shorts.part2(part2(c));
        }
        public static final int add(byte[] arr, int start) {
            return add(shorts.add(arr[start++], arr[start++]),
                    shorts.add(arr[start++], arr[start]));
        }
    }


    public static final class longs{

        public static final long add(int a, int b) {
            return ((long)a << 32) | ((long)b & 0xFFFFFFFFL);
        }

        public static final int part1(long c) {
            return (int)(c >> 32);
        }

        public static final int part2(long c) {
            return (int)c;
        }

        public static final void fill(byte[] arr, int start, long c) {
            arr[start++] = shorts.part1(ints.part1(part1(c)));
            arr[start++] = shorts.part2(ints.part1(part1(c)));
            arr[start++] = shorts.part1(ints.part2(part1(c)));
            arr[start++] = shorts.part2(ints.part2(part1(c)));
            arr[start++] = shorts.part1(ints.part1(part2(c)));
            arr[start++] = shorts.part2(ints.part1(part2(c)));
            arr[start++] = shorts.part1(ints.part2(part2(c)));
            arr[start++] = shorts.part2(ints.part2(part2(c)));
        }
        public static final long add(byte[] arr, int start) {
            return add(ints.add(arr, start),
                        ints.add(arr, start + 4));
        }
    }


    public static final int add(byte b1, byte b2, byte b3, byte b4) {
        return ints.add(shorts.add(b1, b2), shorts.add(b3, b4));
    }

    public static final byte part1(int a) {
        return shorts.part1(ints.part1(a));
    }
    public static final byte part2(int a) {
        return shorts.part2(ints.part1(a));
    }
    public static final byte part3(int a) {
        return shorts.part1(ints.part1(a));
    }
    public static final byte part4(int a) {
        return shorts.part2(ints.part1(a));
    }

    public static final byte part(int a, int index) {
        switch (index) {
            case 0:
                return part1(a);
            case 1:
                return part2(a);
            case 2:
                return part3(a);
            case 3:
                return part4(a);
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static final long add(short b1, short b2, short b3, short b4) {
        return longs.add(ints.add(b1, b2), ints.add(b3, b4));
    }
    public static final short part1(long a) {
        return ints.part1(longs.part1(a));
    }
    public static final short part2(long a) {
        return ints.part2(longs.part1(a));
    }
    public static final short part3(long a) {
        return ints.part1(longs.part1(a));
    }
    public static final short part4(long a) {
        return ints.part2(longs.part1(a));
    }

    public static final short part(long a, int index) {
        switch (index) {
            case 0:
                return part1(a);
            case 1:
                return part2(a);
            case 2:
                return part3(a);
            case 3:
                return part4(a);
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }


    public static final long add(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        return longs.add(
                ints.add(shorts.add(b1, b2),
                        shorts.add(b3, b4)),

                ints.add(shorts.add(b5, b6),
                        shorts.add(b7, b8)));
    }

    public static final byte byte1(long a) {
        return shorts.part1(ints.part1(longs.part1(a)));
    }
    public static final byte byte2(long a) {
        return shorts.part2(ints.part1(longs.part1(a)));
    }
    public static final byte byte3(long a) {
        return shorts.part1(ints.part2(longs.part1(a)));
    }
    public static final byte byte4(long a) {
        return shorts.part2(ints.part2(longs.part1(a)));
    }
    public static final byte byte5(long a) {
        return shorts.part1(ints.part1(longs.part2(a)));
    }
    public static final byte byte6(long a) {
        return shorts.part2(ints.part1(longs.part2(a)));
    }
    public static final byte byte7(long a) {
        return shorts.part1(ints.part2(longs.part2(a)));
    }
    public static final byte byte8(long a) {
        return shorts.part2(ints.part2(longs.part2(a)));
    }

    public static final byte byteAt(long c, int index) {
        switch (index) {
            case 0:
                return byte1(c);
            case 1:
                return byte2(c);
            case 2:
                return byte3(c);
            case 3:
                return byte4(c);
            case 4:
                return byte5(c);
            case 5:
                return byte6(c);
            case 6:
                return byte7(c);
            case 7:
                return byte8(c);
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static byte[] buildHead(boolean uniKey, boolean renewable, int keyLife, int authLife) {
        byte[] head = new byte[18];
        head[0] = uniKey ? (byte)0x01 : (byte)0x00;
        head[1] = renewable ? (byte)0x01 : (byte)0x00;
        Bits.ints.fill(head, 2, keyLife);
        Bits.ints.fill(head, 6, authLife);
        return head;
    }

    public static void main(String...asfsdf) {
        boolean unikey = true;
        boolean renewable = true;
        int keyExp = 454545;
        int authExp = 454564;
        byte[] h = buildHead(unikey, renewable, keyExp, authExp);

        boolean uniKey2 = h[0] == (byte)0x00 ? false : true;
        boolean renewable2 = h[1] == (byte)0x00 ? false : true;
        int keyExp2 = ints.add(h, 2);
        int authExp2 = ints.add(h, 6);

//        byte b1 = 0;
//        byte b2 = 0;
//        byte b3 = 0;
//        byte b4 = 0;
//        byte b11 = -1;
//        byte b22 = 0;
//        byte b33 = 0;
//        byte b44 = 0;
//        long c = add(b1, b2, b3, b4, b11, b22, b33, b44);
//
//        for (int i = 0; i < 8; i++) {
//            byte val = byteAt(c, i);
//            System.out.println(val);
//        }


    }
}
