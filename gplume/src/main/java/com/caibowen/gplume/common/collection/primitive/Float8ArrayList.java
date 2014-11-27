package com.caibowen.gplume.common.collection.primitive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;


/**
 * @auther Bowen Cai
 *
 */
public class Float8ArrayList implements Serializable, Cloneable {

    private static final long serialVersionUID = -3694704272330932126L;

    /** The minimum size allowed when growth occurs */
    private static final int MIN_GROWTH_SIZE = 8;
    /** The amount the collection grows by when resized (3/2) */
    private static final int GROWTH_FACTOR_MULTIPLIER = 3;
    /** The amount the collection grows by when resized (3/2) */
    private static final int GROWTH_FACTOR_DIVISOR = 2;

    public double[] data;
    public int size;


    public Float8ArrayList() {
    }

    /**
     * Constructor that defines an initial size for the internal storage array.
     *
     * @param initialSize  the initial size of the internal array, negative treated as zero
     */
    public Float8ArrayList(int initialSize) {
        data = new double[initialSize];
    }

    /**
     * Constructor that copies the specified values.
     *
     * @param values  an array of values to copy, null treated as zero size array
     */
    public Float8ArrayList(double[] values) {
        data = (double[]) values.clone();
        size = values.length;
    }

    /**
     * Constructor that copies the specified values.
     *
     * @param coll  a collection of values to copy, null treated as zero size collection
     */
    public Float8ArrayList(Collection<Long> coll) {
        if (coll instanceof Float8ArrayList) {
            Float8ArrayList c = (Float8ArrayList) coll;
            if (c.size > 0) {
                this.data = new double[c.size];
                System.arraycopy(c.data, 0, this.data, 0, c.size);
            }
            size = c.size;
        } else {
            int i = 0;
            size = coll.size();
            data = new double[size];
            for (Iterator<?> it = coll.iterator(); it.hasNext(); i++) {
                Integer value = (Integer) it.next();
                data[i] = value.longValue();
            }
        }
    }

    // Implementation
    //-----------------------------------------------------------------------
    /**
     * Gets the current size of the collection.
     *
     * @return the current size
     */
    public int size() {
        return size;
    }

    /**
     * Gets the primitive value at the specified index.
     *
     * @param index  the index to at from
     * @return value at the index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public double at(int index) {
        return data[index];
    }

    /**
     * Adds a primitive value to this collection.
     *
     * @param index  the index to insert at
     * @param value  the value to pushBack to this collection
     * @return <code>true</code> if this collection was modified by this method call
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public void insert(int index, double value) {
        ensureCapacity(size + 1);
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = value;
        size++;
    }

    public void pushBack(double val) {
        ensureCapacity(size + 1);
        data[size++] = val;
    }

    public double popAt(int index) {
        double result = data[index];
        System.arraycopy(data, index + 1, data, index, size - 1 - index);
        size--;
        return result;
    }

    public double popBack() {
        return data[--size];
    }

    public double front() {
        return data[0];
    }
    public double back() {
        return data[size - 1];
    }
    /**
     * Removes a range of values from the list.
     *
     * @param fromIndexInclusive  the start of the range to remove, inclusive
     * @param toIndexExclusive  the end of the range to remove, exclusive
     * @return <code>true</code> if the collection was modified
     */
    public void removeRange(int fromIndexInclusive, int toIndexExclusive) {
        System.arraycopy(data, toIndexExclusive, data, fromIndexInclusive, size - toIndexExclusive);
        size -= (toIndexExclusive - fromIndexInclusive);
    }

    /**
     * Sets the primitive value at a specified index.
     *
     * @param index  the index to set
     * @param value  the value to store
     * @return the previous value at the index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public double set(int index, double value) {
        double result = data[index];
        data[index] = value;
        return result;
    }

    // Overrides
    //-----------------------------------------------------------------------
    /**
     * Optimizes the implementation.
     * <p>
     * This implementation changes the internal array to be the same size as
     * the size of the collection.
     */
    public void trim() {
        if (size < data.length) {
            double[] array = new double[size];
            System.arraycopy(data, 0, array, 0, size);
            data = array;
        }
    }

    /**
     * Clears the collection/map of all elements.
     * <p>
     * This implementation resets the size, but does not reduce the internal storage array.
     * <p>
     * The collection/map will have a zero size after this method completes.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Checks whether this collection contains a specified primitive value.
     * <p>
     * This implementation accesses the internal storage array directly.
     *
     * @param value  the value to search for
     * @return <code>true</code> if the value is found
     */
    public boolean contains(double value) {
        for (int i = 0; i < size; i++) {
            if (data[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an array of primitive values to this collection at a specified index.
     *
     * @param index  the index to pushBack at
     * @param values  the values to pushBack to this collection
     * @return <code>true</code> if this collection was modified by this method call
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public boolean addAll(int index, double[] values) {
        if (values == null || values.length == 0) {
            return false;
        }
        int len = values.length;
        ensureCapacity(size + len);
        System.arraycopy(data, index, data, index + len, size - index);
        System.arraycopy(values, 0, data, index, len);
        size += len;
        return true;
    }

    /**
     * Clone implementation that calls Object clone().
     *
     * @return the clone
     */
    public Object clone() throws CloneNotSupportedException {
        Float8ArrayList cloned = (Float8ArrayList) super.clone();
        cloned.data = (double[]) data.clone();
        return cloned;
    }


    // Internal implementation
    //-----------------------------------------------------------------------
    /**
     * Ensures that the internal storage array has at least the specified size.
     *
     * @param capacity  the amount to expand to
     */
    protected void ensureCapacity(int capacity) {
        Deque a;
        int len = data.length;
        int newLen = len * GROWTH_FACTOR_MULTIPLIER / GROWTH_FACTOR_DIVISOR + 1;
        if (newLen < capacity) {
            newLen = capacity;
        }
        if (newLen < MIN_GROWTH_SIZE) {
            newLen = MIN_GROWTH_SIZE;
        }
        double[] newArray = new double[newLen];
        System.arraycopy(data, 0, newArray, 0, len);
        data = newArray;
    }

}
    