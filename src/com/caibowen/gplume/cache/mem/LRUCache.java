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
import java.util.LinkedHashMap;
import java.util.Map;

import com.caibowen.gplume.common.CacheBuilder;

/**
 * LRU cache based on LinkedHashMap
 * @author BowenCai
 *
 */
public class LRUCache<K,V> implements Serializable {

	private static final long serialVersionUID = 6428478108963526728L;
	
	final int size;
	private final LinkedHashMap<K, V> map = new LinkedHashMap<K, V>(101){
		private static final long serialVersionUID = 7598485345807394732L;
		@Override
	    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
	        return this.size() > LRUCache.this.size;
	    }
	};
	
	public LRUCache(int size) {
		this.size = size;
	}
	public V get(K key) {
		return map.get(key);
	}

	public V get(K k, CacheBuilder<V> builder) {
		V v = map.get(k);
		if (v == null) {
			v = builder.build();
			synchronized (map) {
				map.put(k, v);
			}
		}
		return v;
	}
	
	public void put(K k, V v) {
		synchronized (map) {
			map.put(k, v);
		}
	}

	public void clear() {
		synchronized (map) {
			map.clear();
		}
	}
}
