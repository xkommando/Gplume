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
 * 
 * Really fast random numbers generator if you do not care about security at all!
 * 
 * @author BowenCai
 *
 */
public final class FastRand {
	
	/**
	 * 
	 * @return may be negative
	 */
	public static int nextInt() {
		return next(32);
	}

	/**
	 * @return may be negative
	 */
	public static float nextFloat() {
		return nextInt();
	}
	
	/**
	 * 
	 * @return may be negative
	 */
	public static long nextLong() {
		int i = next(32);
		int j = next(32);
		return (long)i << 32 | j & 0xFFFFFFFFL;
	}

	/**
	 * 
	 * @return may be negative
	 */
	public static double nextDouble() {
		return nextLong();
	}
	
	
	private final static int UPPER_MASK = 0x80000000;
	private final static int LOWER_MASK = 0x7fffffff;

	private final static int N = 624;
	private final static int M = 397;
	private final static int MAGIC[] = { 0x0, 0x9908b0df };
	private final static int MAGIC_FACTOR1 = 1812433253;
	private final static int MAGIC_MASK1   = 0x9d2c5680;
	private final static int MAGIC_MASK2   = 0xefc60000;
	// Internal state
	private static volatile int[] bed;
	private static volatile int cur;
	static {
		bed = new int[N];
		bed[0] = (int) System.currentTimeMillis();
		for (cur = 1; cur < N; cur++) {
			bed[cur] = (MAGIC_FACTOR1 * (bed[cur-1] ^ (bed[cur-1] >>> 30)) + cur);
		}
	}
	private static int next(int bits) {
		int y;
		synchronized (bed) {
			if (cur >= N) {
				refresh();
				cur = 0;
			}
			y = bed[cur++];
		}
		// Tempering
		y ^= (y >>> 11);
		y ^= (y << 7) & MAGIC_MASK1;
		y ^= (y << 15) & MAGIC_MASK2;
		y ^= (y >>> 18);
		return (y >>> (32-bits));
	}
	
	private static void refresh() {
		
		int y, kk;
		for (kk = 0; kk < N-M; kk++) {
			y = (bed[kk] & UPPER_MASK) | (bed[kk+1] & LOWER_MASK);
			bed[kk] = bed[kk+M] ^ (y >>> 1) ^ MAGIC[y & 0x1];
		}
		for (;kk < N-1; kk++) {
			y = (bed[kk] & UPPER_MASK) | (bed[kk+1] & LOWER_MASK);
			bed[kk] = bed[kk+(M-N)] ^ (y >>> 1) ^ MAGIC[y & 0x1];
		}
		y = (bed[N-1] & UPPER_MASK) | (bed[0] & LOWER_MASK);
		bed[N-1] = bed[M-1] ^ (y >>> 1) ^ MAGIC[y & 0x1];
		cur = 0;
	}
}
