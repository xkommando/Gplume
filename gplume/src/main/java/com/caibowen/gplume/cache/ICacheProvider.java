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
