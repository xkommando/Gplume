package com.caibowen.gplume.common.collection;

import java.util.HashMap;
import java.util.Map;

public class NoCaseMap<V> extends HashMap<String, V>{

	private static final long serialVersionUID = 6686934837476598122L;

	public NoCaseMap() {
		this(16);
	}
	
	public NoCaseMap(int initCap) {
		super(initCap * 4 / 3 + 1);
	}
	
	@Override
	public V put(String key, V value) {
		return super.put(key.toLowerCase(), value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> map) {
		if (map.isEmpty()) {
			return;
		}
		for (Entry<? extends String, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return key instanceof String 
				&& super.containsKey(((String) key).toLowerCase());
	}

	@Override
	public V get(Object key) {
		if (key instanceof String) {
			return super.get(((String) key).toLowerCase());
		}
		else {
			return null;
		}
	}

	@Override
	public V remove(Object key) {
		if (key instanceof String ) {
			return super.remove(((String)key).toLowerCase());
		}
		else {
			return null;
		}
	}

	@Override
	public void clear() {
		super.clear();
	}
}
