/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.common;


import java.io.Serializable;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author BowenCai
 *
 * @param <K>
 * @param <V>
 */
public final class SimpleCache<K,V> implements Serializable {

	private static final long serialVersionUID = 3159753264696995816L;
	
	
	private final int size;

    private final ConcurrentHashMap<K,V> eden;
    private transient WeakHashMap<K,V> longterm;
    
    public SimpleCache(int size) {
        this.size = size;
        this.eden = new ConcurrentHashMap<>(size * 4 / 3);
        this.longterm = new WeakHashMap<>(size * 4 / 3);
    }

	public V get(K k) {
		V v = this.eden.get(k);
		if (v == null) {
			synchronized (this) {
				if (longterm == null) {
					longterm = new WeakHashMap<K, V>(size * 4 / 3);
					return null;
				} else {
					v = this.longterm.get(k);
					if (v != null) {
						this.eden.put(k, v);
					}
				}
			}
		}
		return v;
	}
	
	/**
	 * get value, create if not presented
	 * @param k
	 * @param calculator calculate the new value
	 * @return
	 */
	public V get(K k, CacheBuilder<V> builder) {
		V v = this.eden.get(k);
		if (v == null) {
			synchronized (this) {
				if (longterm == null) {
					longterm = new WeakHashMap<K, V>(size * 4 / 3);
					v = builder.build();
					this.eden.put(k, v);
					return v;
				} else {
					v = this.longterm.get(k);
					if (v != null) {
						this.eden.put(k, v);
					} else {
						v = builder.build();
						this.eden.put(k, v);
						return v;
					}
				}
			}
		}
		return v;
	}
	
	public void put(K k, V v) {

		if (this.eden.size() >= size) {
			synchronized (this) {
				if (longterm == null) {
					longterm = new WeakHashMap<K, V>(size * 4 / 3);
				}
				this.longterm.putAll(this.eden);
				this.eden.clear();
			}
		}
		this.eden.put(k, v);
	}
	
	synchronized public void clear() {
		this.eden.clear();
		if (this.longterm == null) {
			this.longterm = new WeakHashMap<K, V>(size * 4 / 3);
		} else {
			this.longterm.clear();
		}
	}
}
