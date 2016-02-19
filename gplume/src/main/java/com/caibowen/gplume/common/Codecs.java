/*******************************************************************************
 * Copyright 2014 Bowen Cai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.caibowen.gplume.common;



/**
 * common codecs
 * @author BowenCai
 *
 */
public class Codecs {
	
	public static final class hex {

		private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

		public static String toHexStr(byte[] bytes) {
			return new String(toHexChars(bytes));
		}

		public static char[] toHexChars(byte[] bytes) {
		    char[] hexChars = new char[bytes.length * 2];
		    for ( int j = 0; j < bytes.length; j++ ) {
		        int v = bytes[j] & 0xFF;
		        hexChars[j * 2] = HEX_CHARS[v >>> 4];
		        hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
		    }
		    return hexChars;
		}

        public static byte[] fromHexChars(char[] chars) {
            byte[] bytes = new byte[chars.length / 2];
            int len = chars.length;
            for (int i = 0, j = 0; j < len; i++) {
                int f = Character.digit(chars[j], 16) << 4;
                j++;
                f = f | Character.digit(chars[j], 16);
                j++;
                bytes[i] = (byte) (f & 0xFF);
            }
            return bytes;
        }
		
		public static byte[] fromStr(String s) {
		    int len = s.length();
		    byte[] data = new byte[len / 2];
		    for (int i = 0; i < len; i += 2) {
		        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                             + Character.digit(s.charAt(i+1), 16));
		    }
		    return data;
		}

		/** Mask for bit 0 of a byte. */
		private static final int BIT_0 = 1;

		/** Mask for bit 1 of a byte. */
		private static final int BIT_1 = 0x02;

		/** Mask for bit 2 of a byte. */
		private static final int BIT_2 = 0x04;

		/** Mask for bit 3 of a byte. */
		private static final int BIT_3 = 0x08;

		/** Mask for bit 4 of a byte. */
		private static final int BIT_4 = 0x10;

		/** Mask for bit 5 of a byte. */
		private static final int BIT_5 = 0x20;

		/** Mask for bit 6 of a byte. */
		private static final int BIT_6 = 0x40;

		/** Mask for bit 7 of a byte. */
		private static final int BIT_7 = 0x80;

		private static final int[] BITS = {BIT_0, BIT_1, BIT_2, BIT_3, BIT_4, BIT_5, BIT_6, BIT_7};

		public static char[] toBinChars(final byte[] raw) {
			final char[] l_ascii = new char[raw.length << 3];
			for (int ii = 0, jj = l_ascii.length - 1; ii < raw.length; ii++, jj -= 8) {
				for (int bits = 0; bits < BITS.length; ++bits) {
					if ((raw[ii] & BITS[bits]) == 0) {
						l_ascii[jj - bits] = '0';
					} else {
						l_ascii[jj - bits] = '1';
					}
				}
			}
			return l_ascii;
		}
		public static String toBinStr(final byte[] raw) {
			return new String(toBinChars(raw));
		}

		public static byte[] fromBinStr(String s) {
			return fromBinChars(s.toCharArray());
		}
		public static byte[] fromBinChars(final char[] ascii) {
			final byte[] l_raw = new byte[ascii.length >> 3];
			for (int ii = 0, jj = ascii.length - 1; ii < l_raw.length; ii++, jj -= 8) {
				for (int bits = 0; bits < BITS.length; ++bits) {
					if (ascii[jj - bits] == '1') {
						l_raw[ii] |= BITS[bits];
					}
				}
			}
			return l_raw;
		}
	}
	
	public static class base64 {
	     /**
         * Encodes all bytes from the specified byte array into a newly-allocated
         * byte array using the {@link Base64} encoding scheme. The returned byte
         * array is of the length of the resulting bytes.
         *
         * @param   src
         *          the byte array to encode
         * @return  A newly-allocated byte array containing the resulting
         *          encoded bytes.
         */
        public static byte[] encode(byte[] src) {
        	return Base64.getEncoder().encode(src);
        }

        /**
         * Decodes all bytes from the input byte array using the {@link Base64}
         * encoding scheme, writing the results into a newly-allocated output
         * byte array. The returned byte array is of the length of the resulting
         * bytes.
         *
         * @param   src
         *          the byte array to decode
         *
         * @return  A newly-allocated byte array containing the decoded bytes.
         *
         * @throws  IllegalArgumentException
         *          if {@code src} is not in valid Base64 scheme
         */
        public static byte[] decode(byte[] src) {
        	return Base64.getDecoder().decode(src);
        }
        
        /**
         * Encodes the specified byte array into a String using the {@link Base64}
         * encoding scheme.
         *
         * <p> This method first encodes all input bytes into a base64 encoded
         * byte array and then constructs a new String by using the encoded byte
         * array and the {@link java.nio.charset.StandardCharsets#ISO_8859_1
         * ISO-8859-1} charset.
         *
         * <p> In other words, an invocation of this method has exactly the same
         * effect as invoking
         * {@code new String(encode(src), StandardCharsets.ISO_8859_1)}.
         *
         * @param   src
         *          the byte array to encode
         * @return  A String containing the resulting Base64 encoded characters
         */
        public static String encodeToString(byte[] src) {
        	return Base64.getEncoder().encodeToString(src);
        }
        
        /**
         * Decodes a Base64 encoded String into a newly-allocated byte array
         * using the {@link Base64} encoding scheme.
         *
         * <p> An invocation of this method has exactly the same effect as invoking
         * {@code decode(src.getBytes(StandardCharsets.ISO_8859_1))}
         *
         * @param   src
         *          the string to decode
         *
         * @return  A newly-allocated byte array containing the decoded bytes.
         *
         * @throws  IllegalArgumentException
         *          if {@code src} is not in valid Base64 scheme
         */
        public static byte[] decode(String src) {
        	return Base64.getDecoder().decode(src);
        }
	}
}
