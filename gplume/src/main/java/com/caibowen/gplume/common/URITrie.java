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

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/*
 *  benchmark
  K/V : String Integer
 HashMap TreeMap: put / get
 URITrie branch / matchPrefix
 
 1. 
 data : 100 random strings sized under 60 randomly
 
  map.get vs trie.matchPrefix  cycle : 100 * 1000
  
 hash 30031633
 tree 41383009
 trie 33626105
 
 
 tree slower than hash by: 37.798%
 trie slower than hash by: 11.969%
 
 trie faster than tree by: 18.744%
 
 
 2.  tree vs trie
 
  map.get vs trie.matchPrefix  cycle : 100 * 1000
 data 240 random strings size random under 60
 
 tree79230807
trie80667221


 3.  tree vs trie
 
  map.get vs trie.matchPrefix  cycle : 100 * 1000
  
 data 1000 random strings size random under 60
 
 tree 305070823
 trie 849602832
 
 trie slower than hash by : 64.092%
 

 conclusion:
 
 trie performs better(than rb-tree) when size is under 200 
 when a trie grows up, performance decrease sharply.
 

 */

/**
 *
 * thread-safe prefix tree for URI matching
 * 
 * @author BowenCai
 * 
 * @since 2014-1-14
 * @param <V>
 */
public class URITrie<V> implements Serializable {

	private static final long serialVersionUID = 6420673991145017909L;
	
	// from 45 to 126, note that TABLE is for indexing only, not all chars are valid for URI
	// partial URI pattern is "^/[\\w\\-\\.~/_]{1,64}$"
	protected static final char[] TABLE;
	protected static final char OFFSET;
	
	// 82
	protected static final int TABLE_LEN;
	// input checking
	protected static final Pattern PATTERN;
	
	static {
		TABLE = ("-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^"
					+"_`abcdefghijklmnopqrstuvwxyz{|}~").toCharArray();
//		ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
//		-._~:/?#[]@!$&'()*+,;=
		OFFSET = TABLE[0];
		TABLE_LEN = TABLE.length;//w /._-~#
		PATTERN = Pattern.compile("^[\\w\\-\\.~/_=#]{1,512}$");
	}

	protected int size;
	protected TrieNode root;
	
	public URITrie(){
		size = 0;
		root = new TrieNode(null, (int)('/' - OFFSET));// the char '/'
	}
	/**
	 * to create sub-tree
	 * @see  public URITrie<V> getBranch(final String k)
	 * 
	 * @param rt
	 * @param sz
	 */
	protected URITrie(TrieNode rt, int sz){
		this.size = sz;
		this.root = rt;
	}
	
	/**
	 * add a value to the tree, create new one if no matching branch exists
	 * 
	 * @param k
	 * @param v
	 * @return true value added, false value already exists at the branch(position is taken)
	 */
	synchronized public boolean branch(final String k, V v) {
		
		if (!PATTERN.matcher(k).matches()) {
			throw new IllegalArgumentException("string [" + k +"] must matches regex " +
					PATTERN.pattern());
		}
		/**
		 * start from index 1, index[0] is / 
		 */
		TrieNode ptr = root;//.sub[(int) (k.charAt(0) - BTN)];
		int len = k.length();
		int seqIdx = 1;
		
		while (seqIdx < len) {
			int _idx = (int) (k.charAt(seqIdx) - OFFSET);
			/**
			 * node array is not initialized until first visit,
			 *  so use nodeAt()  to detect node array,
			 *   after which, we can use node.subs[_idx] directly
			 *   @see TrieNode()
			 *   @see TrieNode.nodeAt()
			 */
			/**
			 * now subs is needed, initialize it.
			 * @param idx
			 * @return
			 */
//			public TrieNode nodeAt(int idx) {
//				if (subs == null) {
//					subs = new TrieNode[TABLE_LEN];
//				}
//				return subs[idx];
//			}
			if (ptr.subs == null) {
				ptr.subs = new TrieNode[TABLE_LEN];
				ptr.subs[_idx] = new TrieNode(ptr, _idx); 
			} else if (ptr.subs[_idx] == null) {
				ptr.subs[_idx] = new TrieNode(ptr, _idx);
			}
//			if (ptr.nodeAt(_idx) == null) {
//				ptr.subs[_idx] = new TrieNode(ptr, _idx);
//			}
			ptr = ptr.subs[_idx];
			seqIdx++;
		}
		
		if (ptr.var == null) {
			ptr.var = v;
			size++;
			return true;
		} else {
			return false;	
		}	
	}
	
