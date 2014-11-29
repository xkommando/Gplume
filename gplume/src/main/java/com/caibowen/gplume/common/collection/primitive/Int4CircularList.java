package com.caibowen.gplume.common.collection.primitive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author BowenCai
 * @since 27/11/2014.
 */
public class Int4CircularList implements Serializable {

    private int[] data;
    private int head = 0;
    private int tail = 0;

    private int size = 0;

    public Int4CircularList() {
        this(8);
    }

    public Int4CircularList(int size) {
        data = new int[size];
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity < oldCapacity)
            return;

        int newCapacity = (oldCapacity * 3) / 2 + 1;
        if (newCapacity < minCapacity)
            newCapacity = minCapacity;
        int newData[] = new int[newCapacity];
        toArray(newData);
        tail = size;
        head = 0;
        data = newData;
    }

    public int size() {
        return size;
    }

    public boolean contains(int elem) {
        return indexOf(elem) >= 0;
    }

    public int indexOf(int elem) {
        for (int i = 0; i < size; i++)
            if (elem == data[(i + head) % data.length])
                return i;
        return -1;
    }

    public int lastIndexOf(int elem) {
        for (int i = size - 1; i >= 0; i--)
            if (elem == data[(i + head) % data.length])
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
            System.arraycopy(data, head, a, 0, tail - head);
        } else {
            System.arraycopy(data, head, a, 0,
                    data.length - head);
            System.arraycopy(data, 0, a, data.length - head,
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
        return data[(index + head) % data.length];
    }

    public int front() {
        return data[head];
    }

    public int back() {
        return data[tail];
    }

    public int popBack() {
        int v = data[tail];
        data[tail] = 0;
        tail = (tail - 1 + data.length) % data.length;
        size--;
        return v;
    }

    public int popFront() {
        int v = data[head];
        data[head] = 0;
        head = (head + 1) % data.length;
        size--;
        return v;
    }

    public int set(int index, int element) {
        rangeCheck(index);
        int oldValue = data[(index + head) % data.length];
        data[(index + head) % data.length] = element;
        return oldValue;
    }

    public boolean add(int o) {
        ensureCapacity(size + 2);
        data[tail] = o;
        tail = (tail + 1) % data.length;
        size++;
        return true;
    }

    public void add(int index, int element) {
        rangeCheck(index);
        ensureCapacity(size + 2);
        int pos = (index + head) % data.length;
        if (pos == tail) {
            data[pos] = element;
            tail = (tail + 1) % data.length;
        } else if (pos == head) {
            head--;
            data[head] = element;
        } else {
            if (head < pos && tail < pos) {
                System.arraycopy(data, pos, data, pos - 1, pos - head + 1);
                data[pos] = element;
                head = (head - 1) % data.length;
            } else {
                System.arraycopy(data, pos, data, pos + 1, tail - pos);
                data[pos] = element;
                tail = (tail + 1 + data.length) % data.length;
            }
        }

        size++;
    }

    public int remove(int index) {
        rangeCheck(index);
        int pos = (index + head) % data.length;

        int ret = data[pos];
        size--;
        data[pos] = 0;
        if (pos == head) {
            head = (head + 1) % data.length;
        } else if (pos == tail) {
            tail = (tail - 1 + data.length) % data.length;
        } else {
            if (pos > head && pos > tail) { // tail/head/pos
                System.arraycopy(data, head, data, head + 1,
                        pos - head);
                head = (head + 1) % data.length;
            } else {
                System.arraycopy(data, pos + 1, data, pos,
                        tail - pos - 1);
                tail = (tail - 1 + data.length) % data.length;
            }
        }
        return ret;
    }

    public void clear() {
        for (int i = head; i != tail; i = (i + 1) % data.length)
            data[i] = 0;
        head = tail = size = 0;
    }

    public boolean addAll(Collection<Integer> c) {
        int numNew = c.size();
        ensureCapacity(size + numNew + 2);
        Iterator<Integer> e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            data[tail] = e.next();
            tail = (tail + 1) % data.length;
            size++;
        }
        return numNew != 0;
    }

    public String toString() {
        if (isEmpty())
            return "{}";

        StringBuilder sb = new StringBuilder(1024);
        sb.append("{ capacity:").append(data.length).append(", ")
                .append("size:").append(size).append(", ")
                .append("head:").append(head).append(", ")
                .append("tail").append(tail)
                .append(", [");

        for (int i = head; i != tail; i = (i + 1) % data.length) {
            int _t = data[i];
            if (_t != 0)
                sb.append(_t).append(',').append(' ');
        }
        return sb.append("] }").toString();
    }

}
