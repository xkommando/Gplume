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
package com.caibowen.gplume.misc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Some functions are adopted from apache.commons.lang3
 * 
 * @author BowenCai
 *
 */
public class Str {

	public static final String SPACE = " ";

    public static final String EMPTY = "";
    
	public static class Patterns{

		public static final Pattern POST_URI_TITLE = Pattern.compile(
				"^[A-Za-z0-9_.-]{3,200}$");

		public static final Pattern ENTITY_FIELD_NAME = Pattern.compile(
				"^[A-Za-z][A-Za-z0-9_]{3,500}$");
	
		public static final Pattern URL = Pattern.compile(
				"^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
				Pattern.CASE_INSENSITIVE);

		public static final Pattern ASCII = Pattern.compile("\\A\\p{ASCII}*\\z");
		
		/**
		 * mapping URL must from the root(leading with '/')
		 */
		public static final Pattern MAPPING_URI = Pattern.compile(
				"^([\\w\\-\\./_=]){0,64}\\*?$", Pattern.CASE_INSENSITIVE);

		public static final Pattern COOKIE_NAME = Pattern.compile(
			"^(([\\w\\-]+|\\{([a-zA-Z][\\w]*)\\})(;*)/?)+(\\.\\w+$)?|^/$");
		
		/**
		 * from Struts2
		 */
		public static final Pattern EMAIL = Pattern.compile(
				"\\b^['_a-z0-9-\\+]+(\\.['_a-z0-9-\\+]+)*"
+"@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.([a-z]{2}|aero|arpa|asia|biz|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|id|nato|net|org|pro|tel|travel|xxx)$\\b"
		);

//		public static void main(String...a) {
//			System.out.println(EMAIL.matcher("hzzyxxx@163.com").matches());
//		}
		public static final Pattern INTERGER = Pattern.compile("(-?[0-9]*)");
		
		public static final Pattern FLOAT_NUMBER = Pattern.compile(
				"(([1-9][0-9]*\\.?[0-9]*)|(\\.[0-9]+))([Ee][+-]?[0-9]+)?");

		private Patterns(){}
	}

//	public static void main(String[] args) {
//		System.out.println(Patterns.INTERGER.matcher(null).matches());
//	}
	public static class Math {
		
		private static int max(int a, int b) {
			return a > b ? a : b;
		}
		private static int min(int a, int b) {
			return a > b ? a : b;
		}
	    private static String getSetOfMatchingCharacterWithin(final CharSequence first, final CharSequence second, final int limit) {
	        final StringBuilder common = new StringBuilder();
	        final StringBuilder copy = new StringBuilder(second);

	        for (int i = 0; i < first.length(); i++) {
	            final char ch = first.charAt(i);
	            boolean found = false;
	            // See if the character is within the limit positions away from the original position of that character.
	            for (int j = max(0, i - limit); 
	            		!found && j < min(i + limit, second.length());
	            		j++) {
	                if (copy.charAt(j) == ch) {
	                    found = true;
	                    common.append(ch);
	                    copy.setCharAt(j,'*');
	                }
	            }
	        }
	        return common.toString();
	    }


		// Difference
		// -----------------------------------------------------------------------
		/**
		 * <p>
		 * Compares two Strings, and returns the portion where they differ. More
		 * precisely, return the remainder of the second String, starting from
		 * where it's different from the first. This means that the difference
		 * between "abc" and "ab" is the empty String and not "c".
		 * </p>
		 *
		 * <p>
		 * For example,
		 * {@code difference("i am a machine", "i am a robot") -> "robot"}.
		 * </p>
		 *
		 * <pre>
		 * StringUtils.difference(null, null) = null
		 * StringUtils.difference("", "") = ""
		 * StringUtils.difference("", "abc") = "abc"
		 * StringUtils.difference("abc", "") = ""
		 * StringUtils.difference("abc", "abc") = ""
		 * StringUtils.difference("abc", "ab") = ""
		 * StringUtils.difference("ab", "abxyz") = "xyz"
		 * StringUtils.difference("abcde", "abxyz") = "xyz"
		 * StringUtils.difference("abcde", "xyz") = "xyz"
		 * </pre>
		 *
		 * @param str1
		 *            the first String, may be null
		 * @param str2
		 *            the second String, may be null
		 * @return the portion of str2 where it differs from str1; returns the
		 *         empty String if they are equal
		 * @see #indexOfDifference(CharSequence,CharSequence)
		 * @since 2.0
		 */
		public static String difference(final String str1, final String str2) {
			if (str1 == null) {
				return str2;
			}
			if (str2 == null) {
				return str1;
			}
			final int at = indexOfDifference(str1, str2);
			if (at == -1) {
				return EMPTY;
			}
			return str2.substring(at);
		}
		