	/**
	 * 
	 *  add a value to the tree, do nothing if no matching branch exists
	 * 
	 * @param k
	 * @param v
	 * @return true added to existing tree, false no matching branch exists 
	 * or value already exists at the branch(position is taken)
	 */
	synchronized public boolean join(final String k, V v) {
		
		if (!PATTERN.matcher(k).matches()) {
			throw new IllegalArgumentException("string must matches regex ^/[\\w\\-\\.~/]{1,64}$");
		}
		/**
		 * start from index 1, index[0] is / 
		 */
		TrieNode ptr = root;//.sub[(int) (k.charAt(0) - BTN)];
		int len = k.length();
		int seqIdx = 1;
		
		while (seqIdx < len) {
			
			if (ptr.subs == null) {
				return false;
			}
			
			int _idx = (int) (k.charAt(seqIdx) - OFFSET);
			if (ptr.subs[_idx] == null) {
				return false;
			}
			
			ptr = ptr.subs[_idx];
			seqIdx++;
		}
		
		if (ptr.var == null) {
			ptr.var = v;
			size++;
			return true;
		} else {
			return false;	
		}	
	}
	
	@SuppressWarnings("unchecked")
	public V getVar(final String k) {
		
		// check here to avoid array-index-out-of-range error
		
		// 2014-1-14: pattern match disabled to speed up searching
		// WARN: may cause array-out-of-boundary exception!
		
//		if (!PATTERN.matcher(k).matches()) {
//			throw new IllegalArgumentException("string must matches regex ^/[\\w\\-\\.~/][1,64]$");
//		}
		/**
		 * start from index 1, index[0] is / 
		 */
		TrieNode ptr = root;//.sub[(int) (k.charAt(0) - BTN)];
		int len = k.length();
		int seqIdx = 1;
		
		while (seqIdx < len) {
			int _idx = (int) (k.charAt(seqIdx) - OFFSET);
			if (ptr.subs == null) {
				return null;
			}
			TrieNode _node = ptr.subs[_idx];
//			System.out.println("idx " + _idx + "  ch " + TABLE[_idx]);
			if (_node == null) {
				return null;
			}
			ptr = _node;
			seqIdx++;
		}
		return (V) ptr.var;
	}
	/**
	 * 
	 * @param k
	 * @return sub-tree from this branch
	 */
	public URITrie<V> getBranch(final String k) {
		
		// check here to avoid array-index-out-of-range error
		
		// 2014-1-14: pattern match disabled to speed up searching
		// WARN: may cause array-out-of-boundary exception!
		
//		if (!PATTERN.matcher(k).matches()) {
//			throw new IllegalArgumentException("string must matches regex ^/[\\w\\-\\.~/][1,64]$");
//		}
		/**
		 * start from index 1, index[0] is / 
		 */
		TrieNode ptr = root;//.sub[(int) (k.charAt(0) - BTN)];
		int len = k.length();
		int seqIdx = 1;
		
		while (seqIdx < len) {
			
			if (ptr.subs == null) {
				return null;
			}
			int _idx = (int) (k.charAt(seqIdx) - OFFSET);
			TrieNode _node = ptr.subs[_idx];
//			System.out.println("idx " + _idx + "  ch " + TABLE[_idx]);
			if (_node == null) {
				return null;
			}
			ptr = _node;
			seqIdx++;
		}
		return new URITrie<>(ptr, countVar(ptr));
	}
	
