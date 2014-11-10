package com.caibowen.gplume.cache.mem;

import com.caibowen.gplume.misc.Assert;

import java.util.*;

/**
 * @author BowenCai
 * @param <V> the value type
 */
public class Int4LIRSCache<V> {

    /**
     * The maximum memory this cache should use.
     */
    private int maxMemory;

    /**
     * The average memory used by one entry.
     */
    private int averageMemory;

    private final Segment<V>[] segments;

    private final int segmentCount;
    private final int segmentShift;
    private final int segmentMask;
    private final int stackMoveDistance;

    /**
     * Create a new cache with the given number of entries, and the default
     * settings (an average size of 1 per entry, 16 segments, and stack move
     * distance equals to the maximum number of entries divided by 100).
     *
     * @param maxMemory the maximum number of entries
     */
    public Int4LIRSCache(int maxMemory) {
        this(maxMemory, 1, 16, maxMemory / 100);
    }

    public Int4LIRSCache(int maxMemory, int averageMemory) {
        this(maxMemory, averageMemory, 16, maxMemory / averageMemory / 100);
    }

    /**
     * Create a new cache with the given memory size.
     *
     * @param maxMemory the maximum memory to use (1 or larger)
     * @param averageMemory the average memory (1 or larger)
     * @param segmentCount the number of cache segments (must be a power of 2)
     * @param stackMoveDistance how many other item are to be moved to the top
     *        of the stack before the current item is moved
     */
    public Int4LIRSCache(int maxMemory, int averageMemory,
                         int segmentCount, int stackMoveDistance) {
        Assert.isTrue(
                Integer.bitCount(segmentCount) == 1,
                "The segment count must be a power of 2, is " + segmentCount);
        setMaxMemory(maxMemory);
        setAverageMemory(averageMemory);

        this.segmentCount = segmentCount;
        this.segmentMask = segmentCount - 1;
        this.stackMoveDistance = stackMoveDistance;
        segments = new Segment[segmentCount];
        clear();
        // use the high bits for the segment
        this.segmentShift = 32 - Integer.bitCount(segmentMask);
    }

    /**
     * Remove all entries.
     */
    public void clear() {
        int max = Math.max(1, maxMemory / segmentCount);
        int segmentLen = getSegmentLen(max);
        for (int i = 0; i < segmentCount; i++) {
            segments[i] = new Segment<V>(
                    max, segmentLen, stackMoveDistance);
        }
    }

    private int getSegmentLen(long max) {
        // calculate the size of the map array
        // assume a fill factor of at most 75%
        long maxLen = (long) (max / averageMemory / 0.75);
        // the size needs to be a power of 2
        long l = 8;
        while (l < maxLen) {
            l += l;
        }
        // the array size is at most 2^31 elements
        return (int) Math.min(1L << 31, l);
    }

    private Entry<V> find(int key) {
        return getSegment(key).find(key);
    }

    /**
     * Check whether there is a resident entry for the given key. This
     * method does not adjust the internal state of the cache.
     *
     * @param key the key (may not be null)
     * @return true if there is a resident entry
     */
    public boolean containsKey(int key) {
        return getSegment(key).containsKey(key);
    }

    /**
     * Get the value for the given key if the entry is cached. This method does
     * not modify the internal state.
     *
     * @param key the key (may not be null)
     * @return the value, or null if there is no resident entry
     */
    public V peek(int key) {
        Entry<V> e = find(key);
        return e == null ? null : e.value;
    }

    /**
     * Add an entry to the cache using the average memory size.
     *
     * @param key the key (may not be null)
     * @param value the value (may not be null)
     * @return the old value, or null if there was no resident entry
     */
    public V put(int key, V value) {
        return put(key, value, averageMemory);
    }

    /**
     * Add an entry to the cache. The entry may or may not exist in the
     * cache yet. This method will usually mark unknown entries as cold and
     * known entries as hot.
     *
     * @param key the key (may not be null)
     * @param value the value (may not be null)
     * @param memory the memory used for the given entry
     * @return the old value, or null if there was no resident entry
     */
    public V put(int key, V value, int memory) {
        int segmentIndex = (key >>> segmentShift) & segmentMask;
        Segment<V> s = segments[segmentIndex];
        // check whether resize is required:
        // synchronize on s, to avoid concurrent writes also
        // resize (concurrent reads read from the old segment)
        synchronized (s) {
            if (s.isFull()) {
                // another thread might have resized
                // (as we retrieved the segment before synchronizing on it)
                s = segments[segmentIndex];
                if (s.isFull()) {
                    s = new Segment<V>(s, 2);
                    segments[segmentIndex] = s;
                }
            }
            return s.put(key, value, memory);
        }
    }


