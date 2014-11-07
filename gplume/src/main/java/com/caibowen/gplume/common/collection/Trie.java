package com.caibowen.gplume.common.collection;

import javax.annotation.Nonnull;

/**
 * @author BowenCai
 * @since 6-11-2014.
 */
public interface Trie<V> {

    /**
     * add a value to the tree, create new branches if no matching branch exists
     *
     * @param k
     * @param v
     * @return true value added, false value already exists at the branch(position is taken)
     */
    boolean makeBranch(@Nonnull CharSequence k, @Nonnull V v);

    /**
     *
     *  add a value to the existing branch of the tree, do nothing if no existing matching branch exists
     *
     * @param k
     * @param v
     * @return true added to existing tree, false no matching branch exists
     * or value already exists at the branch(position is taken)
     */
    boolean join(@Nonnull CharSequence k, @Nonnull V v);

    V valAt(CharSequence k);

    /**
     *
     * @param k
     * @return sub-tree from this branch
     */
    URIPrefixTrie<V> branchAt(CharSequence k);

    /**
     *
     * @param k
     * @return  the first value at the branch that is covered by 'k'.
     *  AKA, first value from the branch in which the branch is the prefix of 'k'
     *
     */
    V matchSuffix(CharSequence k);


    /**
     *
     * @param k
     * @return  the first value at the branch that is covered by 'k'.
     *  AKA, first value from the branch in which the branch is the prefix of 'k'
     *
     */
    V matchPrefix(CharSequence k);

    /**
     *
     * @param k
     * @return -1 k covers existing brach
     * 			0 no intersection
     * 			1 k covered by existing branch
     */
    String coversOrCoveredBy(CharSequence k);

    /**
     * remove branch as long as possible from tree
     * @param k
     * @return the removed value. null if not found
     * @see com.caibowen.gplume.common.collection.URIPrefixTrie.TrieNode.disjoin()
     */
    V disjoin(CharSequence k);

    StringBuilder getPrefix(V var);

    int size();

    TrieNode getRoot();

    void clear();
}