	/**
	 * 
	 * @param k
	 * @return  the first value at the branch that is covered by 'k'.<b>
	 *  AKA, first value from the branch in which the branch is the prefix of 'k'
	 * 
	 */
	@SuppressWarnings("unchecked")
	public V matchPrefix(final String k) {
		
		// check here to avoid array-index-out-of-range error
		
		// 2014-1-14: pattern match disabled to speed up searching
		// WARN: may cause array-out-of-boundary exception!
		
//		if (!PATTERN.matcher(k).matches()) {
//			throw new IllegalArgumentException("string must matches regex ^/[\\w\\-\\.~/][1,64]$");
//		}
		
		TrieNode ptr = root;//.sub[(int) (k.charAt(0) - BTN)];
		int len = k.length();
		int seqIdx = 1;
		
		while (seqIdx < len) {
			
			if (ptr.subs == null) {
				return null;
			}
			
			int _idx = (int) (k.charAt(seqIdx) - OFFSET);
			TrieNode _node = ptr.subs[_idx];
			
			if (_node == null) {
				return null;
			}
			
			if (_node.var != null) {
				return (V) _node.var;
			}
			ptr = _node;
			seqIdx++;
		}
		return null;
	}
	/**
	 * 
	 * @param k
	 * @return -1 k covers existing brach
	 * 			0 no intersection
	 * 			1 k covered by existing branch
	 */
	public String covers_or_covered_by(final String k) {
		
		// check here to avoid array-index-out-of-range error
		
		// 2014-1-14: pattern match disabled to speed up searching
		// WARN: may cause array-out-of-boundary exception!
		
//		if (!PATTERN.matcher(k).matches()) {
//			throw new IllegalArgumentException("string must matches regex ^/[\\w\\-\\.~/][1,64]$");
//		}
		
		TrieNode ptr = root;//.sub[(int) (k.charAt(0) - BTN)];
		int len = k.length();
		int seqIdx = 1;
		
		while (seqIdx < len) {
			
			if (ptr.subs == null) {
				return null;
			}
			
			int _idx = (int) (k.charAt(seqIdx) - OFFSET);
			TrieNode _node = ptr.subs[_idx];
			
			if (_node == null) {
				return null; // no intersection
			}
			
			if (_node.var != null) {
				return _node.buildStr().toString(); // covered by k
			}
			ptr = _node;
			seqIdx++;
		}
		
		if (ptr != null) {
			int sz = countVar(ptr);
			return 0 == sz ? null : new URITrie<V>(ptr, sz).toString(); // covers k
		} else {
			return null;
		}
	}
	/**
	 * remove branch as long as possible from tree
	 * @param k
	 * @return the removed value. null if not found
	 * @see TrieNode.disjoin()
	 */
	synchronized public V disjoin(String k) {
		
		// check here to avoid array-index-out-of-range error
		// 2014-1-14: removed check to speed up searching
//		if (!PATTERN.matcher(k).matches()) {
//			throw new IllegalArgumentException("string must matches regex ^/[\\w\\-\\.~/][1,64]$");
//		}
		/**
		 * start from index 1, index[0] is / 
		 */
		TrieNode ptr = root;//.sub[(int) (k.charAt(0) - BTN)];
		int len = k.length();
		int seqIdx = 1;
		
		while (seqIdx < len) {
			int _idx = (int) (k.charAt(seqIdx) - OFFSET);
			if (ptr.subs == null) {
				return null;
			}
			if (ptr.subs[_idx] == null) {
				return null;
			}
			ptr = ptr.subs[_idx];
			seqIdx++;
		}
		
		ptr.disjoin();
		@SuppressWarnings("unchecked")
		V oldVar = (V) ptr.var;
		if (oldVar != null) {
			size--;
		}
		return oldVar;
	}
	
	public String getPrefix(V var) {
		TrieNode node = find(root, var);
		if (node != null) {
			return node.buildStr().toString();
		} else {
			return null;
		}
	}
	
	protected TrieNode find(TrieNode node, V var) {
		
		if (node.var != null && node.var.equals(var)) {
			return node;
			
		} else if (node.subs != null) {
			for (TrieNode _n : node.subs) {
				if (_n != null) {
					TrieNode varNode = find(_n, var);
					if (varNode != null) {
						return varNode;
					}
				}
			}
			return null;
			
		} else {
			return null;
		}
	}
	
	public int size() {
		return size;
	}

	public TrieNode getRoot() {
		return root;
	}
	
	synchronized public void clear() {
		this.size = 0;
		this.root = new TrieNode(null, (int)('/' - OFFSET));// the char '/'
	}
	
	private static int countVar(TrieNode node) {
		
		int count = node.var == null ? 0 : 1;
		if (node.subs != null) {
			for (TrieNode n : node.subs) {
				if (n != null) {
					count += countVar(n);
				}
			}
		}
		return count;
	}
	

