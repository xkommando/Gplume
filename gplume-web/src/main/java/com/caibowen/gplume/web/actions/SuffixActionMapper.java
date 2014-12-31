package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.common.collection.URIPrefixTrie;
import com.caibowen.gplume.common.collection.URISuffixTrie;
import com.caibowen.gplume.web.IAction;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Bowen Cai on 12/31/2014.
 */
public class SuffixActionMapper<T extends IAction> implements Serializable, IActionMapper<T> {

    private static final long serialVersionUID = 1200692349238527678L;

    // match *.html
    private URISuffixTrie<T> matchSuffix = new URISuffixTrie<>();

    @Override
    public void add(String ptn, T action) throws IllegalArgumentException {
        if (ptn.startsWith("*"))
            matchSuffix.makeBranch(ptn.substring(1, ptn.length()), action);
        else
            throw new IllegalArgumentException("Must specify suffix from [" + ptn + "]");
    }

    @Override
    public boolean remove(String uri) {
        return matchSuffix.disjoin(uri) != null;
    }

    @Nullable
    @Override
    public IAction getAction(String uri) {
        return matchSuffix.matchSuffix(uri);
    }

    @Override
    public void clear() {
        matchSuffix.clear();
    }
}

