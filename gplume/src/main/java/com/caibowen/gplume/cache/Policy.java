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
package com.caibowen.gplume.cache;

/**
 *   set/replace policies of a cache.
 *   
 *   Note that some of them may not be supported by the cache provider
 *  
 * @author BowenCai
 *
 */
public final class Policy {
    
	public enum Replace {
		RANDOM,
		FIFO,
		LRU,
		LFU
	}
	
	/**
	 * policies of setting a k/value pair to a cache.
	 * as important as the replace policy, 
	 * but the Chinese teacher never mentioned them, 
	 * shame on that.
	 * 
	 * @author BowenCai
	 */
	public enum Set {
		
	    /**
	     * Always stores the new value.  
	     * If an existing value was stored with the
	     * given key, it will be discarded and replaced.
	     */
		ALWAYS,
		
	    /**
	     * do not replace existed value,
	     * add value only if the value does not existed, useful to avoid race conditions.
	     */
		ADD_NEW,
		
	    /**
	     * only replace existed value, do not add new key/value pairs
	     */
		REPLACE_OLD
	}
	
	private Policy(){}
}
