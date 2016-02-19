/*******************************************************************************
 * Copyright 2014 Bowen Cai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.caibowen.gplume.common.collection;

import java.io.Serializable;
import java.util.*;


/**
 *  set wrapper for a one dimension array.
 *
 *
 *  WARN:
 *  	There is no guarantee that value in this set is unique, and values are nullable.
 *
 * @author bowen.cbw@alibaba-inc.com
 *
 */
public class ImmutableArraySet<V> implements Set<V>, Cloneable, Serializable {
	
	private static final long serialVersionUID = 753227636731523250L;
	
	private final Object[] value;

	public ImmutableArraySet(Object[] value) {
		this.value = value;
	}

	public ImmutableArraySet(Set<V> jdkSet) {
		value = new Object[jdkSet.size()];
		int i = 0;
		for (V v : jdkSet) {
			value[i] = v;
			i++;
		}
	}

	public TreeSet<V> toTreeSet() {
		return (TreeSet<V>) addTo(new TreeSet<V>());
	}

	public HashSet<V> toHashSet() {
		return (HashSet<V>) addTo(new HashSet<V>(value.length));
	}

	public Set<V> addTo(Set<V> jdkSet) {
		for (int i = 0; i != value.length; i++) {
			jdkSet.add((V)value[i]);
		}
		return jdkSet;
	}

	@Override
	public int size() {
		return value.length;
	}

	@Override
	public boolean isEmpty() {
		return value.length == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) {
			for (Object object : value) {
				if (object == null)
					return true;
			}
		}else {
			for (Object object : value) {
				if (object != null && o.equals(object))
					return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<V> iterator() {
		return new Iterator<V>() {
            private int idx = 0;
            public boolean hasNext() {return idx < value.length;}
            public V next()          {return (V)value[idx++];}
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
	}

	/**
	 * returns a array which is a copy of this set
     * element within this set will not be copied
	 */
	@Override
	public Object[] toArray() {
		Object[] cp = new Object[value.length];
		System.arraycopy(value, 0, cp, 0, value.length);
		return cp;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < value.length)
			return (T[])toArray();
		System.arraycopy(value, 0, a, 0, value.length);
		if (a.length > value.length)
			a[value.length] = null;
		return a;
	}

	@Override
	public boolean add(V e) {
        throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
        throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
	}

	@Override
	public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
        throw new UnsupportedOperationException();
	}

	/**
	 *
	 * @return json string representing the set
     */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(256);
		b.append("\"").append(super.toString()).append(" : [");
		boolean added = false;
		for (Object object : value) {
			b.append('\"').append(object).append("\", ");
			added = true;
		}
		if (added) {
			final int len = b.length();
			b.delete(len - 2, len);
		}
		b.append(']');
		return b.toString();
	}
	
	
	@Override
	public int hashCode() {
        int result = 0;
        for (Object element : value)
            result += (element == null ? 0 : element.hashCode());
        return result;
//        throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (! (obj instanceof Set))
			return false;
        Collection c = (Collection) obj;
        if (c.size() != size())
            return false;
        try {
            return containsAll(c);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
	}

	/**
	 * returns a new set which is a copy of this set 
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("this exception will never throw");
		}
	}
}
