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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import com.caibowen.gplume.common.CacheBuilder;
import com.caibowen.gplume.misc.Hash;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.actions.JspAction;
import com.caibowen.gplume.web.builder.actions.RestAction;
import com.caibowen.gplume.web.builder.actions.SimpleAction;
import com.caibowen.gplume.web.builder.actions.ViewAction;
import com.caibowen.gplume.web.view.IView;

/*
 *  * 
 * For RestAction(parameter in URI), you can declare the argument in the method interface
 * (as the first parameter, and the second also the last parameter, is RequestContext)
 *
 * you can also write the method as other plain handle, 
 * this way, the argument will be put into the RequstContext as a attribute, with the name specified in your URI
 * example:
 * with annotation @Hanlde({"/abc/def/{arg-name::Integer}.html"})
 * you can write method as 
 * cotrollerMethod(Type arg, RequestContext c) {
 * 	// use the arg here, arg may be null
 * }
 * or
 * cotrollerMethod(RequestContext c) {
 * 		Type var = c.getAttr("arg-name");
 * }
 */
/**
 * @author BowenCai
 *
 */
public class ActionBuilder extends RestActionBuilder implements IActionBuilder {
	
//	private static final Logger LOG = Logger.getLogger(ActionBuilder.class.getName());
	
	@Override public Interception 
	buildInterception(String u, 
						Object object, 
						Method method) {
		
		MethodHandle handle = findMethodeHandle(method, INTERCEPT_TYPE);
		handle = object == null ? handle : handle.bindTo(object);
		return new Interception(u , handle);
	}

	
	/**
	 * @param uri
	 * @param object
	 * @param handle
	 * @return Action or RestAction
	 * @see RestAction
	 */
	@Override public IAction buildAction(final String uri, 
									@Nullable Object object, 
									Method method) {
		if (object != null)
			ctrlMap.put(object.getClass(), object);
		
		if (uri.indexOf('{') != -1) {// rest uri
			if (uri.lastIndexOf('}') != -1) {
				return buildRest(uri, object, method);
				
			} else {
				throw new IllegalArgumentException("illegal uri ["
						+ uri + "] miss right '{'" + "in class["
						+ object.getClass().getName() + "]  method ["
						+ method + "]");
			}
			
		} else {// average action
			Class<?> retKlass = method.getReturnType();
			Class<?>[] _ = method.getParameterTypes();
			final boolean hasRequestContext = _.length > 0 && _[_.length - 1].equals(RequestContext.class);
			
			if (retKlass.equals(String.class)) {
				
				MethodHandle handle = 
				findMethodeHandle(method, 
						hasRequestContext ? 
								RET_JSP_TYPE 
								: RET_JSP_NOPARAM_TYPE);
				
				final MethodHandle handle$ = 
						object != null ? handle.bindTo(object) : handle;
				
				return actMap.get(Hash.hash(uri, handle, hasRequestContext), 
									new CacheBuilder<IAction>() {
										@Override
										public IAction build() {
											return new JspAction(uri, handle$, hasRequestContext);
										}
									});

				
			} else if (retKlass.equals(void.class)) {
				MethodHandle handle = findMethodeHandle(method, SIMPLE_TYPE);
				final MethodHandle handle$ = object != null ? handle.bindTo(object) : handle;
				return actMap.get(Hash.hash(uri, handle, handle), 
						new CacheBuilder<IAction>() {
							@Override
							public IAction build() {
								return new SimpleAction(uri, handle$);
							}
						});
				
			} else if (IView.class.isAssignableFrom(retKlass)){
				final Method $$ = method; final Object $$$ = object; 
				return actMap.get(Hash.hash(uri, method, object, hasRequestContext), 
						new CacheBuilder<IAction>() {
							@Override
							public IAction build() {
								return new ViewAction(uri, $$, $$$, hasRequestContext);
							}
						});
				
			} else {
				throw new NullPointerException(
						"null uri || null controller object || null method handle");
			}
		}
	}

}