	    /**
	     * <p>Find the Levenshtein distance between two Strings if it's less than or equal to a given
	     * threshold.</p>
	     *
	     * <p>This is the number of changes needed to change one String into
	     * another, where each change is a single character modification (deletion,
	     * insertion or substitution).</p>
	     *
	     * <p>This implementation follows from Algorithms on Strings, Trees and Sequences by Dan Gusfield
	     * and Chas Emerick's implementation of the Levenshtein distance algorithm from
	     * <a href="http://www.merriampark.com/ld.htm">http://www.merriampark.com/ld.htm</a></p>
	     *
	     * <pre>
	     * StringUtils.getLevenshteinDistance(null, *, *)             = IllegalArgumentException
	     * StringUtils.getLevenshteinDistance(*, null, *)             = IllegalArgumentException
	     * StringUtils.getLevenshteinDistance(*, *, -1)               = IllegalArgumentException
	     * StringUtils.getLevenshteinDistance("","", 0)               = 0
	     * StringUtils.getLevenshteinDistance("aaapppp", "", 8)       = 7
	     * StringUtils.getLevenshteinDistance("aaapppp", "", 7)       = 7
	     * StringUtils.getLevenshteinDistance("aaapppp", "", 6))      = -1
	     * StringUtils.getLevenshteinDistance("elephant", "hippo", 7) = 7
	     * StringUtils.getLevenshteinDistance("elephant", "hippo", 6) = -1
	     * StringUtils.getLevenshteinDistance("hippo", "elephant", 7) = 7
	     * StringUtils.getLevenshteinDistance("hippo", "elephant", 6) = -1
	     * </pre>
	     *
	     * @param s  the first String, must not be null
	     * @param t  the second String, must not be null
	     * @param threshold the target threshold, must not be negative
	     * @return result distance, or {@code -1} if the distance would be greater than the threshold
	     * @throws IllegalArgumentException if either String input {@code null} or negative threshold
	     */
	    public static int getLevenshteinDistance(CharSequence s, CharSequence t, final int threshold) {
	        if (s == null || t == null) {
	            throw new IllegalArgumentException("Strings must not be null");
	        }
	        if (threshold < 0) {
	            throw new IllegalArgumentException("Threshold must not be negative");
	        }

	        /*
	        This implementation only computes the distance if it's less than or equal to the
	        threshold value, returning -1 if it's greater.  The advantage is performance: unbounded
	        distance is O(nm), but a bound of k allows us to reduce it to O(km) time by only
	        computing a diagonal stripe of width 2k + 1 of the cost table.
	        It is also possible to use this to compute the unbounded Levenshtein distance by starting
	        the threshold at 1 and doubling each time until the distance is found; this is O(dm), where
	        d is the distance.

	        One subtlety comes from needing to ignore entries on the border of our stripe
	        eg.
	        p[] = |#|#|#|*
	        d[] =  *|#|#|#|
	        We must ignore the entry to the left of the leftmost member
	        We must ignore the entry above the rightmost member

	        Another subtlety comes from our stripe running off the matrix if the strings aren't
	        of the same size.  Since string s is always swapped to be the shorter of the two,
	        the stripe will always run off to the upper right instead of the lower left of the matrix.

	        As a concrete example, suppose s is of length 5, t is of length 7, and our threshold is 1.
	        In this case we're going to walk a stripe of length 3.  The matrix would look like so:

	           1 2 3 4 5
	        1 |#|#| | | |
	        2 |#|#|#| | |
	        3 | |#|#|#| |
	        4 | | |#|#|#|
	        5 | | | |#|#|
	        6 | | | | |#|
	        7 | | | | | |

	        Note how the stripe leads off the table as there is no possible way to turn a string of length 5
	        into one of length 7 in edit distance of 1.

	        Additionally, this implementation decreases memory usage by using two
	        single-dimensional arrays and swapping them back and forth instead of allocating
	        an entire n by m matrix.  This requires a few minor changes, such as immediately returning
	        when it's detected that the stripe has run off the matrix and initially filling the arrays with
	        large values so that entries we don't compute are ignored.

	        See Algorithms on Strings, Trees and Sequences by Dan Gusfield for some discussion.
	         */

	        int n = s.length(); // length of s
	        int m = t.length(); // length of t

	        // if one string is empty, the edit distance is necessarily the length of the other
	        if (n == 0) {
	            return m <= threshold ? m : -1;
	        } else if (m == 0) {
	            return n <= threshold ? n : -1;
	        }

	        if (n > m) {
	            // swap the two strings to consume less memory
	            final CharSequence tmp = s;
	            s = t;
	            t = tmp;
	            n = m;
	            m = t.length();
	        }

	        int p[] = new int[n + 1]; // 'previous' cost array, horizontally
	        int d[] = new int[n + 1]; // cost array, horizontally
	        int _d[]; // placeholder to assist in swapping p and d

	        // fill in starting table values
	        final int boundary = Math.min(n, threshold) + 1;
	        for (int i = 0; i < boundary; i++) {
	            p[i] = i;
	        }
	        // these fills ensure that the value above the rightmost entry of our
	        // stripe will be ignored in following loop iterations
	        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
	        Arrays.fill(d, Integer.MAX_VALUE);

	        // iterates through t
	        for (int j = 1; j <= m; j++) {
	            final char t_j = t.charAt(j - 1); // jth character of t
	            d[0] = j;

	            // compute stripe indices, constrain to array size
	            final int min = Math.max(1, j - threshold);
	            final int max = (j > Integer.MAX_VALUE - threshold) ? n : Math.min(n, j + threshold);

	            // the stripe may lead off of the table if s and t are of different sizes
	            if (min > max) {
	                return -1;
	            }

	            // ignore entry left of leftmost
	            if (min > 1) {
	                d[min - 1] = Integer.MAX_VALUE;
	            }

	            // iterates through [min, max] in s
	            for (int i = min; i <= max; i++) {
	                if (s.charAt(i - 1) == t_j) {
	                    // diagonally left and up
	                    d[i] = p[i - 1];
	                } else {
	                    // 1 + minimum of cell to the left, to the top, diagonally left and up
	                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
	                }
	            }

	            // copy current distance counts to 'previous row' distance counts
	            _d = p;
	            p = d;
	            d = _d;
	        }

	        // if p[n] is greater than the threshold, there's no guarantee on it being the correct
	        // distance
	        if (p[n] <= threshold) {
	            return p[n];
	        }
	        return -1;
	    }
	    
