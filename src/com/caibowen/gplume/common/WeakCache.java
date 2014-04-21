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

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;


/**
 * A weak and lazy cache.
 * 
 * Weak map with weak ref to the value
 * 
 * the map is transient, and will be inited only if put is called
 * 
 * @author BowenCai
 *
 * @param <K>
 * @param <V>
 */
public final class WeakCache<K, V> {

	private transient WeakHashMap<K, WeakReference<V>> map = new WeakHashMap<K, WeakReference<V>>();
	
	public boolean contains(K key) {
		if (map == null) {
			return false;
		}
		return map.get(key) == null;
	}
	
	public V get(K key) {
		if (map == null) {
			return null;
		}
		WeakReference<V> reference = this.map.get(key);
		if (reference == null) {
			return null;
		}
		V value = reference.get();
		if (value == null) {
			this.map.remove(key);
		}
		return value;
	}
	
	public V get(K key, CacheBuilder<V> builder) {
		if (map == null) {
			map = new WeakHashMap<K, WeakReference<V>>();
			V var = builder.build();
			map.put(key, new WeakReference<V>(var));
			return var;
		}
		
		WeakReference<V> reference = this.map.get(key);
		V value;
		if (reference == null) {
			value = builder.build();
			map.put(key, new WeakReference<V>(value));
			return value;
		}
		value = reference.get();
		if (value == null) {
			value = builder.build();
			this.map.put(key, new WeakReference<V>(value));
		}
		return value;
	}
	
	public void put(K key, V value) {
		if (map == null) {
			map = new WeakHashMap<K, WeakReference<V>>();
			map.put(key, new WeakReference<V>(value));
			return;
		}
		if (value != null) {
			this.map.put(key, new WeakReference<V>(value));
		} else {
			this.map.remove(key);
		}
	}

	public void clear() {
		if (map != null) {
			this.map.clear();
		}
	}
}
