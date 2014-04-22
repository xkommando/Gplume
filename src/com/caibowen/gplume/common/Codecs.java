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