	    /**
	     * <p>Find the Jaro Winkler Distance which indicates the similarity score between two Strings.</p>
	     *
	     * <p>The Jaro measure is the weighted sum of percentage of matched characters from each file and transposed characters. 
	     * Winkler increased this measure for matching initial characters.</p>
	     *
	     * <p>This implementation is based on the Jaro Winkler similarity algorithm
	     * from <a href="http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>.</p>
	     * 
	     * <pre>
	     * StringUtils.getJaroWinklerDistance(null, null)          = IllegalArgumentException
	     * StringUtils.getJaroWinklerDistance("","")               = 0.0
	     * StringUtils.getJaroWinklerDistance("","a")              = 0.0
	     * StringUtils.getJaroWinklerDistance("aaapppp", "")       = 0.0
	     * StringUtils.getJaroWinklerDistance("frog", "fog")       = 0.93
	     * StringUtils.getJaroWinklerDistance("fly", "ant")        = 0.0
	     * StringUtils.getJaroWinklerDistance("elephant", "hippo") = 0.44
	     * StringUtils.getJaroWinklerDistance("hippo", "elephant") = 0.44
	     * StringUtils.getJaroWinklerDistance("hippo", "zzzzzzzz") = 0.0
	     * StringUtils.getJaroWinklerDistance("hello", "hallo")    = 0.88
	     * StringUtils.getJaroWinklerDistance("ABC Corporation", "ABC Corp") = 0.91
	     * StringUtils.getJaroWinklerDistance("D N H Enterprises Inc", "D &amp; H Enterprises, Inc.") = 0.93
	     * StringUtils.getJaroWinklerDistance("My Gym Children's Fitness Center", "My Gym. Childrens Fitness") = 0.94
	     * StringUtils.getJaroWinklerDistance("PENNSYLVANIA", "PENNCISYLVNIA")    = 0.9
	     * </pre>
	     *
	     * @param first the first String, must not be null
	     * @param second the second String, must not be null
	     * @return result distance
	     * @throws IllegalArgumentException if either String input {@code null}
	     * @since 3.3
	     */
	    public static double getJaroWinklerDistance(final CharSequence first, final CharSequence second) {
	        final double DEFAULT_SCALING_FACTOR = 0.1;

	        if (first == null || second == null) {
	            throw new IllegalArgumentException("Strings must not be null");
	        }

	        final double jaro = score(first,second);
	        final int cl = commonPrefixLength(first, second);
	        final double matchScore = java.lang.Math.round((jaro + (DEFAULT_SCALING_FACTOR * cl * (1.0 - jaro))) *100.0)/100.0;

	        return  matchScore;
	    }
	    /**
	     * Calculates the number of transposition between two strings.
	     * @param first The first string.
	     * @param second The second string.
	     * @return The number of transposition between the two strings.
	     */
	    private static int transpositions(CharSequence first, CharSequence second) {
	        int transpositions = 0;
	        for (int i = 0; i < first.length(); i++) {
	            if (first.charAt(i) != second.charAt(i)) {
	                transpositions++;
	            }
	        }
	        return transpositions / 2;
	    }
	    
	    
	    /**
	     * <p>Compares two CharSequences, and returns the index at which the
	     * CharSequences begin to differ.</p>
	     *
	     * <p>For example,
	     * {@code indexOfDifference("i am a machine", "i am a robot") -> 7}</p>
	     *
	     * <pre>
	     * StringUtils.indexOfDifference(null, null) = -1
	     * StringUtils.indexOfDifference("", "") = -1
	     * StringUtils.indexOfDifference("", "abc") = 0
	     * StringUtils.indexOfDifference("abc", "") = 0
	     * StringUtils.indexOfDifference("abc", "abc") = -1
	     * StringUtils.indexOfDifference("ab", "abxyz") = 2
	     * StringUtils.indexOfDifference("abcde", "abxyz") = 2
	     * StringUtils.indexOfDifference("abcde", "xyz") = 0
	     * </pre>
	     *
	     * @param cs1  the first CharSequence, may be null
	     * @param cs2  the second CharSequence, may be null
	     * @return the index where cs1 and cs2 begin to differ; -1 if they are equal
	     * @since 2.0
	     * @since 3.0 Changed signature from indexOfDifference(String, String) to
	     * indexOfDifference(CharSequence, CharSequence)
	     */
	    public static int indexOfDifference(final CharSequence cs1, final CharSequence cs2) {
	        if (cs1 == cs2) {
	            return -1;
	        }
	        if (cs1 == null || cs2 == null) {
	            return 0;
	        }
	        int i;
	        for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
	            if (cs1.charAt(i) != cs2.charAt(i)) {
	                break;
	            }
	        }
	        if (i < cs2.length() || i < cs1.length()) {
	            return i;
	        }
	        return -1;
	    }

	    /**
	     * <p>Compares all CharSequences in an array and returns the index at which the
	     * CharSequences begin to differ.</p>
	     *
	     * <p>For example,
	     * <code>indexOfDifference(new String[] {"i am a machine", "i am a robot"}) -&gt; 7</code></p>
	     *
	     * <pre>
	     * StringUtils.indexOfDifference(null) = -1
	     * StringUtils.indexOfDifference(new String[] {}) = -1
	     * StringUtils.indexOfDifference(new String[] {"abc"}) = -1
	     * StringUtils.indexOfDifference(new String[] {null, null}) = -1
	     * StringUtils.indexOfDifference(new String[] {"", ""}) = -1
	     * StringUtils.indexOfDifference(new String[] {"", null}) = 0
	     * StringUtils.indexOfDifference(new String[] {"abc", null, null}) = 0
	     * StringUtils.indexOfDifference(new String[] {null, null, "abc"}) = 0
	     * StringUtils.indexOfDifference(new String[] {"", "abc"}) = 0
	     * StringUtils.indexOfDifference(new String[] {"abc", ""}) = 0
	     * StringUtils.indexOfDifference(new String[] {"abc", "abc"}) = -1
	     * StringUtils.indexOfDifference(new String[] {"abc", "a"}) = 1
	     * StringUtils.indexOfDifference(new String[] {"ab", "abxyz"}) = 2
	     * StringUtils.indexOfDifference(new String[] {"abcde", "abxyz"}) = 2
	     * StringUtils.indexOfDifference(new String[] {"abcde", "xyz"}) = 0
	     * StringUtils.indexOfDifference(new String[] {"xyz", "abcde"}) = 0
	     * StringUtils.indexOfDifference(new String[] {"i am a machine", "i am a robot"}) = 7
	     * </pre>
	     *
	     * @param css  array of CharSequences, entries may be null
	     * @return the index where the strings begin to differ; -1 if they are all equal
	     * @since 2.4
	     * @since 3.0 Changed signature from indexOfDifference(String...) to indexOfDifference(CharSequence...)
	     */
	    public static int indexOfDifference(final CharSequence... css) {
	        if (css == null || css.length <= 1) {
	            return -1;
	        }
	        boolean anyStringNull = false;
	        boolean allStringsNull = true;
	        final int arrayLen = css.length;
	        int shortestStrLen = Integer.MAX_VALUE;
	        int longestStrLen = 0;

	        // find the min and max string lengths; this avoids checking to make
	        // sure we are not exceeding the length of the string each time through
	        // the bottom loop.
	        for (int i = 0; i < arrayLen; i++) {
	            if (css[i] == null) {
	                anyStringNull = true;
	                shortestStrLen = 0;
	            } else {
	                allStringsNull = false;
	                shortestStrLen = Math.min(css[i].length(), shortestStrLen);
	                longestStrLen = Math.max(css[i].length(), longestStrLen);
	            }
	        }

	        // handle lists containing all nulls or all empty strings
	        if (allStringsNull || longestStrLen == 0 && !anyStringNull) {
	            return -1;
	        }

	        // handle lists containing some nulls or some empty strings
	        if (shortestStrLen == 0) {
	            return 0;
	        }

	        // find the position with the first difference across all strings
	        int firstDiff = -1;
	        for (int stringPos = 0; stringPos < shortestStrLen; stringPos++) {
	            final char comparisonChar = css[0].charAt(stringPos);
	            for (int arrayPos = 1; arrayPos < arrayLen; arrayPos++) {
	                if (css[arrayPos].charAt(stringPos) != comparisonChar) {
	                    firstDiff = stringPos;
	                    break;
	                }
	            }
	            if (firstDiff != -1) {
	                break;
	            }
	        }

	        if (firstDiff == -1 && shortestStrLen != longestStrLen) {
	            // we compared all of the characters up to the length of the
	            // shortest string and didn't find a match, but the string lengths
	            // vary, so return the length of the shortest string.
	            return shortestStrLen;
	        }
	        return firstDiff;
	    }


