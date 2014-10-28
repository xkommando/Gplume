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
package com.caibowen.gplume.web.actions.builder;

import com.caibowen.gplume.common.Function;
import com.caibowen.gplume.web.IAction;
import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.SimpleAction;
import com.caibowen.gplume.web.actions.ViewAction;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/*
 *  * 
 * For RestAction(parameter in URI), you can declare the argument in the method interface
 * (as the first parameter, and the second also the last parameter, is RequestContext)
 *
 * you can also write the method as other plain handle, 
 * this way, the argument will be put into the RequstContext as a attribute, with the id specified in your URI
 * example:
 * with annotation @Hanlde({"/abc/def/{arg-id::Integer}.html"})
 * you can write method as 
 * cotrollerMethod(Type arg, RequestContext c) {
 * 	// use the arg here, arg may be null
 * }
 * or
 * cotrollerMethod(RequestContext c) {
 * 		Type var = c.getAttr("arg-id");
 * }
 */
/**
 * @author BowenCai
 *
 */
class SimpleActionBuilder {
	
//	private static final Logger LOG = Logger.getLogger(SimpleActionBuilder.class.getName());
	
	/**
	 * @param uri
	 * @param object
	 * @param method
	 * @return Action or RestAction
	 * @see com.caibowen.gplume.web.actions.RestAction
	 */
	public static IAction buildAction(final String uri, @Nonnull Object object,
			Method method) {
		/**
		 * set to null to indicate the static method and avoid methodhandle binding
		 */
		Class<?> retKlass = method.getReturnType();
		Class<?>[] _t = method.getParameterTypes();
		final boolean hasRequestContext = _t.length > 0
				&& _t[_t.length - 1].equals(RequestContext.class);

        final IViewResolver resolver;

		if (retKlass.equals(void.class)) {
			MethodHandle handle = BuilderAux.findMethodeHandle(method, BuilderAux.SIMPLE_TYPE);
			final MethodHandle handle$ = Modifier.isStatic(method.getModifiers())
					? handle : handle.bindTo(object);
					
			return BuilderAux.actMap.get(
					BuilderAux.hash(uri, handle, handle),
					new Function<Integer, IAction>() {
						@Override
						public IAction apply(Integer i) {
							return new SimpleAction(uri, handle$);
						}
					});

		} else if (null != (resolver = BuilderAux.viewMatcher.findMatch(retKlass))) {
			final Method $$ = method;
			final Object $$$ = object;
			return BuilderAux.actMap.get(
					BuilderAux.hash(uri, method, object, hasRequestContext),
					new Function<Integer, IAction>() {
						@Override
						public IAction apply(Integer i) {
							return new ViewAction(uri, $$, $$$,
									hasRequestContext, resolver);
						}
					});

		} else {
			throw new IllegalArgumentException(
					"cannot build action for method [" + method + "]");
		}

	}
}




