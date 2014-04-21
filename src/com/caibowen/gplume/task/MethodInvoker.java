/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.inject.Inject;


/**
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 *
 * @author Bowen Cai
 * we should use MethodHandler
 * 
 * @since 19.02.2004
 * @see #prepare
 * @see #invoke
 */
@Deprecated // shold be replaced with MethodHandler
public class MethodInvoker {

	public static final boolean	REFLECT_ON_PRIVATE = false;
	
	@Inject
	private Object targetObject;
	
	/**
	 * set targetMethod or staticMethod
	 */
	private String targetMethod;

	private String staticMethod;

	/**
	 * optional
	 */
	private Object[] arguments = new Object[0];

	private Class<?> targetClass;

	private Method methodObject;


	public void prepare() throws ClassNotFoundException, NoSuchMethodException {
		
		if (this.staticMethod != null) {
			int lastDotIndex = this.staticMethod.lastIndexOf('.');
			if (lastDotIndex == -1 || lastDotIndex == this.staticMethod.length()) {
				throw new IllegalArgumentException(
						"staticMethod must be a fully qualified class plus method name: " +
						"e.g. 'example.MyExampleClass.myExampleMethod'");
			}
			String className = this.staticMethod.substring(0, lastDotIndex);
			String methodName = this.staticMethod.substring(lastDotIndex + 1);
			this.targetClass = Class.forName(className);
			this.targetMethod = methodName;
		}

		if (targetClass == null) {
			throw new IllegalArgumentException("Either 'targetClass' or 'targetObject' is required");
		}
		if (targetMethod == null) {
			throw new IllegalArgumentException("Property 'targetMethod' is required");
		}

		Class<?>[] argTypes = new Class[arguments.length];
		for (int i = 0; i < arguments.length; ++i) {
			argTypes[i] = (arguments[i] != null ? arguments[i].getClass() : Object.class);
		}

		// Try to get the exact method first.
		try {
			this.methodObject = targetClass.getMethod(targetMethod, argTypes);
		}
		catch (NoSuchMethodException ex) {
			// Just rethrow exception if we can't get any match.
			this.methodObject = findMatchingMethod();
			if (this.methodObject == null) {
				throw ex;
			}
		}
	}
	
	public Object invoke() throws InvocationTargetException, IllegalAccessException {
		// In the static case, target will simply be {@code null}.
//		Object targetObject 
		Method preparedMethod = getPreparedMethod();
		if (targetObject == null && !Modifier.isStatic(preparedMethod.getModifiers())) {
			throw new IllegalArgumentException("Target method must not be non-static without a target");
		}
		
		if (REFLECT_ON_PRIVATE) {
			if (!preparedMethod.isAccessible()) {
				preparedMethod.setAccessible(true);
			}
		}
		return preparedMethod.invoke(targetObject, arguments);
	}

	protected Method findMatchingMethod() {

		int argCount = arguments.length;

		Method[] candidates = targetClass.getDeclaredMethods();
		Method matchingMethod = null;

		for (Method candidate : candidates) {
			if (candidate.getName().equals(targetMethod)) {
				Class<?>[] paramTypes = candidate.getParameterTypes();
				if (paramTypes.length == argCount) {
						matchingMethod = candidate;
				}
			}
		}
		return matchingMethod;
	}

	public Method getPreparedMethod() throws IllegalStateException {
		if (this.methodObject == null) {
			throw new IllegalStateException("prepare() must be called prior to invoke() on MethodInvoker");
		}
		return this.methodObject;
	}

	public boolean isPrepared() {
		return (this.methodObject != null);
	}


	
	
	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
		this.targetClass = targetObject.getClass();
	}

	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

	public void setStaticMethod(String staticMethod) {
		this.staticMethod = staticMethod;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = (arguments != null ? arguments : new Object[0]);
	}

}
