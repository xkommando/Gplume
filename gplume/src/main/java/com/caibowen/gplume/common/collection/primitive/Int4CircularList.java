package com.caibowen.gplume.common.collection.primitive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author BowenCai
 * @since 27/11/2014.
 */
public class Int4CircularList implements Serializable {

    private int[] value;
    private int head = 0;
    private int tail = 0;

    private int size = 0;

    public Int4CircularList() {
        this(8);
    }

    public Int4CircularList(int size) {
        value = new int[size];
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = value.length;
        if (minCapacity < oldCapacity)
            return;

        int newCapacity = (oldCapacity * 3) / 2 + 1;
        if (newCapacity < minCapacity)
            newCapacity = minCapacity;
        int newData[] = new int[newCapacity];
        toArray(newData);
        tail = size;
        head = 0;
        value = newData;
    }

    public int size() {
        return size;
    }

    public boolean contains(int elem) {
        return indexOf(elem) >= 0;
    }

    public int indexOf(int elem) {

        for (int i = 0; i < size; i++)
            if (elem == value[(i + head) % value.length])
                return i;

        return -1;
    }

    public int lastIndexOf(int elem) {
        for (int i = size - 1; i >= 0; i--)
            if (elem == value[(i + head) % value.length])
                return i;
        return -1;
    }

    public int[] toArray() {
        return toArray(new int[size]);
    }

    public int[] toArray(int a[]) {
        if (a.length < size)
            a = (int[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        if (head < tail) {
            System.arraycopy(value, head, a, 0, tail - head);
        } else {
            System.arraycopy(value, head, a, 0,
                    value.length - head);
            System.arraycopy(value, 0, a, value.length - head,
                    tail);
        }
        return a;
    }

    private void rangeCheck(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
    }

    public int get(int index) {
        rangeCheck(index);
        return value[(index + head) % value.length];
    }

    public int front() {
        return value[head];
    }

    public int back() {
        return value[tail];
    }

    public int popBack() {
        int v = value[tail];
        value[tail] = 0;
        tail = (tail - 1 + value.length) % value.length;
        size--;
        return v;
    }

    public int popFront() {
        int v = value[head];
        value[head] = 0;
        head = (head + 1) % value.length;
        size--;
        return v;
    }

    public int set(int index, int element) {
        rangeCheck(index);
        int oldValue = value[(index + head) % value.length];
        value[(index + head) % value.length] = element;
        return oldValue;
    }

    public boolean add(int o) {
        ensureCapacity(size + 2);
        value[tail] = o;
        tail = (tail + 1) % value.length;
        size++;
        return true;
    }

    public void add(int index, int element) {
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

    public int remove(int index) {
        rangeCheck(index);
        int pos = (index + head) % value.length;

        int ret = value[pos];
        size--;
        value[pos] = 0;
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

    public void clear() {
        for (int i = head; i != tail; i = (i + 1) % value.length)
            value[i] = 0;
        head = tail = size = 0;
    }

    public boolean addAll(Collection<Integer> c) {
        int numNew = c.size();
        ensureCapacity(size + numNew + 2);
        Iterator<Integer> e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            value[tail] = e.next();
            tail = (tail + 1) % value.length;
            size++;
        }
        return numNew != 0;
    }

    public String toString() {
        if (isEmpty())
            return "{}";

        StringBuilder sb = new StringBuilder(1024);
        sb.append("{ capacity:").append(value.length).append(", ")
                .append("size:").append(size).append(", ")
                .append("head:").append(head).append(", ")
                .append("tail").append(tail)
                .append(", [");

        for (int i = head; i != tail; i = (i + 1) % value.length) {
            int _t = value[i];
            if (_t != 0)
                sb.append(_t).append(',').append(' ');
        }
        return sb.append("] }").toString();
    }

}
