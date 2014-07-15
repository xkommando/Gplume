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

import com.caibowen.gplume.cache.mem.WeakCache;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.actions.SimpleAction;

/**
 * 
 * @author BowenCai
 *
 */
public class BuilderHelper {

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
	public static final WeakCache<Class<?>, Object> ctrlMap = new WeakCache<>(256);
	public static final WeakCache<Class<?>, Object> incMap = new WeakCache<>(128);
	
	public static Interception 
	buildInterception(String u, 
						Object object, 
						Method method) {
		
		MethodHandle handle = findMethodeHandle(method, INTERCEPT_TYPE);
		handle = object == null ? handle : handle.bindTo(object);
		return new Interception(u , handle);
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
					"\nError making Interception : cannot find method ["
							+ method
							+ "]\n of type [" + typ + "]"
							+"\nin class ["
							+ ctrlClazz.getName()
							+ "]");
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
					"Error making Interception : cannot access method ["
							+ method
							+ "] in class ["
							+ ctrlClazz.getName()
							+ "]");
		}
		return actionhandle;
	}

	
}