	    /**
	     * <p>Compares all Strings in an array and returns the initial sequence of
	     * characters that is common to all of them.</p>
	     *
	     * <p>For example,
	     * <code>getCommonPrefix(new String[] {"i am a machine", "i am a robot"}) -&gt; "i am a "</code></p>
	     *
	     * <pre>
	     * StringUtils.getCommonPrefix(null) = ""
	     * StringUtils.getCommonPrefix(new String[] {}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"abc"}) = "abc"
	     * StringUtils.getCommonPrefix(new String[] {null, null}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"", ""}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"", null}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"abc", null, null}) = ""
	     * StringUtils.getCommonPrefix(new String[] {null, null, "abc"}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"", "abc"}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"abc", ""}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"abc", "abc"}) = "abc"
	     * StringUtils.getCommonPrefix(new String[] {"abc", "a"}) = "a"
	     * StringUtils.getCommonPrefix(new String[] {"ab", "abxyz"}) = "ab"
	     * StringUtils.getCommonPrefix(new String[] {"abcde", "abxyz"}) = "ab"
	     * StringUtils.getCommonPrefix(new String[] {"abcde", "xyz"}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"xyz", "abcde"}) = ""
	     * StringUtils.getCommonPrefix(new String[] {"i am a machine", "i am a robot"}) = "i am a "
	     * </pre>
	     *
	     * @param strs  array of String objects, entries may be null
	     * @return the initial sequence of characters that are common to all Strings
	     * in the array; empty String if the array is null, the elements are all null
	     * or if there is no common prefix.
	     * @since 2.4
	     */
	    public static String getCommonPrefix(final String... strs) {
	        if (strs == null || strs.length == 0) {
	            return EMPTY;
	        }
	        final int smallestIndexOfDiff = indexOfDifference(strs);
	        if (smallestIndexOfDiff == -1) {
	            // all strings were identical
	            if (strs[0] == null) {
	                return EMPTY;
	            }
	            return strs[0];
	        } else if (smallestIndexOfDiff == 0) {
	            // there were no common initial characters
	            return EMPTY;
	        } else {
	            // we found a common initial character sequence
	            return strs[0].substring(0, smallestIndexOfDiff);
	        }
	    }
	    
	    /**
	     * Calculates the number of characters from the beginning of the strings that match exactly one-to-one, 
	     * up to a maximum of four (4) characters.
	     * @param first The first string.
	     * @param second The second string.
	     * @return A number between 0 and 4.
	     */
	    private static int commonPrefixLength(CharSequence first, CharSequence second) {
	        final int result = getCommonPrefix(first.toString(), second.toString()).length();

	        // Limit the result to 4.
	        return result > 4 ? 4 : result;
	    }
	    
	    /**
	     * This method returns the Jaro-Winkler score for string matching.
	     * @param first the first string to be matched
	     * @param second the second string to be machted
	     * @return matching score without scaling factor impact
	     */
	    private static double score(final CharSequence first, final CharSequence second) {
	        String shorter;
	        String longer;

	        // Determine which String is longer.
	        if (first.length() > second.length()) {
	            longer = first.toString().toLowerCase();
	            shorter = second.toString().toLowerCase();
	        } else {
	            longer = second.toString().toLowerCase();
	            shorter = first.toString().toLowerCase();
	        }

	        // Calculate the half length() distance of the shorter String.
	        final int halflength = (shorter.length() / 2) + 1;

	        // Find the set of matching characters between the shorter and longer strings. Note that
	        // the set of matching characters may be different depending on the order of the strings.
	        final String m1 = getSetOfMatchingCharacterWithin(shorter, longer, halflength);
	        final String m2 = getSetOfMatchingCharacterWithin(longer, shorter, halflength);

	        // If one or both of the sets of common characters is empty, then
	        // there is no similarity between the two strings.
	        if (m1.length() == 0 || m2.length() == 0) {
	            return 0.0;
	        }

	        // If the set of common characters is not the same size, then
	        // there is no similarity between the two strings, either.
	        if (m1.length() != m2.length()) {
	            return 0.0;
	        }

	        // Calculate the number of transposition between the two sets
	        // of common characters.
	        final int transpositions = transpositions(m1, m2);

	        // Calculate the distance.
	        final double dist =
	                (m1.length() / ((double)shorter.length()) +
	                        m2.length() / ((double)longer.length()) +
	                        (m1.length() - transpositions) / ((double)m1.length())) / 3.0;
	        return dist;
	    }

