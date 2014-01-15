package com.caibowen.gplume.cache;


public interface ICacheProvider {
	
	public void 		setExpiration(int minuts);
	
	public void 		put(Object key, Object value) throws Exception;
	public Object 		get(Object key) throws Exception;
	
	public boolean 		contains(Object key) throws Exception;
	
	public void 		clear() throws Exception;
	public void 		delete(Object key) throws Exception;


//	cache.clearAll();
//	cache.contains(key)
//	cache.delete(key);
//	cache.deleteAll(keys);
//	cache.get(key);
//	cache.getAll(keys);
//	cache.increment(key, delta);
//	cache.put(key, value, expires, policy);
//	cache.putAll(values, expires, policy);
}
