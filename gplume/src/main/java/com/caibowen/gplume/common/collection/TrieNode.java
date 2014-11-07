package com.caibowen.gplume.common.collection;

import java.io.Serializable;
import static com.caibowen.gplume.common.collection.URIPrefixTrie.URI_DEF;

/**
 * @author BowenCai
 * @since 6-11-2014.
 */
public class TrieNode implements Serializable {

    private static final long serialVersionUID = -7216677483159064029L;

    public final TrieNode parent;
    public final int idx;
    /**
     * subs is null, it will not be initialize until is needed
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

        StringBuilder builder = new StringBuilder(64);
        TrieNode ptr = this;
        while (ptr.parent != null){
            builder.append(URI_DEF.TABLE[ptr.idx]);
            ptr = ptr.parent;
        }
        builder.append(URI_DEF.TABLE[ptr.idx]);
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
            for (int i = 0; i != URI_DEF.TABLE_LEN; i++) {
                if (i == ptr.idx) {
                    continue;
                } else if (pp.subs[i] != null) {
                    pp.subs[ptr.idx] = null;
                    return var;
                }
            }
            ptr = ptr.parent;
        } // while
        ptr.subs = new TrieNode[URI_DEF.TABLE_LEN];
        return var;
    }

    public Object getValue() {
        return var;
    }
}