	    /**
	     * <p>Checks whether the String a valid Java number.</p>
	     *
	     * <p>Valid numbers include hexadecimal marked with the <code>0x</code>
	     * qualifier, scientific notation and numbers marked with a type
	     * qualifier (e.g. 123L).</p>
	     *
	     * <p><code>Null</code> and empty String will return
	     * <code>false</code>.</p>
	     *
	     * @param str  the <code>String</code> to check
	     * @return <code>true</code> if the string is a correctly formatted number
	     * @since 3.3 the code supports hex {@code 0Xhhh} and octal {@code 0ddd} validation
	     */
	    public static boolean isNumber(final String str) {
	        if (!Utils.notBlank(str)) {
	            return false;
	        }
	        final char[] chars = str.toCharArray();
	        int sz = chars.length;
	        boolean hasExp = false;
	        boolean hasDecPoint = false;
	        boolean allowSigns = false;
	        boolean foundDigit = false;
	        // deal with any possible sign up front
	        final int start = (chars[0] == '-') ? 1 : 0;
	        if (sz > start + 1 && chars[start] == '0') { // leading 0
	            if (
	                 (chars[start + 1] == 'x') || 
	                 (chars[start + 1] == 'X') 
	            ) { // leading 0x/0X
	                int i = start + 2;
	                if (i == sz) {
	                    return false; // str == "0x"
	                }
	                // checking hex (it can't be anything else)
	                for (; i < chars.length; i++) {
	                    if ((chars[i] < '0' || chars[i] > '9')
	                        && (chars[i] < 'a' || chars[i] > 'f')
	                        && (chars[i] < 'A' || chars[i] > 'F')) {
	                        return false;
	                    }
	                }
	                return true;
	           } else if (Character.isDigit(chars[start + 1])) {
	               // leading 0, but not hex, must be octal
	               int i = start + 1;
	               for (; i < chars.length; i++) {
	                   if (chars[i] < '0' || chars[i] > '7') {
	                       return false;
	                   }
	               }
	               return true;               
	           }
	        }
	        sz--; // don't want to loop to the last char, check it afterwords
	              // for type qualifiers
	        int i = start;
	        // loop to the next to last char or to the last char if we need another digit to
	        // make a valid number (e.g. chars[0..5] = "1234E")
	        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
	            if (chars[i] >= '0' && chars[i] <= '9') {
	                foundDigit = true;
	                allowSigns = false;

	            } else if (chars[i] == '.') {
	                if (hasDecPoint || hasExp) {
	                    // two decimal points or dec in exponent   
	                    return false;
	                }
	                hasDecPoint = true;
	            } else if (chars[i] == 'e' || chars[i] == 'E') {
	                // we've already taken care of hex.
	                if (hasExp) {
	                    // two E's
	                    return false;
	                }
	                if (!foundDigit) {
	                    return false;
	                }
	                hasExp = true;
	                allowSigns = true;
	            } else if (chars[i] == '+' || chars[i] == '-') {
	                if (!allowSigns) {
	                    return false;
	                }
	                allowSigns = false;
	                foundDigit = false; // we need a digit after the E
	            } else {
	                return false;
	            }
	            i++;
	        }
	        if (i < chars.length) {
	            if (chars[i] >= '0' && chars[i] <= '9') {
	                // no type qualifier, OK
	                return true;
	            }
	            if (chars[i] == 'e' || chars[i] == 'E') {
	                // can't have an E at the last byte
	                return false;
	            }
	            if (chars[i] == '.') {
	                if (hasDecPoint || hasExp) {
	                    // two decimal points or dec in exponent
	                    return false;
	                }
	                // single trailing decimal point after non-exponent is ok
	                return foundDigit;
	            }
	            if (!allowSigns
	                && (chars[i] == 'd'
	                    || chars[i] == 'D'
	                    || chars[i] == 'f'
	                    || chars[i] == 'F')) {
	                return foundDigit;
	            }
	            if (chars[i] == 'l'
	                || chars[i] == 'L') {
	                // not allowing L with an exponent or decimal point
	                return foundDigit && !hasExp && !hasDecPoint;
	            }
	            // last character is illegal
	            return false;
	        }
	        // allowSigns is true iff the val ends in 'E'
	        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
	        return !allowSigns && foundDigit;
	    }
	}
	public static class Utils {

		/**
		 * null or filled with space
		 * @param s
		 * @return
		 */
		public static boolean isBlank(final CharSequence s) {
			return !notBlank(s);
		}
		/**
		 * CharSequence not null contains non-space char
		 * @param s
		 * @return s != null && s.trim().length() > 0;
		 */
		public static boolean notBlank(final CharSequence s) {
			if (s != null) {
				
				int right = s.length();
				int left = 0;
				while (left < right && s.charAt(left) <= ' ') {
					left++;
				}
				while (left < right && s.charAt(right - 1) <= ' ') {
					right--;
				}
				return left != right;
			}
			return false;
 		}
		

	    // Joining
	    //-----------------------------------------------------------------------
	    /**
	     * <p>Joins the elements of the provided array into a single String
	     * containing the provided list of elements.</p>
	     *
	     * <p>No separator is added to the joined String.
	     * Null objects or empty strings within the array are represented by
	     * empty strings.</p>
	     *
	     * <pre>
	     * StringUtils.join(null)            = null
	     * StringUtils.join([])              = ""
	     * StringUtils.join([null])          = ""
	     * StringUtils.join(["a", "b", "c"]) = "abc"
	     * StringUtils.join([null, "", "a"]) = "a"
	     * </pre>
	     *
	     * @param <T> the specific type of values to join together
	     * @param elements  the values to join together, may be null
	     * @return the joined String, {@code null} if null array input
	     * @since 2.0
	     * @since 3.0 Changed signature to use varargs
	     */
	    @SafeVarargs
		public static <T> String join(final T... elements) {
	        return join(elements, null);
	    }

	    /**
	     * <p>Joins the elements of the provided array into a single String
	     * containing the provided list of elements.</p>
	     *
	     * <p>No delimiter is added before or after the list.
	     * Null objects or empty strings within the array are represented by
	     * empty strings.</p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
	     * StringUtils.join(["a", "b", "c"], null) = "abc"
	     * StringUtils.join([null, "", "a"], ';')  = ";;a"
	     * </pre>
	     *
	     * @param array  the array of values to join together, may be null
	     * @param separator  the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 2.0
	     */
	    public static String join(final Object[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final long[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final int[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final short[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final byte[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final char[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final float[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final double[] array, final char separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }


	    /**
	     * <p>Joins the elements of the provided array into a single String
	     * containing the provided list of elements.</p>
	     *
	     * <p>No delimiter is added before or after the list.
	     * Null objects or empty strings within the array are represented by
	     * empty strings.</p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
	     * StringUtils.join(["a", "b", "c"], null) = "abc"
	     * StringUtils.join([null, "", "a"], ';')  = ";;a"
	     * </pre>
	     *
	     * @param array  the array of values to join together, may be null
	     * @param separator  the separator character to use
	     * @param startIndex the first index to start joining from.  It is
	     * an error to pass in an end index past the end of the array
	     * @param endIndex the index to stop joining from (exclusive). It is
	     * an error to pass in an end index past the end of the array
	     * @return the joined String, {@code null} if null array input
	     * @since 2.0
	     */
	    public static String join(final Object[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            if (array[i] != null) {
	                buf.append(array[i]);
	            }
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @param startIndex
	     *            the first index to start joining from. It is an error to pass in an end index past the end of the
	     *            array
	     * @param endIndex
	     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
	     *            the array
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final long[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            buf.append(array[i]);
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @param startIndex
	     *            the first index to start joining from. It is an error to pass in an end index past the end of the
	     *            array
	     * @param endIndex
	     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
	     *            the array
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final int[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            buf.append(array[i]);
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @param startIndex
	     *            the first index to start joining from. It is an error to pass in an end index past the end of the
	     *            array
	     * @param endIndex
	     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
	     *            the array
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final byte[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            buf.append(array[i]);
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @param startIndex
	     *            the first index to start joining from. It is an error to pass in an end index past the end of the
	     *            array
	     * @param endIndex
	     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
	     *            the array
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final short[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            buf.append(array[i]);
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @param startIndex
	     *            the first index to start joining from. It is an error to pass in an end index past the end of the
	     *            array
	     * @param endIndex
	     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
	     *            the array
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final char[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            buf.append(array[i]);
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @param startIndex
	     *            the first index to start joining from. It is an error to pass in an end index past the end of the
	     *            array
	     * @param endIndex
	     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
	     *            the array
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final double[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            buf.append(array[i]);
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>
	     * Joins the elements of the provided array into a single String containing the provided list of elements.
	     * </p>
	     *
	     * <p>
	     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
	     * by empty strings.
	     * </p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)               = null
	     * StringUtils.join([], *)                 = ""
	     * StringUtils.join([null], *)             = ""
	     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
	     * StringUtils.join([1, 2, 3], null) = "123"
	     * </pre>
	     *
	     * @param array
	     *            the array of values to join together, may be null
	     * @param separator
	     *            the separator character to use
	     * @param startIndex
	     *            the first index to start joining from. It is an error to pass in an end index past the end of the
	     *            array
	     * @param endIndex
	     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
	     *            the array
	     * @return the joined String, {@code null} if null array input
	     * @since 3.2
	     */
	    public static String join(final float[] array, final char separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }
	        final StringBuilder buf = new StringBuilder(noOfItems * 16);
	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            buf.append(array[i]);
	        }
	        return buf.toString();
	    }


	    /**
	     * <p>Joins the elements of the provided array into a single String
	     * containing the provided list of elements.</p>
	     *
	     * <p>No delimiter is added before or after the list.
	     * A {@code null} separator is the same as an empty String ("").
	     * Null objects or empty strings within the array are represented by
	     * empty strings.</p>
	     *
	     * <pre>
	     * StringUtils.join(null, *)                = null
	     * StringUtils.join([], *)                  = ""
	     * StringUtils.join([null], *)              = ""
	     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
	     * StringUtils.join(["a", "b", "c"], null)  = "abc"
	     * StringUtils.join(["a", "b", "c"], "")    = "abc"
	     * StringUtils.join([null, "", "a"], ',')   = ",,a"
	     * </pre>
	     *
	     * @param array  the array of values to join together, may be null
	     * @param separator  the separator character to use, null treated as ""
	     * @return the joined String, {@code null} if null array input
	     */
	    public static String join(final Object[] array, final String separator) {
	        if (array == null) {
	            return null;
	        }
	        return join(array, separator, 0, array.length);
	    }

	    /**
	     * <p>Joins the elements of the provided array into a single String
	     * containing the provided list of elements.</p>
	     *
	     * <p>No delimiter is added before or after the list.
	     * A {@code null} separator is the same as an empty String ("").
	     * Null objects or empty strings within the array are represented by
	     * empty strings.</p>
	     *
	     * <pre>
	     * StringUtils.join(null, *, *, *)                = null
	     * StringUtils.join([], *, *, *)                  = ""
	     * StringUtils.join([null], *, *, *)              = ""
	     * StringUtils.join(["a", "b", "c"], "--", 0, 3)  = "a--b--c"
	     * StringUtils.join(["a", "b", "c"], "--", 1, 3)  = "b--c"
	     * StringUtils.join(["a", "b", "c"], "--", 2, 3)  = "c"
	     * StringUtils.join(["a", "b", "c"], "--", 2, 2)  = ""
	     * StringUtils.join(["a", "b", "c"], null, 0, 3)  = "abc"
	     * StringUtils.join(["a", "b", "c"], "", 0, 3)    = "abc"
	     * StringUtils.join([null, "", "a"], ',', 0, 3)   = ",,a"
	     * </pre>
	     *
	     * @param array  the array of values to join together, may be null
	     * @param separator  the separator character to use, null treated as ""
	     * @param startIndex the first index to start joining from.
	     * @param endIndex the index to stop joining from (exclusive).
	     * @return the joined String, {@code null} if null array input; or the empty string
	     * if {@code endIndex - startIndex <= 0}. The number of joined entries is given by
	     * {@code endIndex - startIndex}
	     * @throws ArrayIndexOutOfBoundsException ife<br>
	     * {@code startIndex < 0} or <br>
	     * {@code startIndex >= array.length()} or <br>
	     * {@code endIndex < 0} or <br>
	     * {@code endIndex > array.length()}
	     */
	    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
	        if (array == null) {
	            return null;
	        }
	        if (separator == null) {
	            separator = EMPTY;
	        }

	        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
	        //           (Assuming that all Strings are roughly equally long)
	        final int noOfItems = endIndex - startIndex;
	        if (noOfItems <= 0) {
	            return EMPTY;
	        }

	        final StringBuilder buf = new StringBuilder(noOfItems * 16);

	        for (int i = startIndex; i < endIndex; i++) {
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            if (array[i] != null) {
	                buf.append(array[i]);
	            }
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>Joins the elements of the provided {@code Iterator} into
	     * a single String containing the provided elements.</p>
	     *
	     * <p>No delimiter is added before or after the list. Null objects or empty
	     * strings within the iteration are represented by empty strings.</p>
	     *
	     * <p>See the examples here: {@link #join(Object[],char)}. </p>
	     *
	     * @param iterator  the {@code Iterator} of values to join together, may be null
	     * @param separator  the separator character to use
	     * @return the joined String, {@code null} if null iterator input
	     * @since 2.0
	     */
	    public static String join(final Iterator<?> iterator, final char separator) {

	        // handle null, zero and one elements before building a buffer
	        if (iterator == null) {
	            return null;
	        }
	        if (!iterator.hasNext()) {
	            return EMPTY;
	        }
	        final Object first = iterator.next();
	        if (!iterator.hasNext()) {
	        	return first == null ? "" : first.toString();
	        }

	        // two or more elements
	        final StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
	        if (first != null) {
	            buf.append(first);
	        }

	        while (iterator.hasNext()) {
	            buf.append(separator);
	            final Object obj = iterator.next();
	            if (obj != null) {
	                buf.append(obj);
	            }
	        }

	        return buf.toString();
	    }

	    /**
	     * <p>Joins the elements of the provided {@code Iterator} into
	     * a single String containing the provided elements.</p>
	     *
	     * <p>No delimiter is added before or after the list.
	     * A {@code null} separator is the same as an empty String ("").</p>
	     *
	     * <p>See the examples here: {@link #join(Object[],String)}. </p>
	     *
	     * @param iterator  the {@code Iterator} of values to join together, may be null
	     * @param separator  the separator character to use, null treated as ""
	     * @return the joined String, {@code null} if null iterator input
	     */
	    public static String join(final Iterator<?> iterator, final String separator) {

	        // handle null, zero and one elements before building a buffer
	        if (iterator == null) {
	            return null;
	        }
	        if (!iterator.hasNext()) {
	            return EMPTY;
	        }
	        final Object first = iterator.next();
	        if (!iterator.hasNext()) {
	            return first == null ? "" : first.toString();
	        }

	        // two or more elements
	        final StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
	        if (first != null) {
	            buf.append(first);
	        }

	        while (iterator.hasNext()) {
	            if (separator != null) {
	                buf.append(separator);
	            }
	            final Object obj = iterator.next();
	            if (obj != null) {
	                buf.append(obj);
	            }
	        }
	        return buf.toString();
	    }

	    /**
	     * <p>Joins the elements of the provided {@code Iterable} into
	     * a single String containing the provided elements.</p>
	     *
	     * <p>No delimiter is added before or after the list. Null objects or empty
	     * strings within the iteration are represented by empty strings.</p>
	     *
	     * <p>See the examples here: {@link #join(Object[],char)}. </p>
	     *
	     * @param iterable  the {@code Iterable} providing the values to join together, may be null
	     * @param separator  the separator character to use
	     * @return the joined String, {@code null} if null iterator input
	     * @since 2.3
	     */
	    public static String join(@Nullable final Iterable<?> iterable, final char separator) {
	        if (iterable == null) {
	            return null;
	        }
	        return join(iterable.iterator(), separator);
	    }

	    /**
	     * <p>Joins the elements of the provided {@code Iterable} into
	     * a single String containing the provided elements.</p>
	     *
	     * <p>No delimiter is added before or after the list.
	     * A {@code null} separator is the same as an empty String ("").</p>
	     *
	     * <p>See the examples here: {@link #join(Object[],String)}. </p>
	     *
	     * @param iterable  the {@code Iterable} providing the values to join together, may be null
	     * @param separator  the separator character to use, null treated as ""
	     * @return the joined String, {@code null} if null iterator input
	     * @since 2.3
	     */
	    @Nullable
	    public static String join(@Nullable final Iterable<?> iterable, final String separator) {
	        if (iterable == null) {
	            return null;
	        }
	        return join(iterable.iterator(), separator);
	    }


	    public static @Nonnull String 
	    substrBefore(final @Nullable String str, @Nonnull final String separator) {
	    	
	        if (!notBlank(str) || separator == null) {
	            return str;
	        }
	        if (separator.isEmpty()) {
	            return EMPTY;
	        }
	        final int pos = str.indexOf(separator);
	        if (pos == -1) {
	            return str;
	        }
	        return str.substring(0, pos);
	    }

	    /**
	     * <p>Gets the substring after the first occurrence of a separator.
	     * The separator is not returned.</p>
	     *
	     * <p>A {@code null} string input will return {@code null}.
	     * An empty ("") string input will return the empty string.
	     * A {@code null} separator will return the empty string if the
	     * input string is not {@code null}.</p>
	     *
	     * <p>If nothing is found, the empty string is returned.</p>
	     *
	     * <pre>
	     * StringUtils.substringAfter(null, *)      = null
	     * StringUtils.substringAfter("", *)        = ""
	     * StringUtils.substringAfter(*, null)      = ""
	     * StringUtils.substringAfter("abc", "a")   = "bc"
	     * StringUtils.substringAfter("abcba", "b") = "cba"
	     * StringUtils.substringAfter("abc", "c")   = ""
	     * StringUtils.substringAfter("abc", "d")   = ""
	     * StringUtils.substringAfter("abc", "")    = "abc"
	     * </pre>
	     *
	     * @param str  the String to get a substring from, may be null
	     * @param separator  the String to search for, may be null
	     * @return the substring after the first occurrence of the separator,
	     *  {@code null} if null String input
	     * @since 2.0
	     */
	    public static @Nonnull String 
	    substrAfter(@Nullable final String str, @Nullable final String separator) {
	        if (!notBlank(str)) {
	            return str;
	        }
	        if (separator == null) {
	            return EMPTY;
	        }
	        final int pos = str.indexOf(separator);
	        if (pos == -1) {
	            return EMPTY;
	        }
	        return str.substring(pos + separator.length());
	    }
	    /**
	     * <p>Gets the leftmost {@code len} characters of a String.</p>
	     *
	     * <p>If {@code len} characters are not available, or the
	     * String is {@code null}, the String will be returned without
	     * an exception. An empty String is returned if len is negative.</p>
	     *
	     * <pre>
	     * StringUtils.left(null, *)    = null
	     * StringUtils.left(*, -ve)     = ""
	     * StringUtils.left("", *)      = ""
	     * StringUtils.left("abc", 0)   = ""
	     * StringUtils.left("abc", 2)   = "ab"
	     * StringUtils.left("abc", 4)   = "abc"
	     * </pre>
	     *
	     * @param str  the String to get the leftmost characters from, may be null
	     * @param len  the length of the required String
	     * @return the leftmost characters, {@code null} if null String input
	     */
	    public static String left(final String str, final int len) {
	        if (str == null) {
	            return null;
	        }
	        if (len < 0) {
	            return EMPTY;
	        }
	        if (str.length() <= len) {
	            return str;
	        }
	        return str.substring(0, len);
	    }

	    /**
	     * <p>Gets the rightmost {@code len} characters of a String.</p>
	     *
	     * <p>If {@code len} characters are not available, or the String
	     * is {@code null}, the String will be returned without an
	     * an exception. An empty String is returned if len is negative.</p>
	     *
	     * <pre>
	     * StringUtils.right(null, *)    = null
	     * StringUtils.right(*, -ve)     = ""
	     * StringUtils.right("", *)      = ""
	     * StringUtils.right("abc", 0)   = ""
	     * StringUtils.right("abc", 2)   = "bc"
	     * StringUtils.right("abc", 4)   = "abc"
	     * </pre>
	     *
	     * @param str  the String to get the rightmost characters from, may be null
	     * @param len  the length of the required String
	     * @return the rightmost characters, {@code null} if null String input
	     */
	    public static String right(final String str, final int len) {
	        if (str == null) {
	            return null;
	        }
	        if (len < 0) {
	            return EMPTY;
	        }
	        if (str.length() <= len) {
	            return str;
	        }
	        return str.substring(str.length() - len);
	    }
	    
	    public static boolean isDigits(final String str) {
	        if (!notBlank(str)) {
	            return false;
	        }
	        for (int i = 0; i < str.length(); i++) {
	            if (!Character.isDigit(str.charAt(i))) {
	                return false;
	            }
	        }
	        return true;
	    }
	    
	    /**
	     * <p>Checks if the CharSequence contains only lowercase characters.</p>
	     *
	     * <p>{@code null} will return {@code false}.
	     * An empty CharSequence (length()=0) will return {@code false}.</p>
	     *
	     * <pre>
	     * StringUtils.isAllLowerCase(null)   = false
	     * StringUtils.isAllLowerCase("")     = false
	     * StringUtils.isAllLowerCase("  ")   = false
	     * StringUtils.isAllLowerCase("abc")  = true
	     * StringUtils.isAllLowerCase("abC") = false
	     * </pre>
	     *
	     * @param cs  the CharSequence to check, may be null
	     * @return {@code true} if only contains lowercase characters, and is non-null
	     * @since 2.5
	     * @since 3.0 Changed signature from isAllLowerCase(String) to isAllLowerCase(CharSequence)
	     */
	    public static boolean isAllLowerCase(final CharSequence cs) {
	        if (cs == null || !notBlank(cs)) {
	            return false;
	        }
	        final int sz = cs.length();
	        for (int i = 0; i < sz; i++) {
	            if (Character.isLowerCase(cs.charAt(i)) == false) {
	                return false;
	            }
	        }
	        return true;
	    }
	    

	    /**
	     * <p>Checks if the CharSequence contains only uppercase characters.</p>
	     *
	     * <p>{@code null} will return {@code false}.
	     * An empty String (length()=0) will return {@code false}.</p>
	     *
	     * <pre>
	     * StringUtils.isAllUpperCase(null)   = false
	     * StringUtils.isAllUpperCase("")     = false
	     * StringUtils.isAllUpperCase("  ")   = false
	     * StringUtils.isAllUpperCase("ABC")  = true
	     * StringUtils.isAllUpperCase("aBC") = false
	     * </pre>
	     *
	     * @param cs the CharSequence to check, may be null
	     * @return {@code true} if only contains uppercase characters, and is non-null
	     * @since 2.5
	     * @since 3.0 Changed signature from isAllUpperCase(String) to isAllUpperCase(CharSequence)
	     */
	    public static boolean isAllUpperCase(final CharSequence cs) {
	        if (cs == null || !notBlank(cs)) {
	            return false;
	        }
	        final int sz = cs.length();
	        for (int i = 0; i < sz; i++) {
	            if (Character.isUpperCase(cs.charAt(i)) == false) {
	                return false;
	            }
	        }
	        return true;
	    }
	    
		public static final long ipV4ToLong(final String ipAddress) {
			long result = 0;
			String[] atoms = ipAddress.split("\\.");

			for (int i = 3; i >= 0; i--) {
				result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
			}
			return result & 0xFFFFFFFF;
		}

		public static final String longToIpV4(long ip) {
			StringBuilder sb = new StringBuilder(15);

			for (int i = 0; i < 4; i++) {
				sb.insert(0, Long.toString(ip & 0xff));

				if (i < 3) {
					sb.insert(0, '.');
				}
				ip >>= 8;
			}

			return sb.toString();
		}
	}
	
	private Str(){}

}
