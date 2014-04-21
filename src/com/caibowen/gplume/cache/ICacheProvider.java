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

import java.util.concurrent.Future;

/**
 * This wrapper is used to gap between different PaaS caches
 * 
 * Cache provider provides two kinds of operation: synchronous operation and async ops
 * 
 * sync/async operations can be used with no distinction.
 * e.g. put asynchronously and get synchronously.
 * 
 * @author BowenCai
 *
 */
public interface ICacheProvider {

	/**
	 * in second
	 * @param second
	 */
	public void 		setExpiration(int second);
	public void 		setReplacePolicy(Policy.Replace p);
	public void 		setSetPolicy(Policy.Set p);
	
	/**
	 * in second
	 * @param second
	 */
	public int			getExpiration();
	public Policy.Set 	getSetPolicy();
	public Policy.Replace getReplacePolicy();
	
	
	public boolean 		containsSync(Object key) throws Exception;
	public void 		clearSync() throws Exception;
	

	public void 		putSync(Object key, Object value) throws Exception;
	public Object 		getSync(Object key) throws Exception;
	public void 		deleteSync(Object key) throws Exception;
	
	

	public Future<Boolean> 	containsAsync(Object key) throws Exception;
	
	public void 			clearAsync() throws Exception;
	
	public void 			putAsync(Object key, Object value) throws Exception;
	public Future<Object> 	getAsync(Object key) throws Exception;
	public void 			deleteAsync(Object key) throws Exception;
	
}
