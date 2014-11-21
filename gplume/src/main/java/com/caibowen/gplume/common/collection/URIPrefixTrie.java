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
package com.caibowen.gplume.common.collection;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.regex.Pattern;

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
 * supported chars:
 * ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
 * -._~:/?#[]@!$&'()*+,;=
 *
 * @author BowenCai
 * 
 * @since 2014-1-14
 * @param <V>
 */
public class URIPrefixTrie<V> implements Trie<V>, Serializable {

	private static final long serialVersionUID = 6420673991145017909L;

	public static final class URI_DEF {
		// from 45 to 126, note that TABLE is for indexing only, not all chars are valid for URI
		// partial URI pattern is "^/[\\w\\-\\.~/_]{1,64}$"
		static final char[] TABLE;
		static final char OFFSET;

		// 82
		static final int TABLE_LEN;
		// input checking
		static final Pattern PATTERN;

		static {

			TABLE = ("-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^"
					+"_`abcdefghijklmnopqrstuvwxyz{|}~").toCharArray();
			OFFSET = TABLE[0];
			TABLE_LEN = TABLE.length;//w /._-~#
			PATTERN = Pattern.compile("^[\\w\\-\\.~/_=#]{1,512}$");
		}
	}


	protected int size;
	protected TrieNode root;
	public URIPrefixTrie(){
		size = 0;
		root = new TrieNode(null, '/' - URI_DEF.OFFSET);// the char '/'
	}


	/**
	 * to create sub-tree
	 * @see  public URITrie<V> branchAt(final String k)
	 * 
	 * @param rt
	 * @param sz
	 */
	protected URIPrefixTrie(@Nonnull TrieNode rt, int sz){
		this.size = sz;
		this.root = rt;
	}
	
