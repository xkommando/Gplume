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
package com.caibowen.gplume.web.builder.stateful;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.web.builder.BuilderHelper;
import com.caibowen.gplume.web.builder.IAction;
import com.caibowen.gplume.web.builder.IActionBuilder;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.stateful.actions.SimpleStatefulAction;



/**
 * 1. get setters
 * 2. get StateGen
 * 3.  
 * @author BowenCai
 *
 */
public class StatefulActionBuilder implements IActionBuilder {

	@Override
	public Interception buildInterception(String u, Object object, Method method) {
		return BuilderHelper.buildInterception(u, object, method);
	}
	
	@Override
	public IAction buildAction(final String uri, 
									@Nullable Object object, 
									Method method) {
//		if (method.getReturnType().equals(void.class)) {
//			return buildAction(uri, object, method);
//		}
		try {
			return buildSimple(uri, object, method);
		} catch (IllegalAccessException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static IAction buildSimple(final String uri, 
										@Nullable Object object, 
										Method method) throws IllegalAccessException, NoSuchMethodException, SecurityException {
		
		Class<?> stateCls = method.getParameterTypes()[0];
		MethodHandle handle = BuilderHelper.LOOKUP.unreflect(method);
		
		handle = Modifier.isStatic(method.getModifiers())
				? handle : handle.bindTo(object);
		
		return new SimpleStatefulAction(uri, handle, stateGen(stateCls));
	}
	
	private static StateGen stateGen(Class<?> stateCls) throws NoSuchMethodException, SecurityException {
		
		List<IStateSetter> setters = setters(stateCls);
		
		Class<?> refClass = referredClass(stateCls);
		if (refClass == null) {
			Constructor<?> c = stateCls.getConstructor();
			if (!c.isAccessible())
				c.setAccessible(true);
			return new StateGen(setters, c);
			
		} else {// reffered 
			System.out.println("StatefulActionBuilder.stateGen()");
			System.out.println(stateCls);
			System.out.println(refClass);
			Constructor<?> c = stateCls.getDeclaredConstructor(refClass);
			if (!c.isAccessible())
				c.setAccessible(true);
			
			Object ref = BuilderHelper.ctrlMap.get(refClass);
			if (ref == null) {
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
			
			return new NestedStateGen(setters, c, ref);
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