    /**
     * Remove an entry. Both resident and non-resident entries can be
     * removed.
     *
     * @param key the key (may not be null)
     * @return the old value, or null if there was no resident entry
     */
    public V remove(int key) {
        return getSegment(key).remove(key);
    }

    /**
     * Get the memory used for the given key.
     *
     * @param key the key (may not be null)
     * @return the memory, or 0 if there is no resident entry
     */
    public int getMemory(int key) {
        return getSegment(key).getMemory(key);
    }

    /**
     * Get the value for the given key if the entry is cached. This method
     * adjusts the internal state of the cache sometimes, to ensure commonly
     * used entries stay in the cache.
     *
     * @param key the key (may not be null)
     * @return the value, or null if there is no resident entry
     */
    public V get(int key) {
        return getSegment(key).get(key);
    }

    private Segment<V> getSegment(int hash) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }


    /**
     * Get the currently used memory.
     *
     * @return the used memory
     */
    public int getUsedMemory() {
        int x = 0;
        for (Segment<V> s : segments) {
            x += s.usedMemory;
        }
        return x;
    }

    /**
     * Set the maximum memory this cache should use. This will not
     * immediately cause entries to get removed however; it will only change
     * the limit. To resize the internal array, call the clear method.
     *
     * @param maxMemory the maximum size (1 or larger)
     */
    public void setMaxMemory(int maxMemory) {
        Assert.isTrue(
                maxMemory > 0,
                "Max memory must be larger than 0, is " +  maxMemory);
        this.maxMemory = maxMemory;
        if (segments != null) {
            int max = 1 + maxMemory / segments.length;
            for (Segment<V> s : segments) {
                s.setMaxMemory(max);
            }
        }
    }

    /**
     * Set the average memory used per entry. It is used to calculate the
     * length of the internal array.
     *
     * @param averageMemory the average memory used (1 or larger)
     */
    public void setAverageMemory(int averageMemory) {
        Assert.isTrue(
                averageMemory > 0,
                "Average memory must be larger than 0, is " + averageMemory);
        this.averageMemory = averageMemory;
    }

    /**
     * Get the average memory used per entry.
     *
     * @return the average memory
     */
    public int getAverageMemory() {
        return averageMemory;
    }

    /**
     * Get the maximum memory to use.
     *
     * @return the maximum memory
     */
    public int getMaxMemory() {
        return maxMemory;
    }

    /**
     * Get the entry set for all resident entries.
     *
     * @return the entry set
     */
    public synchronized Set<Map.Entry<Integer, V>> entrySet() {
        HashMap<Integer, V> map = new HashMap<Integer, V>();
        for (int k : keySet()) {
            map.put(k,  find(k).value);
        }
        return map.entrySet();
    }

    /**
     * Get the set of keys for resident entries.
     *
     * @return the set of keys
     */
    public synchronized Set<Integer> keySet() {
        HashSet<Integer> set = new HashSet<Integer>();
        for (Segment<V> s : segments) {
            set.addAll(s.keySet());
        }
        return set;
    }

    /**
     * Get the number of non-resident entries in the cache.
     *
     * @return the number of non-resident entries
     */
    public int sizeNonResident() {
        int x = 0;
        for (Segment<V> s : segments) {
            x += s.queue2Size;
        }
        return x;
    }

    /**
     * Get the length of the internal map array.
     *
     * @return the size of the array
     */
    public int sizeMapArray() {
        int x = 0;
        for (Segment<V> s : segments) {
            x += s.entries.length;
        }
        return x;
    }

    /**
     * Get the number of hot entries in the cache.
     *
     * @return the number of hot entries
     */
    public int sizeHot() {
        int x = 0;
        for (Segment<V> s : segments) {
            x += s.mapSize - s.queueSize - s.queue2Size;
        }
        return x;
    }

    /**
     * Get the number of resident entries.
     *
     * @return the number of entries
     */
    public int size() {
        int x = 0;
        for (Segment<V> s : segments) {
            x += s.mapSize - s.queue2Size;
        }
        return x;
    }

    /**
     * Get the list of keys. This method allows to read the internal state of
     * the cache.
     *
     * @param cold if true, only keys for the cold entries are returned
     * @param nonResident true for non-resident entries
     * @return the key list
     */
    public synchronized List<Integer> keys(boolean cold, boolean nonResident) {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        for (Segment<V> s : segments) {
            keys.addAll(s.keys(cold, nonResident));
        }
        return keys;
    }

    /**
     * Get the values for all resident entries.
     *
     * @return the entry set
     */
    public List<V> values() {
        ArrayList<V> list = new ArrayList<V>();
        for (int k : keySet()) {
            V value = find(k).value;
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * Check whether the cache is empty.
     *
     * @return true if it is empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Check whether the given value is stored.
     *
     * @param value the value
     * @return true if it is stored
     */
    public boolean containsValue(Object value) {
        return getMap().containsValue(value);
    }

    /**
     * Convert this cache to a map.
     *
     * @return the map
     */
    public Map<Integer, V> getMap() {
        HashMap<Integer, V> map = new HashMap<Integer, V>();
        for (int k : keySet()) {
            V x = find(k).value;
            if (x != null) {
                map.put(k, x);
            }
        }
        return map;
    }

    /**
     * Add all elements of the map to this cache.
     *
     * @param m the map
     */
    public void putAll(Map<Integer, ? extends V> m) {
        for (Map.Entry<Integer, ? extends V> e : m.entrySet()) {
            // copy only non-null entries
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * A cache segment
     *
     * @param <V> the value type
     */
    private static class Segment<V> {

        /**
         * The number of (hot, cold, and non-resident) entries in the map.
         */
        int mapSize;

        /**
         * The size of the LIRS queue for resident cold entries.
         */
        int queueSize;

        /**
         * The size of the LIRS queue for non-resident cold entries.
         */
        int queue2Size;

        /**
         * The map array. The size is always a power of 2.
         */
        final Entry<V>[] entries;

        /**
         * The currently used memory.
         */
        int usedMemory;

        /**
         * How many other item are to be moved to the top of the stack before
         * the current item is moved.
         */
        private final int stackMoveDistance;

        /**
         * The maximum memory this cache should use.
         */
        private int maxMemory;

        /**
         * The bit mask that is applied to the key hash code to get the index in
         * the map array. The mask is the length of the array minus one.
         */
        private int mask;

        /**
         * The LIRS stack size.
         */
        private int stackSize;

        /**
         * The stack of recently referenced elements. This includes all hot
         * entries, the recently referenced cold entries, and all non-resident
         * cold entries.
         * <p>
         * There is always at least one entry: the head entry.
         */
        private Entry<V> stack;

        /**
         * The queue of resident cold entries.
         * <p>
         * There is always at least one entry: the head entry.
         */
        private Entry<V> queue;

        /**
         * The queue of non-resident cold entries.
         * <p>
         * There is always at least one entry: the head entry.
         */
        private Entry<V> queue2;

        /**
         * The number of times any item was moved to the top of the stack.
         */
        private int stackMoveCounter;

        /**
         * Create a new cache segment.
         *
         * @param maxMemory the maximum memory to use
         * @param len the number of hash table buckets (must be a power of 2)
         * @param stackMoveDistance the number of other entries to be moved to
         *        the top of the stack before moving an entry to the top
         */
        Segment(int maxMemory, int len, int stackMoveDistance) {
            setMaxMemory(maxMemory);
            this.stackMoveDistance = stackMoveDistance;

            // the bit mask has all bits set
            mask = len - 1;

            // initialize the stack and queue heads
            stack = new Entry<V>();
            stack.stackPrev = stack.stackNext = stack;
            queue = new Entry<V>();
            queue.queuePrev = queue.queueNext = queue;
            queue2 = new Entry<V>();
            queue2.queuePrev = queue2.queueNext = queue2;

            @SuppressWarnings("unchecked")
            Entry<V>[] e = new Entry[len];
            entries = e;

            mapSize = 0;
            usedMemory = 0;
            stackSize = queueSize = queue2Size = 0;
        }

        /**
         * Create a new, larger cache segment from an existing one.
         * The caller must synchronize on the old segment, to avoid
         * concurrent modifications.
         *
         * @param old the old segment
         * @param resizeFactor the factor to use to calculate the number of hash
         *            table buckets (must be a power of 2)
         */
        Segment(Segment<V> old, int resizeFactor) {
            this(old.maxMemory,
                    old.entries.length * resizeFactor,
                    old.stackMoveDistance);
            Entry<V> s = old.stack.stackPrev;
            while (s != old.stack) {
                Entry<V> e = copy(s);
                addToMap(e);
                addToStack(e);
                s = s.stackPrev;
            }
            s = old.queue.queuePrev;
            while (s != old.queue) {
                Entry<V> e = find(s.key);
                if (e == null) {
                    e = copy(s);
                    addToMap(e);
                }
                addToQueue(queue, e);
                s = s.queuePrev;
            }
            s = old.queue2.queuePrev;
            while (s != old.queue2) {
                Entry<V> e = find(s.key);
                if (e == null) {
                    e = copy(s);
                    addToMap(e);
                }
                addToQueue(queue2, e);
                s = s.queuePrev;
            }
        }

        private void addToMap(Entry<V> e) {
            int index = e.key & mask;
            e.mapNext = entries[index];
            entries[index] = e;
            usedMemory += e.memory;
            mapSize++;
        }

        private static <V> Entry<V> copy(Entry<V> old) {
            Entry<V> e = new Entry<V>();
            e.key = old.key;
            e.value = old.value;
            e.memory = old.memory;
            e.topMove = old.topMove;
            return e;
        }

        /**
         * Check whether the cache segment is full.
         *
         * @return true if it contains more entries than hash table buckets.
         */
        public boolean isFull() {
            return mapSize > mask;
        }

        /**
         * Get the memory used for the given key.
         *
         * @param key the key (may not be null)
         * @return the memory, or 0 if there is no resident entry
         */
        int getMemory(int key) {
            Entry<V> e = find(key);
            return e == null ? 0 : e.memory;
        }

        /**
         * Get the value for the given key if the entry is cached. This method
         * adjusts the internal state of the cache sometimes, to ensure commonly
         * used entries stay in the cache.
         *
         * @param key the key (may not be null)
         * @return the value, or null if there is no resident entry
         */
        V get(int key) {
            Entry<V> e = find(key);
            if (e == null) {
                // the entry was not found
                return null;
            }
            V value = e.value;
            if (value == null) {
                // it was a non-resident entry
                return null;
            }
            if (e.isHot()) {
                if (e != stack.stackNext) {
                    if (stackMoveDistance == 0 ||
                            stackMoveCounter - e.topMove > stackMoveDistance) {
                        accessHot(e);
                    }
                }
            } else {
                access(e);
            }
            return value;
        }

        /**
         * Access an item, moving the entry to the top of the stack or front of
         * the queue if found.
         *
         */
        private synchronized void accessHot(Entry<V> e) {
            if (e != stack.stackNext) {
                if (stackMoveDistance == 0 ||
                        stackMoveCounter - e.topMove > stackMoveDistance) {
                    // move a hot entry to the top of the stack
                    // unless it is already there
                    boolean wasEnd = e == stack.stackPrev;
                    removeFromStack(e);
                    if (wasEnd) {
                        // if moving the last entry, the last entry
                        // could now be cold, which is not allowed
                        pruneStack();
                    }
                    addToStack(e);
                }
            }
        }
        private synchronized void access(Entry<V> e) {
            removeFromQueue(e);
            if (e.stackNext != null) {
                // resident cold entries become hot
                // if they are on the stack
                removeFromStack(e);
                // which means a hot entry needs to become cold
                // (this entry is cold, that means there is at least one
                // more entry in the stack, which must be hot)
                convertOldestHotToCold();
            } else {
                // cold entries that are not on the stack
                // move to the front of the queue
                addToQueue(queue, e);
            }
            // in any case, the cold entry is moved to the top of the stack
            addToStack(e);

        }

        /**
         * Add an entry to the cache. The entry may or may not exist in the
         * cache yet. This method will usually mark unknown entries as cold and
         * known entries as hot.
         *
         * @param key the key (may not be null)
         * @param value the value (may not be null)
         * @param memory the memory used for the given entry
         * @return the old value, or null if there was no resident entry
         */
        synchronized V put(int key, V value, int memory) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "The value may not be null");
            }
            V old;
            Entry<V> e = find(key);
            if (e == null) {
                old = null;
            } else {
                old = e.value;
                remove(key);
            }
            e = new Entry<V>();
            e.key = key;
            e.value = value;
            e.memory = memory;
            int index = key & mask;
            e.mapNext = entries[index];
            entries[index] = e;
            usedMemory += memory;
            if (usedMemory > maxMemory && mapSize > 0) {
                // an old entry needs to be removed
                evict(e);
            }
            mapSize++;
            // added entries are always added to the stack
            addToStack(e);
            return old;
        }

        /**
         * Remove an entry. Both resident and non-resident entries can be
         * removed.
         *
         * @param key the key (may not be null)
         * @return the old value, or null if there was no resident entry
         */
        synchronized V remove(int key) {
            int index = key & mask;
            Entry<V> e = entries[index];
            if (e == null) {
                return null;
            }
            V old;
            if (e.key == key) {
                old = e.value;
                entries[index] = e.mapNext;
            } else {
                Entry<V> last;
                do {
                    last = e;
                    e = e.mapNext;
                    if (e == null) {
                        return null;
                    }
                } while (e.key != key);
                old = e.value;
                last.mapNext = e.mapNext;
            }
            mapSize--;
            usedMemory -= e.memory;
            if (e.stackNext != null) {
                removeFromStack(e);
            }
            if (e.isHot()) {
                // when removing a hot entry, the newest cold entry gets hot,
                // so the number of hot entries does not change
                e = queue.queueNext;
                if (e != queue) {
                    removeFromQueue(e);
                    if (e.stackNext == null) {
                        addToStackBottom(e);
                    }
                }
            } else {
                removeFromQueue(e);
            }
            pruneStack();
            return old;
        }

        /**
         * Evict cold entries (resident and non-resident) until the memory limit
         * is reached. The new entry is added as a cold entry, except if it is
         * the only entry.
         *
         * @param newCold a new cold entry
         */
        private void evict(Entry<V> newCold) {
            // ensure there are not too many hot entries: right shift of 5 is
            // division by 32, that means if there are only 1/32 (3.125%) or
            // less cold entries, a hot entry needs to become cold
            while (queueSize <= (mapSize >>> 5) && stackSize > 0) {
                convertOldestHotToCold();
            }
            if (stackSize > 0) {
                // the new cold entry is at the top of the queue
                addToQueue(queue, newCold);
            }
            // the oldest resident cold entries become non-resident
            // but at least one cold entry (the new one) must stay
            while (usedMemory > maxMemory && queueSize > 1) {
                Entry<V> e = queue.queuePrev;
                usedMemory -= e.memory;
                removeFromQueue(e);
                e.value = null;
                e.memory = 0;
                addToQueue(queue2, e);
                // the size of the non-resident-cold entries needs to be limited
                while (queue2Size + queue2Size > stackSize) {
                    e = queue2.queuePrev;
                    remove(e.key);
                }
            }
        }

        private void convertOldestHotToCold() {
            // the last entry of the stack is known to be hot
            Entry<V> last = stack.stackPrev;
            if (last == stack) {
                // never remove the stack head itself (this would mean the
                // internal structure of the cache is corrupt)
                throw new IllegalStateException();
            }
            // remove from stack - which is done anyway in the stack pruning,
            // but we can do it here as well
            removeFromStack(last);
            // adding an entry to the queue will make it cold
            addToQueue(queue, last);
            pruneStack();
        }

        /**
         * Ensure the last entry of the stack is cold.
         */
        private void pruneStack() {
            while (true) {
                Entry<V> last = stack.stackPrev;
                // must stop at a hot entry or the stack head,
                // but the stack head itself is also hot, so we
                // don't have to test it
                if (last.isHot()) {
                    break;
                }
                // the cold entry is still in the queue
                removeFromStack(last);
            }
        }

        /**
         * Try to find an entry in the map.
         *
         * @param key the hash
         * @return the entry (might be a non-resident)
         */
        Entry<V> find(int key) {
            int index = key & mask;
            Entry<V> e = entries[index];
            while (e != null && e.key != key) {
                e = e.mapNext;
            }
            return e;
        }

        private void addToStack(Entry<V> e) {
            e.stackPrev = stack;
            e.stackNext = stack.stackNext;
            e.stackNext.stackPrev = e;
            stack.stackNext = e;
            stackSize++;
            e.topMove = stackMoveCounter++;
        }

        private void addToStackBottom(Entry<V> e) {
            e.stackNext = stack;
            e.stackPrev = stack.stackPrev;
            e.stackPrev.stackNext = e;
            stack.stackPrev = e;
            stackSize++;
        }

        /**
         * Remove the entry from the stack. The head itself must not be removed.
         *
         * @param e the entry
         */
        private void removeFromStack(Entry<V> e) {
            e.stackPrev.stackNext = e.stackNext;
            e.stackNext.stackPrev = e.stackPrev;
            e.stackPrev = e.stackNext = null;
            stackSize--;
        }

        private void addToQueue(Entry<V> q, Entry<V> e) {
            e.queuePrev = q;
            e.queueNext = q.queueNext;
            e.queueNext.queuePrev = e;
            q.queueNext = e;
            if (e.value != null) {
                queueSize++;
            } else {
                queue2Size++;
            }
        }

        private void removeFromQueue(Entry<V> e) {
            e.queuePrev.queueNext = e.queueNext;
            e.queueNext.queuePrev = e.queuePrev;
            e.queuePrev = e.queueNext = null;
            if (e.value != null) {
                queueSize--;
            } else {
                queue2Size--;
            }
        }

        /**
         * Get the list of keys. This method allows to read the internal state
         * of the cache.
         *
         * @param cold if true, only keys for the cold entries are returned
         * @param nonResident true for non-resident entries
         * @return the key list
         */
        synchronized List<Integer> keys(boolean cold, boolean nonResident) {
            ArrayList<Integer> keys = new ArrayList<Integer>();
            if (cold) {
                Entry<V> start = nonResident ? queue2 : queue;
                for (Entry<V> e = start.queueNext; e != start;
                     e = e.queueNext) {
                    keys.add(e.key);
                }
            } else {
                for (Entry<V> e = stack.stackNext; e != stack;
                     e = e.stackNext) {
                    keys.add(e.key);
                }
            }
            return keys;
        }

        /**
         * Check whether there is a resident entry for the given key. This
         * method does not adjust the internal state of the cache.
         *
         * @param hash the hash
         * @return true if there is a resident entry
         */
        boolean containsKey(int hash) {
            Entry<V> e = find(hash);
            return e != null && e.value != null;
        }

        /**
         * Get the set of keys for resident entries.
         *
         * @return the set of keys
         */
        synchronized Set<Integer> keySet() {
            HashSet<Integer> set = new HashSet<Integer>();
            for (Entry<V> e = stack.stackNext; e != stack; e = e.stackNext) {
                set.add(e.key);
            }
            for (Entry<V> e = queue.queueNext; e != queue; e = e.queueNext) {
                set.add(e.key);
            }
            return set;
        }

        /**
         * Set the maximum memory this cache should use. This will not
         * immediately cause entries to get removed however; it will only change
         * the limit. To resize the internal array, call the clear method.
         *
         * @param maxMemory the maximum size (1 or larger)
         */
        void setMaxMemory(int maxMemory) {
            this.maxMemory = maxMemory;
        }

    }

    /**
     * A cache entry. Each entry is either hot (low inter-reference recency;
     * LIR), cold (high inter-reference recency; HIR), or non-resident-cold. Hot
     * entries are in the stack only. Cold entries are in the queue, and may be
     * in the stack. Non-resident-cold entries have their value set to null and
     * are in the stack and in the non-resident queue.
     *
     * @param <V> the value type
     */
    static class Entry<V> {

        /**
         * The key.
         */
        int key;

        /**
         * The value. Set to null for non-resident-cold entries.
         */
        V value;

        /**
         * The estimated memory used.
         */
        int memory;

        /**
         * When the item was last moved to the top of the stack.
         */
        int topMove;

        /**
         * The next entry in the stack.
         */
        Entry<V> stackNext;

        /**
         * The previous entry in the stack.
         */
        Entry<V> stackPrev;

        /**
         * The next entry in the queue (either the resident queue or the
         * non-resident queue).
         */
        Entry<V> queueNext;

        /**
         * The previous entry in the queue.
         */
        Entry<V> queuePrev;

        /**
         * The next entry in the map (the chained entry).
         */
        Entry<V> mapNext;

        /**
         * Whether this entry is hot. Cold entries are in one of the two queues.
         *
         * @return whether the entry is hot
         */
        boolean isHot() {
            return queueNext == null;
        }

    }

}
