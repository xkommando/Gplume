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

import com.caibowen.gplume.common.Function;

/**
 * LRU cache based on a synchronized LinkedHashMap
 * @author BowenCai
 *
 */
public class SimpleLRUCache<K,V> implements Serializable {

	private static final long serialVersionUID = 6428478108963526728L;

	private int size;
	private  Map<K, V> map;

    public SimpleLRUCache() {
        this(64);
    }

	public SimpleLRUCache(int size) {
		this.size = size;
        map = new LinkedHashMap<K, V>(size * 4 / 3 + 1){
            private static final long serialVersionUID = 7598485345807394732L;
            @Override
            protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
                return this.size() > SimpleLRUCache.this.size;
            }
        };
	}

    public boolean hasKey(K k) {
        synchronized (map){
            return map.containsKey(k);
        }
    }

    public boolean hasValue(V v) {
        synchronized (map){
            return map.containsValue(v);
        }
    }

	public V get(K key) {
        synchronized (map){
            return map.get(key);
        }
	}

    public V getOrDefault(K key, V defv) {
        V v;
        synchronized (map){
            return (v = map.get(key)) == null ? defv : v;
        }
    }

	public V get(K k, Function<K, V> builder) {
        V v;
        synchronized (map) {
            v = map.get(k);
            if (v == null) {
                v = builder.apply(k);
                map.put(k, v);
            }
        }
		return v;
	}


    public V putIfAbsent(K k, V v) {
        V ov;
        synchronized (map) {
            ov = map.get(k);
            if (ov == null) {
                map.put(k, v);
            }
        }
        return ov;
    }


    public V update(K k, V v) {
        synchronized (map) {
            return map.put(k, v);
        }
    }

    public V remove(K k) {
        synchronized (map) {
            return map.remove(k);
        }
    }

    public void clear() {
        synchronized (map) {
            map.clear();
        }
    }

    public int getSize() {
        synchronized (map) {
            return size;
        }
    }

    public void setSize(int size) {
        synchronized (map) {
            this.size = size;
        }
    }
}
