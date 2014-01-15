package com.caibowen.gplume.web;

import java.util.HashMap;

import com.caibowen.gplume.common.URITrie;
import com.caibowen.gplume.except.DuplicatedActionNameException;
import com.caibowen.gplume.web.ControlCenter.Request;


/**
 * handlers manage actors, and pass request, 
 * along with its response, to the correspondent actor and the actor performs
 * 
 * @author BowenCai
 *
 */
public class RequestHandler {

	// strict match
	private HashMap<String, Actor> actorMap = new HashMap<>(80);

	// versatile actors, match /xyz/* or /sadfj*
	private URITrie<Actor> versatileActorMap = new URITrie<>();
	
	public void add(final String s, final Actor actor) throws DuplicatedActionNameException {
		
		int len = s.length();
		
		if (s.charAt(len - 1) == '*') {
			
			final String _s = s.substring(0, len - 1);
			if (0 == versatileActorMap.covers_or_covered_by(_s)) {
				if (versatileActorMap.branch(_s, actor)) {
					return;
				}
			}
			throw new DuplicatedActionNameException(
						"[" + s+ "] conflicts with exisiting \"*\" multi-match  uri");
			
		} else if (null == actorMap.put(s, actor)) {
			return;
		} else {
			throw new DuplicatedActionNameException(
					"[" + s+ "] already exists");
		}
	}
	
	public boolean remove(Object ctrlObj) {
		
		if(actorMap.values().remove(ctrlObj)) {
			return true;
		} else {
			/**
			 * trie currently cannot delete by value
			 */
			return false;
//			return null != versatileActorMap.disjoin(k)(ctrlObj);
		}
	}
	
	public boolean handle(String actionName,Request request) throws Throwable {

		Actor actor = actorMap.get(actionName);
		
		if (actor == null) {
			actor = versatileActorMap.matchPrefix(actionName);
		}
		
		if (actor == null) {
			// no actor found for this URI: it is a 404 error!
			/**
			 * that HTTP 404 error is quite common 
			 * is the reason why we do not thrown any exception for it
			 * since exception throwing is expensive!
			 */
			return false;
			
		} else {

			/**
			 * throws:
			 * ServletException
			 * IOException
			 * throwable(invoke)
			 * exceptions from under functions
			 */
			actor.perform(request);
			
			// no error, request handled!
			return true;
		}
	}
	
}




