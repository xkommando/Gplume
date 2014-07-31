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
package com.caibowen.gplume.core;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.caibowen.gplume.annotation.Semaphored;


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
