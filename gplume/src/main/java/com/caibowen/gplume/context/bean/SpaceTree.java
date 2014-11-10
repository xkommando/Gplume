package com.caibowen.gplume.context.bean;

import com.caibowen.gplume.annotation.Internal;
import com.caibowen.gplume.misc.Str;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author BowenCai
 * @since 7-11-2014.
 */
@Internal
class SpaceTree<T> implements Serializable {

    private static final long serialVersionUID = 1328069590903412215L;

    private static final Pattern STR_TOK = Pattern.compile(XMLTags.NS_DELI);

    private int size;
    private final TreeNode<T> root;


    public SpaceTree() {
        root = new TreeNode<>(Str.EMPTY, null);
        size = 0;
    }

    public SpaceTree(TreeNode root) {
        this.root = root;
        this.size = root.count();
    }

    TreeNode createNamespace(String str) {
        String[] nss = STR_TOK.split(str);
        TreeNode<T> cur = root;
        int i = 0;
        int len = nss.length;
        for (; i < len; i++) {
            String ns = nss[i];
            if (cur.children == null) {
                cur.children = new ConcurrentHashMap<>(16);
                TreeNode<T> next = new TreeNode<>(ns, cur);
                cur.children.put(ns, next);
                cur = next;
                continue;
            }
            TreeNode<T> next = cur.children.get(ns);
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
    boolean put(String id, T pod) {
        String[] nss = STR_TOK.split(id);
        TreeNode<T> cur = root;
        int len = nss.length - 1;
        for (int i = 0; i < len; i++) {
            String ns = nss[i];
            if (cur.children == null) {
                cur.children = new ConcurrentHashMap<>(16);
                TreeNode<T> next = new TreeNode<>(ns, cur);
                cur.children.put(ns, next);
                cur = next;
                continue;
            }
            TreeNode<T> next = cur.children.get(ns);
            if (next == null) {
                next = new TreeNode(ns, cur);
                cur.children.put(ns, next);
            }
            cur = next;
        }
        String podId = nss[len];
        if (cur.contains(podId))
            return false;
        cur.put(podId, pod);
        size++;
        return true;
    }

    public String getFullPath(String pid, String curNS, String refNS) {
        if (find(pid) != null)
            return pid;
        String lid = curNS + XMLTags.NS_DELI + pid;
        if (find(lid) != null)
            return lid;
        if (find((lid = refNS + XMLTags.NS_DELI + pid)) != null)
            return lid;

        return null;
    }

    public String getFullPath(String pid, @Nonnull String...refNS) {
        String[] pNss = STR_TOK.split(pid);
        if (pNss.length == 1) { // most case.
            String _id = refNS[0] + XMLTags.NS_DELI + pid;
            T pod = find(_id);
            if (pod != null)
                return _id;
            if (refNS.length > 1) {
                _id = refNS[1] + XMLTags.NS_DELI + pid;
                if (null != find(_id))
                    return _id;
            }
        }

        for (String curNS : refNS) {
            String[] curNss = STR_TOK.split(curNS);
            int idx = strOverlap(curNss, 0, curNss.length, pNss, 0, pNss.length - 1);
            if (idx == -1)
                continue;
            String[] fulp = new String[idx + pNss.length];
            System.arraycopy(curNss, 0, fulp, 0, idx);
            System.arraycopy(pNss, 0, fulp, idx, pNss.length);
            T p = find(fulp);
            if (p != null)
                return Str.Utils.join(fulp, XMLTags.NS_DELI);
        }
        return null;
    }

    /**
     *
     * @param pid partial Id
     * @param curNS current namespace
     * @param refNS referred namespace
     * @return
     */
    T findByPartialId(@Nonnull String pid, @Nonnull String...refNS) {
        String[] pNss = STR_TOK.split(pid);
        if (pNss.length == 1) { // most case.
            String _id = refNS[0] + XMLTags.NS_DELI + pid;
            T pod = find(_id);
            if (pod != null)
                return pod;
            if (refNS.length > 1) {
                _id = refNS[1] + XMLTags.NS_DELI + pid;
                if (null != (pod = find(_id)))
                    return pod;
            }
        }
        for (String curNS : refNS) {
            String[] curNss = STR_TOK.split(curNS);
            int idx = strOverlap(curNss, 0, curNss.length, pNss, 0, pNss.length - 1);
            if (idx == -1)
                continue;
            String[] fulp = new String[idx + pNss.length];
            System.arraycopy(curNss, 0, fulp, 0, idx);
            System.arraycopy(pNss, 0, fulp, idx, pNss.length);
            return find(fulp);
        }
        throw new IllegalArgumentException("cannot find bean with id[" + pid
                + "] referred namespace[" + Arrays.toString(refNS) + "]");
    }

    private static int strOverlap(String[] arr, int astar, int aend, String[] ptn, int ptnStart, int ptnEnd) {
        int iend = aend - (ptnEnd - ptnStart) + 1;
        int i = astar;
        int j = ptnStart;
        for (; i < iend; i++) {
            int ii = i;
            j = ptnStart;
            for (; j < ptnEnd; j++, ii++) {
                if (! ptn[j].equals(arr[ii]))
                    break;
            }
            if (j == ptnEnd)
                break;
        }
        if (j == ptnEnd)
            return i;
        else return -1; // assert i == iend
    }



    @Nullable
    T find(@Nonnull String id) {
        return find(STR_TOK.split(id));
    }

    @Nullable
    T find(@Nonnull String[] nss) {
        TreeNode<T> cur = root;
        int i = 0;
        int len = nss.length;
        for (; i < len - 1; i++) {
            String ns = nss[i];
            if (cur.children == null)
                return null;

            TreeNode<T> next = cur.children.get(ns);
            if (next == null)
                return null;
            cur = next;
        }

        String podId = nss[i];
        return cur.get(podId);
    }


    T removeByPartialId(String pid, String curNS, String refNS) {
        return remove(getFullPath(pid, curNS, refNS));
    }

    T remove(String id) {
        String[] nss = STR_TOK.split(id);
        return remove(nss);
    }

    T remove(String[] nss) {
        TreeNode<T> cur = root;
        int i = 0;
        int len = nss.length;
        for (; i < len - 1; i++) {
            String ns = nss[i];
            if (cur.children == null)
                return null;
            TreeNode<T> next = cur.children.get(ns);
            if (next == null)
                return null;
            cur = next;
        }
        String podId = nss[i];
        T r = cur.remove(podId);
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

}
