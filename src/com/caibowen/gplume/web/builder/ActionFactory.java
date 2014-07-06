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
package com.caibowen.gplume.web.builder;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.actions.SimpleAction;
import com.caibowen.gplume.web.note.Handle;
import com.caibowen.gplume.web.note.Intercept;

/**
 * container for action,interception
 * 
 * @author BowenCai
 *
 */
public class ActionFactory implements IActionFactory, Serializable {
	
	private static final long serialVersionUID = -4677797873703541912L;
	// -----------------------------------------------------------------------------
	// GET, POST, HEAD, PUT, PATCH, DELETE, OPTIONS, TRACE
	// -----------------------------------------------------------------------------
	private ActionMapper<SimpleAction> getMapper = new ActionMapper<SimpleAction>();
	private ActionMapper<SimpleAction> postMapper = new ActionMapper<SimpleAction>();
	private ActionMapper<SimpleAction> headMapper = new ActionMapper<SimpleAction>();
	private ActionMapper<SimpleAction> putMapper = new ActionMapper<SimpleAction>();
	private ActionMapper<SimpleAction> patchMapper = new ActionMapper<SimpleAction>();
	private ActionMapper<SimpleAction> deleteMapper = new ActionMapper<SimpleAction>();
	private ActionMapper<SimpleAction> optionMapper = new ActionMapper<SimpleAction>();

	private ActionMapper<Interception> interceptMapper = new ActionMapper<Interception>();
	
	@Override
	public SimpleAction findAction(HttpMethod httpmMthod, String uri) {
		
		switch (httpmMthod) {
		
		case GET:
			return getMapper.getAction(uri);
		case POST:
			return postMapper.getAction(uri);
		case PUT:
			return putMapper.getAction(uri);
		case HEAD:
			return headMapper.getAction(uri);
		case OPTIONS:
			return optionMapper.getAction(uri);
		case DELETE:
			return deleteMapper.getAction(uri);
		case PATCH:
			return patchMapper.getAction(uri);
		default:
			throw new UnsupportedOperationException(
					"http method[" + httpmMthod + "] for [" + uri + "] is not suported");
		}
	}
	
	@Override
	public Interception findInterception(String uri) {
		return interceptMapper.getAction(uri);
	}
	

	@Override
	public void registerIntercept(Object controller, Method method) {

		Object ctrl;
		if (Modifier.isStatic(method.getModifiers())) {
			ctrl = null;
		} else {
			ctrl = controller;
		}
		String[] uris = method.getAnnotation(Intercept.class).value();
		if (uris != null && uris.length > 0) {
			for (String uri : uris) {
				checkURI(uri);
				Interception i = ActionBuilder.buildInterception(uri, ctrl, method);
				interceptMapper.add(i);
			}
		} else {
			throw new NullPointerException(
					"No URI specified for Intercept method[" + method.getName()
							+ "] in class [" + controller.getClass().getName()
							+ "]");
		}
	}

	@Override
	public void registerHandle(Object controller, Method method) {
//System.out.println("add class[" + controller.getClass().getSimpleName() + "] method[" + method.getName() + ']');

		Object ctrl = null;

		if (!Modifier.isStatic(method.getModifiers())) {
			ctrl = controller;
		}
		toHandle(ctrl, method, method.getAnnotation(Handle.class));
	}
	
	private void toHandle(Object ctrl, Method func, Handle info) {
		String[] uris = info.value();
		
		if (uris == null || uris.length == 0) {
			throw new NullPointerException("cannot found uri"
					+ " check object["
					+ ctrl.getClass().getName() + "]");
		}
		
		for (String uri : uris) {
			SimpleAction action = ActionBuilder.buildAction(uri, ctrl, func);
			checkURI(action.getEffectiveURI());
			HttpMethod[] methods = info.httpMethods();
			
			for (HttpMethod method : methods) {
				// do dispatch
				switch (method) {

				case GET:
					getMapper.add(action);
					break;
				case POST:
					postMapper.add(action);
					break;
				case PUT:
					putMapper.add(action);
					break;
				case HEAD:
					headMapper.add(action);
					break;
				case OPTIONS:
					optionMapper.add(action);
					break;
				case DELETE:
					deleteMapper.add(action);
					break;
				case PATCH:
					patchMapper.add(action);
					break;
				default:
					/**
					 * how to debug using trace ???
					 */
					break;
				} // switch
			} // methods
		}
		
	}
	
	
	protected void checkURI(final String uri) {

//System.out.println("[" + uri +"]");
		if (!Str.Patterns.MAPPING_URI.matcher(uri).matches()) {
			
			throw new IllegalArgumentException("URI [" + uri
					+ "] must match \"" + Str.Patterns.MAPPING_URI.pattern()
					+ "\"");
		}
	}
	
	@Override
	public boolean removeHandle(String uri) {
		return getMapper.remove(uri) || postMapper.remove(uri)
				|| deleteMapper.remove(uri) || putMapper.remove(uri)
				|| headMapper.remove(uri) || optionMapper.remove(uri)
				|| patchMapper.remove(uri);
	}

	@Override
	public boolean removeInterception(final String uri) {
		return interceptMapper.remove(uri);
	}

	@Override
	public void destroy() {
		
		getMapper.clear();
		getMapper = null;
		postMapper.clear();
		postMapper = null;
		headMapper.clear();
		headMapper = null;
		putMapper.clear();
		putMapper = null;
		patchMapper.clear();
		patchMapper = null;
		deleteMapper.clear();
		deleteMapper = null;
		optionMapper.clear();
		optionMapper = null;
		interceptMapper.clear();
		interceptMapper = null;
	}
}
