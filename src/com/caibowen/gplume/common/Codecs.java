/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.common;

public class Codecs {
	
	public static final class Hex {

		private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
		
		public static char[] toChars(byte[] bytes) {
			
		    char[] hexChars = new char[bytes.length * 2];
		    for ( int j = 0; j < bytes.length; j++ ) {
		        int v = bytes[j] & 0xFF;
		        hexChars[j * 2] = HEX_CHARS[v >>> 4];
		        hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
		    }
		    return hexChars;
		}
		
		public static byte[] fromChars(char[] chars) {
			byte[] bytes = new byte[chars.length / 2];
			for (int i = 0; i < chars.length; i += 2) {
				bytes[i/2] = (byte) ((Character.digit(chars[i], 16) << 4)
                        + Character.digit(chars[i], 16));
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
	}
}
