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
package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.common.URITrie;
import com.caibowen.gplume.web.IAction;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 
 * 
 * handlers manage actions, and pass request,
 * along with its response, to the correspondent actor and the actor performs
 * 
 * @author BowenCai
 *
 */
public class ActionMapper<T extends IAction> implements Serializable {

	private static final long serialVersionUID = 9039999917329134916L;

	// strict match
	private HashMap<String, T> fixedURIMap = new HashMap<>(128);

	// versatile actors, match /xyz/* or /sadfj*
	private URITrie<T> multiURIMap = new URITrie<>();
	
	public void 
	add(final T action)throws IllegalArgumentException {

		String s = action.effectiveURI();
		int len = s.length();
		//  /*
		if (s.charAt(len - 1) == '*') {
			
			final String _s = s.substring(0, len - 1);
			final String bch = multiURIMap.covers_or_covered_by(_s);
			if (null == bch) {
				if (multiURIMap.branch(_s, action)) {
					return;
				}
			}
			
			throw new IllegalArgumentException(
					  "multi-match uri conflicts"
						+"\n[" + _s + "]\n[" + bch + "]");
			
		} else if (null == fixedURIMap.put(s, action)) {
			return;
		} else {
			throw new IllegalArgumentException(
					"[" + s+ "] already exists");
		}
	}
	
	public boolean remove(final String uri) {

		int len = uri.length();
		if (uri.charAt(len - 1) == '*') {
			return null != multiURIMap.disjoin(uri.substring(0, len - 1));
		} else {
			return null != fixedURIMap.remove(uri);
		}
	}
	

	public IAction getAction(String actionName){

		IAction action = fixedURIMap.get(actionName);
		
		if (action == null) {
			action = multiURIMap.matchPrefix(actionName);
		}
		return action;
	}
	
	public void clear() {
		fixedURIMap.clear();
		multiURIMap.clear();
	}
}



