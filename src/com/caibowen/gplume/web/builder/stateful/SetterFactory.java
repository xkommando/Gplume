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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Map;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.annotation.ContextAttr;
import com.caibowen.gplume.web.annotation.CookieAttr;
import com.caibowen.gplume.web.annotation.ReqAttr;
import com.caibowen.gplume.web.annotation.SessionAttr;



public class SetterFactory {

	/**
	 *  nullable -> silent Req.get faild ? null
	 *  
	 *  precast and get default Object, do not invoke exact
	 *  nonnull -> silent Req.get failed? -> default;
	 * 
	 * 
	 *  nonull no default -> warning
	 * 
	 */
	
	Map<Class<? extends Annotation>, MethodHandle> 
	annoHandleMap = new HashMap<Class<? extends Annotation>, MethodHandle>(32);
	
	public void name() throws NoSuchMethodException, SecurityException, IllegalAccessException {
		
		
		Class<RequestContext> klass = RequestContext.class;
		Converter.slient.translateStr("", Integer.class);
		Lookup look = MethodHandles.publicLookup();
//		public<T> T contextAttr(String name) {
		annoHandleMap.put(ContextAttr.class, look.unreflect(
				klass.getMethod("contextAttr", String.class)
				));

//		public<T> T sessionAttr(String name) {
		annoHandleMap.put(SessionAttr.class, look.unreflect(
				klass.getMethod("sessionAttr", String.class)
				));

//		public String cookieVal(String name) {
		annoHandleMap.put(CookieAttr.class, look.unreflect(
				klass.getMethod("cookieVal", String.class)
				));

//		public<T> T attr(String key) {
		annoHandleMap.put(ReqAttr.class, look.unreflect(
				klass.getMethod("attr", String.class)
				));
		
	}
}
