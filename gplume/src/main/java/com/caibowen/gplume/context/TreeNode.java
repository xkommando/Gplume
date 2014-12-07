package com.caibowen.gplume.context;

import com.caibowen.gplume.context.bean.BeanVisitor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BowenCai
 * @since 7-11-2014.
 */
class TreeNode<T> extends ConcurrentHashMap<String, T> {
    private static final long serialVersionUID = -2808170053256196522L;

    public String name;
    public TreeNode parent;
    public ConcurrentHashMap<String, TreeNode<T> > children;

    TreeNode(String name,TreeNode parent) {
        super(16);
        this.name = name;
        this.parent = parent;
    }

    int count() {
        int ct = size();
        if (children != null)
            for (TreeNode tn : children.values())
                ct += tn.count();

        return ct;
    }
    void intake(BeanVisitor visitor) {
        for (T p : this.values())
            visitor.visit(p);

        if (children != null)
            for (TreeNode tn : children.values())
                tn.intake(visitor);
    }

}
