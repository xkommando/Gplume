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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.caibowen.gplume.cache.mem.WeakCache;
import com.caibowen.gplume.common.CacheBuilder;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.actions.SimpleAction;
import com.caibowen.gplume.web.builder.stateful.StateGen;
import com.caibowen.gplume.web.misc.JspViewResolvers;
import com.caibowen.gplume.web.view.IStrViewResolver;

/**
 * 
 * @author BowenCai
 *
 */
public class BuilderAux {

	// from java.lang.invoke
	public static final Lookup LOOKUP = MethodHandles.publicLookup();

	/**
	 * 
	 * void func(RequestContext );
	 */
	public static final MethodType SIMPLE_TYPE = MethodType.methodType(
			void.class, RequestContext.class);
	
	/**
	 * String func(RequestContext );
	 */
	public static final MethodType RET_JSP_TYPE = MethodType.methodType(
			String.class, RequestContext.class);
	
	/**
	 * String func();
	 */
	public static final MethodType RET_JSP_NOPARAM_TYPE = MethodType.methodType(
			String.class);
	
	/**
	 * void func(RequestContext, SimpleAction);
	 */
	public static final MethodType INTERCEPT_TYPE = MethodType.methodType(
			void.class, RequestContext.class, SimpleAction.class);

	/**
	 * keep track of all actions, avoid rebuilding actions
	 */
	public static final WeakCache<Integer, IAction> actMap = new WeakCache<>(256);
	public static final WeakCache<Integer, Interception> incMap = new WeakCache<>(128);

	public static final WeakCache<Class<?>, StateGen> stateMap = new WeakCache<>(256);
	public static final WeakCache<Class<?>, Object> ctrlMap = new WeakCache<>(256);

    public static IStrViewResolver STR_VIEW_RESOLVER = new JspViewResolvers.CompletePathViewResolver();


	public static Interception 
	buildInterception(final String u, 
						Object object, 
						Method method) {
		
		MethodHandle handle = findMethodeHandle(method, INTERCEPT_TYPE);
		final MethodHandle $ = Modifier.isStatic(method.getModifiers())
				? handle : handle.bindTo(object);
		
		return incMap.get(hash(u, handle), new CacheBuilder<Interception>() {
			@Override
			public Interception build() {
				return new Interception(u , $);
			}
		});
	}

	public static int hash(@Nullable Object...args) {
		int h = 1;
		for (int i = 0; i < args.length; i++) {
			Object object = args[i];
			h = 31 * h + (object == null ? 0 : object.hashCode());
		}
		return h;
	}
	/**
	 * @Handle(uri={"/abc/{erf::date}"})
	 * void handle(RequestContext req); OK, value put to request attribute
	 * void handle(Date date, RequestContext req); OK, value as arg
	 * 
	 * why not unreflect ? 
	 * to get detailed info in debugging
	 * 
	 * @param controller
	 * @param method
	 * @param typ
	 * @return
	 */
	protected static @Nonnull MethodHandle 
	findMethodeHandle(Method method, MethodType typ) {

		String mName = method.getName();
		Class<?> ctrlClazz = method.getDeclaringClass();
		MethodHandle actionhandle = null;
		
		try {
			if (Modifier.isStatic(method.getModifiers())) {
				
				actionhandle = LOOKUP.findStatic(ctrlClazz, mName,
						typ);
			} else {
				actionhandle = LOOKUP.findVirtual(ctrlClazz, mName,
						typ);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(
					"\nError making Action : cannot find method ["
							+ method
							+ "]\n of type [" + typ + "]"
							+"\n", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
					"Error making Action : cannot access method ["
							+ method, e);
		}
		return actionhandle;
	}

	
}
