package com.caibowen.gplume.jdbc;

import com.caibowen.gplume.annotation.Internal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * @author BowenCai
 * @since 29-10-2014.
 */
@Internal("internal list for connection holders")
public class LocalList {

    private static final ThreadLocal<ArrayList<?>> local = new ThreadLocal<>();

    public static<E> void push(@Nonnull E item) {
        ArrayList arr = local.get();
        if (arr == null) {
            arr = new ArrayList(8);
            local.set(arr);
        }
        arr.add(item);
    }


    @Nonnull
    public static <E> E pop() {
        ArrayList arr = local.get();
        if (arr == null)
            return null;
        int idx = arr.size() - 1;
        E last = (E)arr.get(idx);
        arr.remove(idx);
        return last;
    }

    @Nullable
    public static <E> E front() {
        return front(0);
    }

    @Nullable
    public static <E> E front(int order) {
        ArrayList arr = local.get();
        if (arr == null)
            return null;
        if (order >= arr.size())
            return null;
        return (E)arr.get(order);
    }

    @Nullable
    public static <E> E last() {
        return last(0);
    }

    @Nullable
    public static <E> E last(int order) {
        ArrayList arr = local.get();
        if (arr == null)
            return null;
        int idx = arr.size() - 1 - order;
        if (idx < 0)
            return null;
        E last = (E)arr.get(idx);
        return last;
    }

    @Nullable
    public static <E> E remove(int order) {
        ArrayList arr = local.get();
        if (arr == null)
            return null;
        return (E)arr.remove(order);
    }
    @Nullable
    public static <E> E remove(Object obj) {
        ArrayList arr = local.get();
        if (arr == null)
            return null;
        int idx = search(obj);
        if (idx == -1)
            return null;

        E v = (E) arr.get(idx);
        arr.remove(idx);
        return v;
    }
    /**
     *
     * @param item
     * @param <E>
     * @return old value or null if is empty
     */
    @Nullable
    public static <E> E setTop(@Nonnull E item) {
        ArrayList arr = local.get();
        if (arr == null) {
            arr = new ArrayList(8);
            arr.add(item);
            return null;
        }
        int idx = arr.size() - 1;
        if (idx < 0) {
            arr.add(item);
            return null;
        }
        E old = (E)arr.get(idx);
        arr.set(idx, item);
        return old;
    }

    public boolean empty() {
        ArrayList arr = local.get();
        if (arr == null)
            return true;
        return arr.size() == 0;
    }

    public static <E> int search(E o) {
        ArrayList arr = local.get();
        if (arr == null)
            return -1;
        return arr.lastIndexOf(o);
    }

    /**
     *
     * @return -1 when value for this thread has not been set
     */
    public static int size() {
        ArrayList arr = local.get();
        if (arr == null)
            return -1;
        return arr.size();
    }

    public void clear() {
        local.remove();
    }

}
