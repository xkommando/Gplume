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
package com.caibowen.gplume.core;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;


/**
 * keeper
 * 
 * @author BowenCai
 *
 */
public class SemaphoreKeeper {

	public static final int HIGH = 2;
	public static final int MEDIAN = 1;
	public static final int LOW = 0;
	
	ConcurrentHashMap<Method, MethodHandle> handleMap;
	
	public SemaphoreKeeper() {
		handleMap = new ConcurrentHashMap<Method, MethodHandle>(128);
	}
	
	public Semaphore getSemaphore(Semaphored note) {
		return new Semaphore(note.permit(), note.fair());
	}
	
	public MethodHandle register(Method method, Object obj, Semaphore semo) {

		if (!method.isAnnotationPresent(Semaphored.class)) {
			return null;
		}
		return null;
	}
	
	public MethodHandle get(Method method) {
		return handleMap.get(method);
	}
	
	public MethodHandle remove(Method method) {
		return handleMap.remove(method);
	}
	
	public Set<Method> keySet() {
		return handleMap.keySet();
	}
}
