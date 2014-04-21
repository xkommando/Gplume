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
