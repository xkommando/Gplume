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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @author BowenCai
 *
 */
public class Sort {

	public static class heap {
	
	public static <T> void 
	heap_sort(T[] v, final int left, final int right,
				final Comparator<T> cmp) {

		make_left_heap(v, left, right, cmp);
		for (int i = right - 1; i >= left; i--) {
			T _ = v[left];
			v[left] = v[i];
			v[i] = _;
			left_heaplify(v, left, left, i, cmp);
		}
	}
	
	public static<T> void 
	make_left_heap(T[] v, final int left, final int right,
						final Comparator<T> cmp) {

		final int range = right - left;
		for (int i = range / 2 + left; i != left; i--) {
			left_heaplify(v, i - 1, left, right, cmp);
		}

	}
	/**
	 *  interval: [0, range )
	 * @param v
	 * @param idx
	 * @param range is unreachable
	 * @param cmp
	 */
	public static<T> void 
	left_heaplify(T[] v, 
				final int idx, final int left, final int right,
				final Comparator<T> cmp) {
		
		int _l = idx * 2 - left + 1;
		int _r = _l + 1;

//System.out.println("idx:" + idx+ "*"+ v[idx] + "  _l:"  + _l + "*"+ v[_l] + "   _r:" + _r + "*"+ v[_r]);
		int maxIdx = (left <= _l && _l < right
						&& (cmp.compare(v[_l], 
								v[idx]) > 0)) ? _l : idx;
		
		if (_r < right && cmp.compare(v[_r], v[maxIdx]) > 0) {
			maxIdx = _r;
		}
		
		if (maxIdx != idx) {
			T _t = v[maxIdx];
			v[maxIdx] = v[idx];
			v[idx] = _t;
			left_heaplify(v, maxIdx, left, right, cmp);
		}
	}
	
	public static <T extends Comparable<T> > void 
	heap_sort(T[] v, final int left, final int right) {

		make_left_heap(v, left, right);
		for (int i = right - 1; i >= left; i--) {
			T _ = v[left];
			v[left] = v[i];
			v[i] = _;
			left_heaplify(v, left, left, i);
		}
	}
	
	public static<T extends Comparable<T> > void 
	make_left_heap(T[] v, final int left, final int right) {

		final int range = right - left;
		for (int i = range / 2 + left; i != left; i--) {
			left_heaplify(v, i - 1, left, right);
		}

	}
	/**
	 *  interval: [0, range )
	 * @param v
	 * @param idx
	 * @param range is unreachable
	 * @param cmp
	 */
	public static<T extends Comparable<T> > void 
	left_heaplify(T[] v, 
				final int idx, final int left, final int right) {
		
		int _l = idx * 2 - left + 1;
		int _r = _l + 1;

		
		int maxIdx = (left <= _l && _l < right
				&& v[_l].compareTo(v[idx]) > 0
						) ? _l : idx;
		
		if (_r < right && v[_r].compareTo(v[maxIdx]) > 0) {
			maxIdx = _r;
		}
		
		if (maxIdx != idx) {
			T _t = v[maxIdx];
			v[maxIdx] = v[idx];
			v[idx] = _t;
			left_heaplify(v, maxIdx, left, right);
		}
	}
	
	public static <T extends Comparable<T> > void 
	heap_sort(List<T> v, final int left, final int right) {

		make_left_heap(v, left, right);
		for (int i = right - 1; i >= left; i--) {
			T _ = v.get(left);
			v.set(left, v.get(i));
			v.set(i, _);
			left_heaplify(v, left, left, i);
		}
	}
	
