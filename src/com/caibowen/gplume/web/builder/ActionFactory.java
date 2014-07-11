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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.annotation.Handle;
import com.caibowen.gplume.web.annotation.Intercept;
import com.caibowen.gplume.web.builder.actions.Interception;

/**
 * container for action, interception
 * 
 * @author BowenCai
 *
 */
public class ActionFactory implements IActionFactory, Serializable {
	
	private static final long serialVersionUID = -4677797873703541912L;
	// -----------------------------------------------------------------------------
	// GET, POST, HEAD, PUT, PATCH, DELETE, OPTIONS, TRACE
	// -----------------------------------------------------------------------------
	private ActionMapper<IAction> getMapper = new ActionMapper<>();
	private ActionMapper<IAction> postMapper = new ActionMapper<>();
	private ActionMapper<IAction> headMapper = new ActionMapper<>();
	private ActionMapper<IAction> putMapper = new ActionMapper<>();
	private ActionMapper<IAction> patchMapper = new ActionMapper<>();
	private ActionMapper<IAction> deleteMapper = new ActionMapper<>();
	private ActionMapper<IAction> optionMapper = new ActionMapper<>();

	private ActionMapper<Interception> interceptMapper = new ActionMapper<>();
	
	@Inject
	private IActionBuilder actionBuilder;
	@Override
	public void setActionBuilder(IActionBuilder actionBuilder) {
		this.actionBuilder = actionBuilder;
	}

	@Override
	public IAction findAction(HttpMethod httpmMthod, String uri) {
		
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
		return (Interception) interceptMapper.getAction(uri);
	}
	

	@Override
	public void registerIntercept(@Nullable String prefix, 
									@Nonnull Object controller, 
									@Nonnull Method method) {

		Object ctrl;
		if (Modifier.isStatic(method.getModifiers())) {
			ctrl = null;
		} else {
			ctrl = controller;
		}
		String[] uris = method.getAnnotation(Intercept.class).value();
		if (uris != null && uris.length > 0) {
			for (String uri : uris) {
				if (Str.Utils.notBlank(prefix))
					uri = prefix + uri;
				
				checkURI(uri);
				Interception i = actionBuilder.buildInterception(uri, ctrl, method);
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
	public void registerHandles(@Nullable String prefix, 
								@Nullable Object ctrl,
								@Nonnull Method method) {
		/**
		 * set to null to indicate the static method and avoid methodhandle binding
		 */
		if (Modifier.isStatic(method.getModifiers())) {
			ctrl = null;
		}
		
		Handle info = method.getAnnotation(Handle.class);
		String[] uris = info.value();
		
		if (uris == null || uris.length == 0) {
			throw new NullPointerException("cannot found uri"
					+ " check object["
					+ ctrl.getClass().getName() + "]");
		}
		
		boolean addPrefix = Str.Utils.notBlank(prefix);
		for (String uri : uris) {
			if (addPrefix)
				uri = prefix + uri;
			
			IAction action = actionBuilder.buildAction(uri, ctrl, method);
			
			checkURI(action.getEffectiveURI());
			HttpMethod[] methods = info.httpMethods();
			
//			System.out.print(uri);
			for (HttpMethod hm : methods) {
//				System.out.println(hm);
				// do dispatch
				switch (hm) {

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
