package com.caibowen.gplume.common.collection;

import javax.annotation.Nonnull;
import java.io.Serializable;

import static com.caibowen.gplume.common.collection.URIPrefixTrie.URI_DEF;

/**
 *
 * Similar to the URIPrefixTrie, only reversed
 *
 * @author BowenCai
 * @since 30-10-2014.
 */
public class URISuffixTrie<V> implements Trie<V>, Serializable {

    private static final long serialVersionUID = -1495004473834609704L;

    protected int size;
    protected TrieNode root;
    //	protected ReadWriteLock lock = new ReentrantReadWriteLock();
    public URISuffixTrie(){
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
    protected URISuffixTrie(@Nonnull TrieNode rt, int sz){
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
        int seqIdx = len - 1;

        while (-1 < seqIdx) {
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
            seqIdx--;
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
        int seqIdx = len - 1;

        while (-1 < seqIdx) {
            if (ptr.subs == null)
                return false;
            int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
            if (ptr.subs[_idx] == null)
                return false;
            ptr = ptr.subs[_idx];
            seqIdx--;
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
        int seqIdx = len - 1;

        while (-1 < seqIdx) {
            int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
            if (ptr.subs == null)
                return null;
            TrieNode _node = ptr.subs[_idx];
            if (_node == null)
                return null;

            ptr = _node;
            seqIdx--;
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
        int seqIdx = len - 1;

        while (-1 < seqIdx) {
            if (ptr.subs == null)
                return null;
            int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
            TrieNode _node = ptr.subs[_idx];
            if (_node == null)
                return null;

            ptr = _node;
            seqIdx--;
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
    public V matchSuffix(CharSequence k) {
        TrieNode ptr = root;
        int len = k.length();
        int seqIdx = len - 1;

        while (-1 < seqIdx) {
            if (ptr.subs == null)
                return null;
            int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
            TrieNode _node = ptr.subs[_idx];
            if (_node == null)
                return null;
            if (_node.var != null)
                return (V) _node.var;

            ptr = _node;
            seqIdx--;
        }
        return null;
    }

    @Override
    public V matchPrefix(CharSequence k) {
        throw new UnsupportedOperationException("this is a suffix trie, perhaps what you are looking for is a prefix trie");
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
        int seqIdx = len - 1;

        while (-1 < seqIdx) {
            if (ptr.subs == null)
                return null;
            int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
            TrieNode _node = ptr.subs[_idx];
            if (_node == null)
                return null; // no intersection

            if (_node.var != null)
                return _node.buildStr().toString(); // covered by k

            ptr = _node;
            seqIdx--;
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
        int seqIdx = len - 1;

        while (-1 < seqIdx) {
            int _idx = k.charAt(seqIdx) - URI_DEF.OFFSET;
            if (ptr.subs == null || ptr.subs[_idx] == null)
                return null;
            ptr = ptr.subs[_idx];
            seqIdx--;
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