	@Override
	public String toString() {

		class Closure{

			final public StringBuilder builder0;
			Closure() {
				 builder0 = new StringBuilder(128);
				 builder0.append('{').append('\n');
			}
			public void iter(TrieNode node, StringBuilder builder) {
				
				builder.append(TABLE[node.idx]);
				
				boolean commit = true;
				if (node.subs != null) {
					for (TrieNode n : node.subs) {
						if (n != null) {
							commit = false;
							iter(n, new StringBuilder(builder));
						}
					}
				}

				if (commit && node.var == null) {
						builder0.append(builder).append(':').append("null");
				} else if (node.var != null) {
					builder0.append(builder).append("  :  ").append(node.var.toString())
							.append(',').append('\n');
				}
			}
		}
		
		Closure dd = new Closure();
		
		dd.iter(root, new StringBuilder(128));
		
		return dd.builder0.append('}').toString();
	}
	
	public static class TrieNode implements Serializable {

		private static final long serialVersionUID = -7216677483159064029L;
		
		private TrieNode parent = null;
		int idx;
		/**
		 * subs is null, it will not be initialize until is needed
		 * @see nodeAt
		 */
		TrieNode[] subs;
		Object var;
		
		TrieNode(TrieNode p, int c) {
			parent = p;
			idx = c;
			subs = null;
			var = null;
		}

		public StringBuilder buildStr() {
			
			StringBuilder builder = new StringBuilder(32);
			TrieNode ptr = this;
			while (ptr.parent != null){
				builder.append(TABLE[ptr.idx]);
				ptr = ptr.parent;
			}
			builder.append(TABLE[ptr.idx]);
			return builder.reverse();
		}
		/**
		 * Disjoint a branch : the branch ends at this node 
		 * and no other nodes are attached at this branch
		 * @return
		 */
		public Object disjoin() {
			
			TrieNode ptr = this;
			
			while (ptr.parent != null){
				TrieNode pp = ptr.parent;
				for (int i = 0; i != TABLE_LEN; i++) {
					if (i == ptr.idx) {
						continue;
					} else if (pp.subs[i] != null) {
						pp.subs[ptr.idx] = null;
						return var;
					}
				}
				ptr = ptr.parent;
			} // while
			ptr.subs = new TrieNode[TABLE_LEN];
			return var;
		}
		
		public Object getValue() {
			return var;
		}
	}

}


//public static void main(String ... args) {
//	URITrie<Integer> trie = new URITrie<>();
//
//	trie.branch("/ea", 1);
//	trie.branch("/eb2", 2);
//	trie.branch("/ec3", 3);
//	
//	trie.branch("/ka1", 1);
//	trie.branch("/kb2", 2);
//	trie.branch("/kc3", 3);
//	System.out.println( trie.join("/exb", 46465)); // false
//	trie.branch("/kb", 22);
//	trie.branch("/kc", 35);
//	
//	trie.branch("/sadfd", 4);
//	trie.branch("/sdfg4/sdfji~-_sef3.7/", 4);
//	 
//	System.out.println("dj : " + trie.disjoin("/ec3"));
////	trie.clear();
////	trie.iiter(0);
//	System.out.println("size : " + trie.size());
//	System.out.println("size : " + URITrie.countVar(trie.getRoot()));
//	System.out.println("match prefix : " + trie.matchPrefix("/kc"));
//	System.out.println("cover : " + trie.covers_or_covered_by("/sdfg4/-"));
//	System.out.println("match " + trie.matchPrefix("/sadfd/-"));
//	System.out.println(trie.toString());
	
//	System.out.println(trie.getBud("/k3").getValue());
	
//	System.out.println(trie.getBud("/sdfg4/sdfji~-_sef3.7/").getValue());
//	System.out.println(countNode(trie.getRoot()));
//	System.out.println(trie.size());
//	System.out.println(trie.getBranch("/kb").size());


//	Class<?> clazz_  = (Class<?>) 
//			(	
//				(ParameterizedType)
//					trie.getClass().getTypeParameters()[0]
//							
//			).getActualTypeArguments()[0];
//	
//	System.out.println(clazz_.getName());
//	
//	
////	System.out.println(Pattern.matches("^/[\\w\\-\\.~/_]*$",
////			"/sdhf_sadifj235~soidjgf./sdofj.html-~/"));
//	Type[] typeParams = trie.getClass().getTypeParameters();
//	System.out.println(typeParams[0]);
//	Class<?> clazz = (Class<? extends AppEvent>) TypeTraits.getClass(typeParams[0], 0);
//	
//	
//	
//	System.out.println(clazz.getName());
	
	
//	URITrie<Double> test = new URITrie<>();
//	System.out.println( test.getClass().getTypeParameters()[0].equals(
//			trie.getClass().getTypeParameters()[0]
//			)
//			);	
//}
