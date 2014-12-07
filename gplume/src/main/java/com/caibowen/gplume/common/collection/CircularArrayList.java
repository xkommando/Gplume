package com.caibowen.gplume.common.collection;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 *
 */
public class CircularArrayList extends AbstractList implements List, Serializable {

    private Object[] value;
    private int head;
    private int tail;

    private int size;
    private int mask;
    public CircularArrayList() {
        this(8);
    }

    public CircularArrayList(int size) {
        value = new Object[size];
    }

    public CircularArrayList(Collection c) {
        size = tail = c.size();
        value = new Object[c.size()];
        c.toArray(value);
    }


    @Override
    public boolean isEmpty() {
        return head == tail;
    }

    // We use this method to ensure that the capacity of the
    // list will suffice for the number of elements we want to
    // insert.  If it is too small, we make a new, bigger array
    // and copy the old elements in.
    public void ensureCapacity(int minCapacity) {
        int oldCapacity = value.length;
        if (minCapacity < oldCapacity)
            return;

        int newCapacity = (oldCapacity * 3) / 2 + 1;
        if (newCapacity < minCapacity)
            newCapacity = minCapacity;
        Object newData[] = new Object[newCapacity];
        toArray(newData);
        tail = size;
        head = 0;
        value = newData;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }

    @Override
    public int indexOf(Object elem) {
        if (elem == null) {
            for (int i = 0; i < size; i++)
                if (value[(i + head) % value.length] == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (elem.equals(value[(i + head) % value.length]))
                    return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            for (int i = size - 1; i >= 0; i--)
                if (value[(i + head) % value.length] == null)
                    return i;
        } else {
            for (int i = size - 1; i >= 0; i--)
                if (elem.equals(value[(i + head) % value.length]))
                    return i;
        }
        return -1;
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[size]);
    }

    @Override
    public Object[] toArray(Object a[]) {
        if (a.length < size)
            a = (Object[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        if (head < tail) {
            System.arraycopy(value, head, a, 0, tail - head);
        } else {
            System.arraycopy(value, head, a, 0,
                    value.length - head);
            System.arraycopy(value, 0, a, value.length - head,
                    tail);
        }
        if (a.length > size)
            a[size] = null;
        return a;
    }

    private void rangeCheck(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
    }

    @Override
    public Object get(int index) {
        rangeCheck(index);
        return value[(index + head) % value.length];
    }

    public Object front() {
        return value[head];
    }
    public Object back() {
        return value[tail];
    }

    public Object popBack() {
        Object v = value[tail];
        value[tail] = null;
        tail = (tail - 1 + value.length) % value.length;
        size--;
        return v;
    }

    public Object popFront() {
        Object v = value[head];
        value[head] = null;
        head = (head + 1) % value.length;
        size--;
        return v;
    }

    @Override
    public Object set(int index, Object element) {
        modCount++;
        rangeCheck(index);
        Object oldValue = value[(index + head) % value.length];
        value[(index + head) % value.length] = element;
        return oldValue;
    }

    @Override
    public boolean add(Object o) {
        modCount++;
        ensureCapacity(size + 2);
        value[tail] = o;
        tail = (tail + 1) % value.length;
        size++;
        return true;
    }

    @Override
    public void add(int index, Object element) {
        modCount++;
        rangeCheck(index);
        ensureCapacity(size + 2);
        int pos = (index + head) % value.length;
        if (pos == tail) {
            value[pos] = element;
            tail = (tail + 1) % value.length;
        } else if (pos == head) {
            head--;
            value[head] = element;
        } else {
            if (head < pos && tail < pos) {
                System.arraycopy(value, pos, value, pos - 1, pos - head + 1);
                value[pos] = element;
                head = (head - 1) % value.length;
            } else {
                System.arraycopy(value, pos, value, pos + 1, tail - pos);
                value[pos] = element;
                tail = (tail + 1 + value.length) % value.length;
            }
        }

        size++;
    }

    @Override
    public Object remove(int index) {
        modCount++;
        rangeCheck(index);
        int pos = (index + head) % value.length;

        Object ret = value[pos];
        value[pos] = null;
        size--;

        if (pos == head) {
            head = (head + 1) % value.length;
        } else if (pos == tail) {
            tail = (tail - 1 + value.length) % value.length;
        } else {
            if (pos > head && pos > tail) { // tail/head/pos
                System.arraycopy(value, head, value, head + 1,
                        pos - head);
                head = (head + 1) % value.length;
            } else {
                System.arraycopy(value, pos + 1, value, pos,
                        tail - pos - 1);
                tail = (tail - 1 + value.length) % value.length;
            }
        }
        return ret;
    }

    @Override
    public void clear() {
        modCount++;
        for (int i = head; i != tail; i = (i + 1) % value.length)
            value[i] = null;
        head = tail = size = 0;
    }

    @Override
    public boolean addAll(Collection c) {
        modCount++;
        int numNew = c.size();
        ensureCapacity(size + numNew + 2);
        Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            value[tail] = e.next();
            tail = (tail + 1) % value.length;
            size++;
        }
        return numNew != 0;
    }

}