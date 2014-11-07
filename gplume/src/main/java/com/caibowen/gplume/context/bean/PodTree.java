package com.caibowen.gplume.context.bean;

import com.caibowen.gplume.annotation.Internal;
import com.caibowen.gplume.misc.Str;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author BowenCai
 * @since 7-11-2014.
 */
@Internal
class PodTree implements Serializable {

    private static final long serialVersionUID = 1328069590903412215L;

    private static final Pattern STR_TOK = Pattern.compile(XMLTags.NS_DELI);

    private int size;
    private final TreeNode root;


    public PodTree() {
        root = new TreeNode(Str.EMPTY, null);
        size = 0;
    }

    public PodTree(TreeNode root) {
        this.root = root;
        this.size = root.count();
    }

    TreeNode createNamespace(String str) {
        String[] nss = STR_TOK.split(str);
        TreeNode cur = root;
        int i = 0;
        int len = nss.length;
        for (; i < len; i++) {
            String ns = nss[i];
            if (cur.children == null) {
                cur.children = new ConcurrentHashMap<>(16);
                TreeNode next = new TreeNode(ns, cur);
                cur.children.put(ns, next);
                cur = next;
                continue;
            }
            TreeNode next = cur.children.get(ns);
            if (next == null) {
                next = new TreeNode(ns, cur);
                cur.children.put(ns, next);
            }
            cur = next;
        }
        return cur;
    }

    /**
     *
     * @param id
     * @param pod
     * @return true if new value added, false if already exists a pod
     */
    boolean addPod(String id, Pod pod) {
        String[] nss = STR_TOK.split(id);
        TreeNode cur = root;
        int i = 0;
        int len = nss.length;
        for (; i < len - 1; i++) {
            String ns = nss[i];
            if (cur.children == null) {
                cur.children = new ConcurrentHashMap<>(16);
                TreeNode next = new TreeNode(ns, cur);
                cur.children.put(ns, next);
                cur = next;
                continue;
            }
            TreeNode next = cur.children.get(ns);
            if (next == null) {
                next = new TreeNode(ns, cur);
                cur.children.put(ns, next);
            }
            cur = next;
        }
        String podId = nss[i];
        if (cur.contains(podId))
            return false;
        cur.put(podId, pod);
        size++;
        return true;
    }

    @Nullable
    Pod findPod(String id) {
        String[] nss = STR_TOK.split(id);
        TreeNode cur = root;
        int i = 0;
        int len = nss.length;
        for (; i < len - 1; i++) {
            String ns = nss[i];
            if (cur.children == null)
                return null;

            TreeNode next = cur.children.get(ns);
            if (next == null)
                return null;
            cur = next;
        }

        String podId = nss[i];
        return cur.get(podId);
    }

    Pod remove(String id) {
        String[] nss = STR_TOK.split(id);
        TreeNode cur = root;
        int i = 0;
        int len = nss.length;
        for (; i < len - 1; i++) {
            String ns = nss[i];
            if (cur.children == null)
                return null;
            TreeNode next = cur.children.get(ns);
            if (next == null)
                return null;
            cur = next;
        }
        String podId = nss[i];
        Pod r = cur.remove(podId);
        if (r != null) {
            size--;
            if (cur.size() == 0
                    && (cur.children == null || cur.children.size() == 0)
                    && cur.parent != null)
                cur.parent.children.remove(cur.name);
        }
        return r;
    }

    public int size() {
        return size;
    }


    public void intake(BeanVisitor visitor) {
        root.intake(visitor);
    }

//    public static void main(String...aretge) {
//        PodTree t = new PodTree();
//        t.addPod("id1", new Pod("pod1", null, "value1"));
//        t.addPod("id1::1", new Pod("pod1.1", null, "value1.1"));
//        t.addPod("id1::1::1", new Pod("pod1.1.1", null, "value1.1.1"));
//        t.addPod("id2::1", new Pod("pod2.1", null, "value2.1"));
//        System.out.println(t.size());
//        System.out.println(t.findPod("id1").getInstance());
//        System.out.println(t.findPod("id1::1").getInstance());
//        System.out.println(t.findPod("id1::1::1").getInstance());
//        System.out.println(t.findPod("id2::1").getInstance());
//        t.remove("id1::1");
//        t.remove("id1::1::1");
//        System.out.println(t.size());
//        System.out.println(t.findPod("id1").getInstance());
//        System.out.println(t.findPod("id1::1"));
//        System.out.println(t.findPod("id1::1::1"));
//        System.out.println(t.findPod("id2::1").getInstance());
//    }

}
