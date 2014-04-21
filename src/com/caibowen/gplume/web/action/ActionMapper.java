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
package com.caibowen.gplume.web.action;

import java.io.Serializable;
import java.util.HashMap;

import com.caibowen.gplume.common.URITrie;

/**
 * 
 * 
 * handlers manage actors, and pass request, 
 * along with its response, to the correspondent actor and the actor performs
 * 
 * @author BowenCai
 *
 */
public class ActionMapper<T extends Action> implements Serializable {

	private static final long serialVersionUID = 9039999917329134916L;

	// strict match
	private HashMap<String, T> fixedURIMap = new HashMap<>(80);

	// versatile actors, match /xyz/* or /sadfj*
	private URITrie<T> multiURIMap = new URITrie<>();
	
	public void add(final T action)throws IllegalArgumentException {

		String s = action.getEffectiveURI();
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
	
//	public boolean remove(Object ctrlObj) {
//		
//		Actor actor = new Actor(ctrlObj, null);
//		boolean fixedURI = actorMap.values().remove(actor);
//		/**
//		 * @see Actor equals
//		 */
//			String uri = versatileActorMap.getPrefix(actor);
//		boolean multiURI = uri ==  null ? false : null == versatileActorMap.disjoin(uri);
//		
//		return fixedURI | multiURI;
//	}

	public T getAction(String actionName){

		T action = fixedURIMap.get(actionName);
		
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