	public static<T extends Comparable<T> > void 
	make_left_heap(List<T> v, final int left, final int right) {

		final int range = right - left;
		for (int i = range / 2 + left; i != left; i--) {
			left_heaplify(v, i - 1, left, right);
		}

	}
	/**
	 *  interval: [0, range )
	 * @param v
	 * @param idx
	 * @param range is unreachable
	 * @param cmp
	 */
	public static<T extends Comparable<T> > void 
	left_heaplify(List<T> v, 
				final int idx, final int left, final int right) {
		
		int _l = idx * 2 - left + 1;
		int _r = _l + 1;

		
		int maxIdx = (left <= _l && _l < right
				&& v.get(_l).compareTo(v.get(idx)) > 0
						) ? _l : idx;
		
		if (_r < right && v.get(_r).compareTo(v.get(maxIdx)) > 0) {
			maxIdx = _r;
		}
		
		if (maxIdx != idx) {
			T _t = v.get(maxIdx);
			v.set(maxIdx, v.get(idx));
			v.set(idx, _t);
			left_heaplify(v, maxIdx, left, right);
		}
	}
	/**
	 * interval : [start, v.length)
	 * @param v
	 * @param start start index, reachable
	 * @param cmp
	 */
	public static<T> void 
	make_right_heap(T[] v, final int start, 
							final Comparator<T> cmp) {
		
		final int len = v.length;
		for (int i = (len + start + 2 ) / 2; i != len; i++) {
			right_heaplify(v, i, start, cmp);
			for (int j = start; j < v.length; j++) {
				System.out.print(v[j] + "  ");
			}
			System.out.println();
		}
	}
	/**
	 * interval : [start, v.length)
	 * @param v
	 * @param idx
	 * @param start start index, reachable
	 * @param cmp
	 */
	public static<T> void 
	right_heaplify(T[] v, 
				final int idx, final int start,
				final Comparator<T> cmp) {

		final int len = v.length;
		int _l =  len - (len - idx) * 2;
		int _r = _l - 1;
System.out.println(_l + "   " + _r);

		int maxIdx = (start <= _l
						&& (cmp.compare(v[_l], v[idx]) > 0)) ? _l : idx;
		
		if (start <= _r && cmp.compare(v[_r], v[maxIdx]) > 0) {
			maxIdx = _r;
		}
		
		if (maxIdx != idx) {
			T _t = v[maxIdx];
			v[maxIdx] = v[idx];
			v[idx] = _t;
			right_heaplify(v, maxIdx, start, cmp);
		}
	}

	}

//	make_left_heap(v, left, right, cmp);
//	for (int i = right - 1; i >= left; i--) {
//		T _ = v[left];
//		v[left] = v[i];
//		v[i] = _;
//		left_heaplify(v, left, left, i, cmp);
//	}
	public static <T> void 
	sort_right_part(T[] v, 
				final int left, final int right, int n, 
				final Comparator<T> cmp) {

		heap.make_left_heap(v, left, right, cmp);
		for (int i = right - 1; i >= left && n != 0; i--, n--) {
			T _ = v[left];
			v[left] = v[i];
			v[i] = _;
			heap.left_heaplify(v, left, left, i, cmp);
		}
	}
	
	/**
	 * select the biggest/smallest n elements from <code>src</code> to <code>dest</code>
	 * <br>
	 * <code>src</code> stays unchanged
	 * @param src source address
	 * @param soffset source address offset
	 * @param sright  right edge of source address interval: [soffset, sright)
	 * @param dest destination address
	 * @param doffset destination address offset
	 * @param n
	 * @param cmp
	 */
	public static<T> void
	partial_sort_copy(final T[] src, final int soffset, final int sright,
						T[] dest, final int doffset,
						final int n,
						final Comparator<T> cmp) {

		System.arraycopy(src, soffset, dest, doffset, n);
		
		final int dright = doffset + n;
		int minIdx = min_index(dest, doffset, dright, cmp);
		for (int i = n; i < sright; i++) {
			if (cmp.compare(src[i], dest[minIdx]) > 0) {
				dest[minIdx] = src[i];
				minIdx = min_index(dest, doffset, dright, cmp);
			}
		}
		Arrays.sort(dest, doffset, dright, cmp);
	}
	
