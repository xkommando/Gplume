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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * wrapper for java.util.ArrayList
 * WARN: Do not modify this list by iterator or the list will be unordered
 * 
 * @author BowenCai
 *
 * @param <E>
 */
public class SortedArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = -5486145917221312801L;


    private final Comparator<? super E> comparator;
    
    public SortedArrayList() {
    	this(8, null);
    }
    
    public SortedArrayList(int initSz) {
    	this(initSz, null);
    }
    
    public SortedArrayList(Comparator<? super E> comp) {
    	this(8, comp);
    }
    
    public SortedArrayList(Collection<? extends E> c, Comparator<? super E> comp) {
    	this(8, comp);
    	this.addAll(c);
    }
    
    public SortedArrayList(int initSz, Comparator<? super E> comp) {
    	super(initSz);
    	if (comp != null) {
    		this.comparator = comp;
    	} else {
			this.comparator = new Comparator<E>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(E o1, E o2) {
					return ((Comparable<E>)o1).compareTo(o2);
				}
			};
		}
    }
    
    @Override
	public void add(int index, E element) {
    	throw new UnsupportedOperationException(
    			"cannot add value to specified position in the sorted list");
    }
    
    @Override
	public boolean add(E e) {
        int insertionPoint = Collections.binarySearch(this, e, comparator);
        super.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, e);
        return true;
    }
    
    @Override
	public E set(int index, E element) {
    	throw new UnsupportedOperationException("cannot set value of the sorted list");
    }
    
    @Override
	public boolean addAll(Collection<? extends E> c) {
        boolean result = false;
        if (c.size() > 8) {
            result = super.addAll(c);
            Collections.sort(this, comparator);
        }
        else {
            for (E e : c) {
                result |= add(e);
            }
        }
        return result;
    }

    @Override
	public boolean addAll(int index, Collection<? extends E> c) {
    	throw new UnsupportedOperationException(
    			"cannot add values to specified position in the sorted list");
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public int indexOf(Object o) {
    	return Collections.binarySearch(this, (E)o, comparator);
    }
    
}
