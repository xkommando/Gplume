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
package com.caibowen.gplume.cache.mem;


import java.io.Serializable;
import java.util.HashMap;
import java.util.WeakHashMap;

import com.caibowen.gplume.common.CacheBuilder;

/**
 * the cache entries are stored in two map: ConcurrentHashMap as eden
 * and WeakHashMap as long term
 * 
 * longterm is used when eden is full
 * when serialized and deserialized, cache entries in longterm is lost,
 * this is different from WeakCache, where data is preserved 
 * 
 * @author BowenCai
 *
 * @param <K>
 * @param <V>
 */
public final class SimpleCache<K,V> implements Serializable {

	private static final long serialVersionUID = 3159753264696995816L;
	
	/**
	 * max size of eden, when eden is exceeded, data will be moved to longterm
	 */
	private final int size;

    private final HashMap<K,V> eden;
    private transient WeakHashMap<K,V> longterm;
    
    public SimpleCache(int size) {
        this.size = size;
        this.eden = new HashMap<>(size * 4 / 3);
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
		} else {
			this.longterm.clear();
		}
	}
}