	/**
	 * add a value to the tree, create new branches if no matching branch exists
	 * 
	 * @param k
	 * @param v
	 * @return true value added, false value already exists at the branch(position is taken)
	 */
	@Override
	synchronized public boolean makeBranch(@Nonnull final CharSequence k, @Nonnull V v) {
		
		if (!URI_DEF.PATTERN.matcher(k).matches()) {
			throw new IllegalArgumentException("string [" + k +"] must matches regex " +
					URI_DEF.PATTERN.pattern());
		}
		/**
		 * start from index 1, index[0] is / 
		 */
		TrieNode ptr = root;
		int len = k.length();
		int seqIdx = 0;

		while (seqIdx < len) {
			int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
			/**
			 * node array is not initialized until first visit,
			 *  so use nodeAt()  to detect node array,
			 *   after which, we can use node.subs[_idx] directly
			 *   @see TrieNode()
			 *   @see TrieNode.nodeAt()
			 */
//			 now subs is needed, initialize it.
			if (ptr.subs == null) {
				ptr.subs = new TrieNode[URI_DEF.TABLE_LEN];
				ptr.subs[_idx] = new TrieNode(ptr, _idx);
			} else if (ptr.subs[_idx] == null) {
				ptr.subs[_idx] = new TrieNode(ptr, _idx);
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
	
	/**
	 * 
	 *  add a value to the existing branch of the tree, do nothing if no existing matching branch exists
	 * 
	 * @param k
	 * @param v
	 * @return true added to existing tree, false no matching branch exists 
	 * or value already exists at the branch(position is taken)
	 */
	@Override
	synchronized public boolean join(@Nonnull CharSequence k, @Nonnull V v) {
		
		if (!URI_DEF.PATTERN.matcher(k).matches()) {
			throw new IllegalArgumentException("string must matches regex ^/[\\w\\-\\.~/]{1,64}$");
		}
		/**
		 * start from index 1, index[0] is / 
		 */
		TrieNode ptr = root;
		int len = k.length();
		int seqIdx = 0;
		
		while (seqIdx < len) {
			if (ptr.subs == null)
				return false;
			int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
			if (ptr.subs[_idx] == null)
				return false;
			ptr = ptr.subs[_idx];
			seqIdx++;
		}

		if (ptr.var != null)
			return false;

		ptr.var = v;
		size++;
		return true;
	}

	@Override
	public V valAt(final CharSequence k) {
		TrieNode ptr = root;
		int len = k.length();
		int seqIdx = 0;
		
		while (seqIdx < len) {
			int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
			if (ptr.subs == null)
				return null;
			TrieNode _node = ptr.subs[_idx];
			if (_node == null)
				return null;

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
	@Override
	public URIPrefixTrie<V> branchAt(final CharSequence k) {
		TrieNode ptr = root;
		int len = k.length();
		int seqIdx = 0;
		while (seqIdx < len) {
			if (ptr.subs == null)
				return null;
			int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
			TrieNode _node = ptr.subs[_idx];
			if (_node == null)
				return null;

			ptr = _node;
			seqIdx++;
		}
		return new URIPrefixTrie<>(ptr, countVar(ptr));
	}
	
	/**
	 * 
	 * @param k
	 * @return  the first value at the branch that is covered by 'k'.
	 *  AKA, first value from the branch in which the branch is the prefix of 'k'
	 * 
	 */
	@Override
	public V matchPrefix(CharSequence k) {
		TrieNode ptr = root;
		int len = k.length();
		int seqIdx = 0;
		
		while (seqIdx < len) {
			if (ptr.subs == null)
				return null;
			int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
			TrieNode _node = ptr.subs[_idx];
			if (_node == null)
				return null;
			if (_node.var != null)
				return (V) _node.var;

			ptr = _node;
			seqIdx++;
		}
		return null;
	}

	@Override
	public V matchSuffix(CharSequence k) {
		throw new UnsupportedOperationException("this is a prefix trie, perhaps what you are looking for is a suffix trie");
	}

	/**
	 * 
	 * @param k
	 * @return -1 k covers existing brach
	 * 			0 no intersection
	 * 			1 k covered by existing branch
	 */
	@Override
	public String coversOrCoveredBy(final CharSequence k) {
		TrieNode ptr = root;
		int len = k.length();
		int seqIdx = 0;
		
		while (seqIdx < len) {
			if (ptr.subs == null)
				return null;
			int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
			TrieNode _node = ptr.subs[_idx];
			if (_node == null)
				return null; // no intersection

			if (_node.var != null)
				return _node.buildStr().toString(); // covered by k

			ptr = _node;
			seqIdx++;
		}
		
		if (ptr != null) {
			int sz = countVar(ptr);
			return 0 == sz ? null : new URIPrefixTrie<V>(ptr, sz).toString(); // covers k
		} else {
			return null;
		}
	}

	/**
	 * remove branch as long as possible from tree
	 * @param k
	 * @return the removed value. null if not found
	 */
	@Override
	synchronized public V disjoin(CharSequence k) {
		TrieNode ptr = root;
		int len = k.length();
		int seqIdx = 0;
		
		while (seqIdx < len) {
			int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
			if (ptr.subs == null || ptr.subs[_idx] == null)
				return null;
			ptr = ptr.subs[_idx];
			seqIdx++;
		}
		
		ptr.disjoin();
		V oldVar = (V) ptr.var;
		if (oldVar != null) {
			size--;
		}
		return oldVar;
	}
	
	@Override
	public StringBuilder getPrefix(V var) {
		TrieNode node = find(root, var);
		if (node != null) {
			return node.buildStr();
		} else {
			return null;
		}
	}
	
	protected TrieNode find(TrieNode node, V var) {
		
		if (node.var != null && node.var.equals(var)) {
			return node;

		} else if (node.subs != null) {
			for (TrieNode _n : node.subs) {
				if (_n == null)
					return null;
				TrieNode varNode = find(_n, var);
				if (varNode != null)
					return varNode;
			}
		}
		return null;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public TrieNode getRoot() {
		return root;
	}
	
	@Override
	synchronized public void clear() {
		this.size = 0;
		this.root = new TrieNode(null, '/' -URI_DEF. OFFSET);// the char '/'
	}
	
	private static int countVar(TrieNode node) {
		
		int count = node.var == null ? 0 : 1;
		if (node.subs != null) {
			for (TrieNode n : node.subs) {
				if (n != null)
					count += countVar(n);
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
				 builder0.append('{').append("\r\n");
			}
			public void iter(TrieNode node, StringBuilder builder) {
				
				builder.append(URI_DEF.TABLE[node.idx]);
				
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
					builder0.append(builder).append(" : ").append(node.var.toString())
							.append(',').append("\r\n");
				}
			}
		}
		
		Closure dd = new Closure();
		
		dd.iter(root, new StringBuilder(128));
		
		return dd.builder0.append('}').toString();
	}


}
