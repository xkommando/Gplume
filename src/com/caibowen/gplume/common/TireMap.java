package com.caibowen.gplume.common;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class TireMap<K extends CharSequence, V> extends AbstractMap<K, V> {

	Map<K, V> wordMap;

	@Override
	public Set<java.util.Map.Entry<K, V> > entrySet() {
		return wordMap.entrySet();
	}
}
