package com.caibowen.gplume.context;

import com.caibowen.gplume.context.Pod;
import com.caibowen.gplume.context.SpaceTree;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpaceTreeTest {

    @Test
    public void s1() {
        SpaceTree<Pod> t = new SpaceTree();
        t.put("id1", new Pod("pod1", null, "value1"));
        t.put("id1::1", new Pod("pod1.1", null, "value1.1"));
        t.put("id1::1::1", new Pod("pod1.1.1", null, "value1.1.1"));
        t.put("id2::1", new Pod("pod2.1", null, "value2.1"));
        Assert.assertEquals(t.size(), 4);
        assertEquals(t.find("id1").instance, "value1");
        assertEquals(t.find("id1::1").instance, "value1.1");
        assertEquals(t.find("id1::1::1").instance, "value1.1.1");
        assertEquals(t.find("id2::1").instance, "value2.1");
        t.remove("id1::1");
        t.remove("id1::1::1");
        Assert.assertEquals(t.size(), 2);
        assertEquals(t.find("id1").instance, "value1");
        assertNull(t.find("id1::1"));
        assertNull(t.find("id1::1::1"));
        assertEquals(t.find("id2::1").instance, "value2.1");
    }

    @Test
    public void overlap() {
        String[] arr = {"ns1","ns2","ns3","ns4"};
        String[] ptn = {"ns2","ns3","ns4","id1"};
//        int idx = SpaceTree.strOverlap(arr, 0, arr.length, ptn, 0, ptn.length - 1);
//        if (idx == -1) {
//            System.out.println("no");
//            return;
//        }
//        System.out.println(idx);
//        String[] fulp = new String[idx + ptn.length];
//        System.arraycopy(arr, 0, fulp, 0, idx);
//        System.arraycopy(ptn, 0, fulp, idx, ptn.length);
//        System.out.println(Arrays.toString(fulp));
    }

    @Test
    public void s2() {
        SpaceTree t = new SpaceTree();
        t.put("ns1::ns2::id1", 1);
        t.put("ns1::ns2::ns3::id1", 2);
        t.put("ns1::ns2::ns3::ns4::id2", 3);
//        t.put("ns1::ns2::id1", new Pod("4", null, "4"));
        assertEquals(t.size(), 3);
        assertEquals(t.find("ns1::ns2::ns3::id1"), 2);
        assertEquals(t.findByPartialId("id1", "ns3:ns5656", "ns1::ns2::ns3"), 2);
        assertEquals(t.findByPartialId("ns3::ns4::id2", "ns2::ns3::ns4", "ns1::ns2::ns3::ns4"), null);
        assertEquals(t.findByPartialId("ns3::ns4::id2", "ns1::ns2::ns3::ns4", "ns2::ns3::ns4"), 3);
        assertEquals(t.findByPartialId("ns3::ns4::id2", "ns3::ns5656", "ns1::ns2::ns3::ns4"), 3);
    }

}