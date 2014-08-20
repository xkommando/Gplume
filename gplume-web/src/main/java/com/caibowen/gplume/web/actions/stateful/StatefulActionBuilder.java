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
package com.caibowen.gplume.web.actions.stateful;

import com.caibowen.gplume.common.CacheBuilder;
import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.web.IAction;
import com.caibowen.gplume.web.IView;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.builder.BuilderAux;
import com.caibowen.gplume.web.actions.stateful.actions.SimpleStatefulAction;
import com.caibowen.gplume.web.actions.stateful.actions.StrStatefulAction;
import com.caibowen.gplume.web.actions.stateful.actions.ViewStatefulAction;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * 1. get setters
 * 2. get StateGen
 * 3.  
 * @author BowenCai
 *
 */
public class StatefulActionBuilder {
	
	public static IAction buildAction(final String uri, 
									@Nullable Object object, 
									Method method) {
		Class<?> stateCls = method.getParameterTypes()[0];
		MethodHandle handle;
		try {
			handle = BuilderAux.LOOKUP.unreflect(method);
		} catch (IllegalAccessException e1) {
			throw new RuntimeException(e1);
		}
		
		final MethodHandle handle$ = Modifier.isStatic(method.getModifiers())
				? handle : handle.bindTo(object);

		final StateGen gen = stateGen(stateCls);
		
		try {
			if (method.getReturnType().equals(String.class))
				return buildJSP(uri, gen, handle$);
			
			else if (IView.class.isAssignableFrom(method.getReturnType()))
				return buildViewed(uri, object, method);
			
			else if (method.getReturnType().equals(void.class))
				return buildSimple(uri, gen, handle$);
			
		} catch (IllegalAccessException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
		
		throw new IllegalArgumentException("cannot build action out of "  + method);
	}
	
	
	/**
	 * 
	 * void act(State s, RequestContext c)
	 * 
	 * @param uri
	 * @param gen
	 * @param handle$
	 * @return
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private static IAction buildSimple(final String uri, 
										final StateGen gen,
										final MethodHandle handle$) throws IllegalAccessException, NoSuchMethodException, SecurityException {
		
		return BuilderAux.actMap.get(
				BuilderAux.hash(uri, handle$, gen), 
				new CacheBuilder<IAction>() {
					@Override
					public IAction build() {
						return new SimpleStatefulAction(uri, handle$, gen);
					}
		});
	}

	private static IAction buildJSP(final String uri, 
									final StateGen gen,
									final MethodHandle handle$) throws IllegalAccessException, NoSuchMethodException, SecurityException {

		Class<?> ps[] = handle$.type().parameterArray();
		final boolean hasReq = ps[ps.length - 1].equals(RequestContext.class);
		
		return BuilderAux.actMap.get(BuilderAux.hash(uri, handle$, gen, hasReq),
				new CacheBuilder<IAction>() {
					@Override
					public IAction build() {
						return new StrStatefulAction(
                                uri, handle$, gen,
                                hasReq, BuilderAux.STR_VIEW_RESOLVER);
					}
				});
	}
	
	private static IAction 
	buildViewed(final String uri, @Nullable final Object object, 
			final Method method) throws IllegalAccessException, NoSuchMethodException, SecurityException {
//ViewStatefulAction(String u, Method m, Object ctrl, boolean hasReq, StateGen g) {

		Class<?> ps[] = method.getParameterTypes();
		final boolean hasReq = ps[ps.length - 1].equals(RequestContext.class);
		final StateGen gen = stateGen(ps[0]);
		return BuilderAux.actMap.get(
		BuilderAux.hash(uri, method, object, gen, hasReq),
				new CacheBuilder<IAction>() {
					@Override
					public IAction build() {
						return 
						new ViewStatefulAction(uri, method, object, hasReq, gen, BuilderAux.IVIEW_RESOLVER);
					}
				});
	}
	
	private static StateGen stateGen(Class<?> stateCls) {
		
		StateGen s = BuilderAux.stateMap.get(stateCls);
		if (s != null)
			return s;
		List<IStateSetter> setters = setters(stateCls);
		
		Class<?> refClass = referredClass(stateCls);
		// has trivial constructor
		if (refClass == null) {
			Constructor<?> c;
			try {
				c = stateCls.getDeclaredConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
			if (!c.isAccessible())
				c.setAccessible(true);
			return new StateGen(setters, c);
			
		} else {
			// non-static inner class
			// ctor has referred object
			Constructor<?> c;
			try {
				c = stateCls.getDeclaredConstructor(refClass);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
			if (!c.isAccessible())
				c.setAccessible(true);
			
			Object ref = BuilderAux.ctrlMap.get(refClass);
			
			if (ref == null) {
			// controller not registered, search bean container
				Set<Object> beans = AppContext.beanAssembler.getBeans(refClass);
				if (beans.size() != 1)
					throw new RuntimeException(
				MessageFormat.format(
				"State Class [{0}] referred class [{1}], and {1} is not a controller class with instance registered."
				+ "\r\n and cannot determine Object of this class from beanassembler, need 1, get {2}"
				+ "\r\n, founded objects of this class are {3}"
				, stateCls, refClass, beans.size(), beans)
				);
				ref = beans.iterator().next();
			}
			s = new NestedStateGen(setters, c, ref);
			BuilderAux.stateMap.put(stateCls, s);
			return s;
		}
	}
	
	private static List<IStateSetter> setters(Class<?> stateCls) {
		
		Field[] fs = stateCls.getDeclaredFields();
		List<IStateSetter> setters = new ArrayList<>(fs.length);
		for (Field f : stateCls.getDeclaredFields()) {
			IStateSetter st = SetterFactory.createSetter(f);
			if (st != null)
				setters.add(st);
		}
		return setters;
	}
	
	/**
	 * 
	 * @param stateCls
	 * @return null if no ref
	 */
	private static Class<?> referredClass(Class<?> stateCls) {
		if (stateCls.isMemberClass() && !Modifier.isStatic(stateCls.getModifiers())) {
			return stateCls.getDeclaringClass();
		}
		return null;
	}
}