	public static<T> int min_index(T[] v, final int left, final int right, Comparator<T> cmp) {
		int idx = left;
		for (int i = left + 1; i < right; i++) {
			if (cmp.compare(v[idx], v[i]) > 0) {
				idx = i;
			}
		}
		return idx;
	}
	
	public static<T> void
	partial_sort_copy(final List<T> src, final int soffset, final int sright,
						List<T> dest, final int doffset,
						final int n,
						final Comparator<T> cmp) {

		dest.clear();
		for (int i = 0; i < n; i++) {
			dest.add(src.get(i));
		}
		
		final int dright = doffset + n;
		int minIdx = min_index(dest, doffset, dright, cmp);
		for (int i = n; i < sright; i++) {
			if (cmp.compare(src.get(i), dest.get(minIdx)) > 0) {
				dest.set(minIdx, src.get(i));
				minIdx = min_index(dest, doffset, dright, cmp);
			}
		}
		Collections.sort(dest, cmp);
	}
	
	public static<T> int min_index(List<T> v, final int left, final int right, Comparator<T> cmp) {
		int idx = left;
		for (int i = left + 1; i < right; i++) {
			if (cmp.compare(v.get(idx), v.get(i)) > 0) {
				idx = i;
			}
		}
		return idx;
	}
}
//
//	public static void main(String...args) {
//		Comparator<Integer> cmp = new Comparator<Integer>() {
//			@Override
//			public int compare(Integer o1, Integer o2) {
//				return o1.compareTo(o2);
//			}
//		};
//		Comparator<Integer> cmp2 =new Comparator<Integer>() {
//
//			@Override
//			public int compare(Integer o1, Integer o2) {
//				return o2.compareTo(o1);
//			}
//		};
//		Integer[] v = new Integer[16];
//		Integer[] v2 = new Integer[6];
//		for (int i = 0; i < v.length; i++) {
//			v[i] = new Integer(v.length - i);
//		}
//		for (int i = 0; i < v2.length; i++) {
//			v2[i] = i;
//		}
//		
//		List<Integer> ls = Arrays.asList(v);
//		Collections.shuffle(ls);
//		v = (Integer[]) ls.toArray();
//		
//		for (Integer integer : v) {
//			System.out.print(integer + "  ");
//		}
//		System.out.println();		
//		for (Integer integer : v2) {
//			System.out.print(integer + "  ");
//		}
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		
//		
//		partial_sort_copy(v, 0, v.length, v2, 0, v2.length, cmp);
////		Arrays.sort(v2,cmp2);
//		System.out.println("-------------------------");
////		for (Integer integer : v) {
////			System.out.print(integer + "  ");
////		}
////		System.out.println();		
//		for (Integer integer : v2) {
//			System.out.print(integer + "  ");
//		}
//	}
//
//	public static void _main(String...args) {
//		Comparator<Integer> cmp = new Comparator<Integer>() {
//			@Override
//			public int compare(Integer o1, Integer o2) {
//				return o1.compareTo(o2);
//			}
//		};
//		Integer[] v = new Integer[16];
//		for (int i = 0; i < v.length; i++) {
//			v[i] = new Integer(v.length - i);
//		}
//		
//		List<Integer> ls = Arrays.asList(v);
//		Collections.shuffle(ls);
//		v = (Integer[]) ls.toArray();
//		
//		for (Integer integer : v) {
//			System.out.print(integer + "  ");
//		}
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		
////		right_heaplify(v, 15, 0, cmp);
//		
////		heap_sort(v, 0, v.length, cmp);
//		sort_right_part(v, 2, v.length-2, 4, cmp);
//		
//		System.out.println("-------------------------");
//		for (Integer integer : v) {
//			System.out.print(integer + "  ");
//		}
//	}
//	
//	public static <T> T[] next_permutation(T[] v) {
//		return v;
//	}
//	
//	public static <T> T[] prev_permutation(T[] v) {
//		return v;
//	}

