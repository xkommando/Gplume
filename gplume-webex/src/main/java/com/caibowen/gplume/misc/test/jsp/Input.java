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
package com.caibowen.gplume.misc.test.jsp;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.caibowen.gplume.web.RequestContext;


/**
 * Wrapper for the HttpServletRequest.
 * 
 * delegate all attributes to the hashmap,
 * so that all operation during the compilation will not affect the original request
 * 
 * @author BowenCai
 *
 */
public class Input extends HttpServletRequestWrapper {
	
	public static Input buildFrom(RequestContext c) {
		return new Input(c.request);
	}
	public static Input buildFrom(HttpServletRequest c) {
		return new Input(c);
	}
	public WeakHashMap<String, Object> map;
	private Input(HttpServletRequest request) {
		super(request);
        map = new WeakHashMap<>(64);
		Enumeration<String> names = request.getAttributeNames();
		while (names.hasMoreElements()) {
			String string = (String) names.nextElement();
			this.map.put(string, request.getAttribute(string));
		}
	}
	
	@Override
	public Object getAttribute(String arg0) {
		return map.get(arg0);
	}

	@Override
	public Enumeration getAttributeNames() {
		Set<String> keys = map.keySet();
		Vector<String> vector = new Vector<String>(keys.size());
		for (String k : keys) {
			vector.add(k);
		}
		return vector.elements();
	}

	@Override
	public void removeAttribute(String arg0) {
		this.map.remove(arg0);
	}


	@Override
	public void setAttribute(String arg0, Object arg1) {
		this.map.put(arg0, arg1);
	}
}
