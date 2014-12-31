package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.common.collection.URIPrefixTrie;
import com.caibowen.gplume.common.collection.URISuffixTrie;
import com.caibowen.gplume.web.IAction;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 *
 * map by full uri or uri prefix or uri suffix
 *
 * Created by Bowen Cai on 12/31/2014.
 */
public class TriMapper<T extends IAction> implements Serializable, IActionMapper<T> {

    private static final long serialVersionUID = 1200692349238527678L;

    // strict match
    private HashMap<String, T> matchFull = new HashMap<>(128);

    // match /xyz/* or /sadfj*
    private URIPrefixTrie<T> matchPrefix = new URIPrefixTrie<>();

    // match *.html
    private URISuffixTrie<T> matchSuffix = new URISuffixTrie<>();

    @Override
    public void add(String ptn, T action) throws IllegalArgumentException {
        if (ptn.startsWith("*"))
            matchSuffix.makeBranch(ptn.substring(1, ptn.length() - 1), action);
        else if (ptn.endsWith("*"))
            matchPrefix.makeBranch(ptn.substring(0, ptn.length() - 1), action);
        else
            matchFull.put(ptn, action);
    }

    @Override
    public boolean remove(String uri) {
        return matchFull.remove(uri) != null
                || matchPrefix.disjoin(uri) != null
                || matchSuffix.disjoin(uri) != null;
    }

    @Override
    public IAction getAction(String uri) {
        IAction a;
        if (null != (a = matchSuffix.matchSuffix(uri)))
            return a;
        else if (null != (a = matchPrefix.matchPrefix(uri)))
            return a;
        return matchFull.get(uri);
    }

    @Override
    public void clear() {
        matchFull.clear();
        matchPrefix.clear();
        matchSuffix.clear();
    }
}
