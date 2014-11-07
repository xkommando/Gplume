package com.caibowen.gplume.common.test;

import com.caibowen.gplume.cache.XXXPOX;
import com.caibowen.gplume.common.collection.URIPrefixTrie;
import com.caibowen.gplume.common.collection.URISuffixTrie;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author BowenCai
 * @since 6-11-2014.
 */
public class TrieTest {

    @Test
    public void t1() {
        URIPrefixTrie<String> pt = new URIPrefixTrie<>();
        pt.makeBranch("12456", "1-6");
        pt.makeBranch("12ab", "12ab");
        pt.makeBranch("56ab", "56ab");
        pt.makeBranch("12cv", "12cv");
        Assert.assertEquals(pt.matchPrefix("12456rrrrrrrrrrrrrrr"), "1-6");
    }

    @Test
    public void t2() {
//        System.out.println(XXXPOX.class.isAssignableFrom(InvocationHandler.class));
//        System.out.println(InvocationHandler.class.isAssignableFrom(XXXPOX.class));

        URISuffixTrie<String> pt = new URISuffixTrie<>();
        pt.makeBranch("12456", "1-6");
        pt.makeBranch("12ab", "12ab");
        pt.makeBranch("56ab", "56ab");
        pt.makeBranch("12cv", "12cv");
        System.out.println(pt.matchSuffix("asfrwef56ab"));
        Assert.assertEquals(pt.matchSuffix("rrrrrrrrrrrrrrr12456"), "1-6");
    }
}
