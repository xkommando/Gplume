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
package com.caibowen.gplume.misc;

public class Search {
	
	public static class BoyerMoore {
		
		private static final int[] BASE;
		static {
			BASE = new int[256];
			for (int i = 0; i < 256; i++) {
				BASE[i] = -1;
			}
		}
		
		private int patternLen;
	    private int[] occ;     // the bad-character skip array

	    private CharSequence pattern;  // store the pattern as a character array
	    BoyerMoore(CharSequence p) {
			occ = new int[256];
			System.arraycopy(BASE, 0, occ, 0, 256);
			pattern = p;
			patternLen = pattern.length();
			for (int i = 0; i < pattern.length(); i++) {
				occ[pattern.charAt(i)] = i;
			}
		}
	    
	    public static BoyerMoore search(CharSequence p) {
	    	return new BoyerMoore(p);
	    }
	    public int from(CharSequence text) {
	    	final int length = text.length();
	        int skip = 0;
	        for (int i = 0; i <= length - patternLen; i += skip) {
	            skip = 0;
	            for (int j = patternLen-1; j >= 0; j--) {
	                if (pattern.charAt(j) != text.charAt(i+j)) {
	                    skip = Math.max(1, j - occ[text.charAt(i+j)]);
	                    break;
	                }
	            }
	            if (skip == 0) return i;    // found
	        }
	    	return -1;
	    }
	}

}
