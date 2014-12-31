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

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.stateful.setters.BeanSetter;
import com.caibowen.gplume.web.actions.stateful.setters.ReqDefaultValSetter;
import com.caibowen.gplume.web.actions.stateful.setters.ReqParamDefaultValSetter;
import com.caibowen.gplume.web.actions.stateful.setters.ReqSetter;
import com.caibowen.gplume.web.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <pre>
 * create setters for field annotated with:
 * @ContextAttr
 * @CookieVal
 * @ReqAttr
 * @SessionAttr
 * @ReqParam // no default val
 * @PathVal
 * 
 * @Inject
 * 
 * Strategies:
 * 
 * required ?
 * annotated with @Nonnull && required
 * 
 * name ? @Named value 
 * 			or @ReqAttr value 
 * 				or field name
 * 
 * 
 * @Inject : BeanSetter
 * 
 * @ContextAttr
 * @CookieVal
 * @ReqAttr
 * @SessionAttr
 * 1. get method handle by annotaion class hashcode
 * 2. has default value -> ReqDefaultValSetter
 * otherwise -> ReqSetter
 * 
 * @ReqParam
 *  1. field type(int bool ...) primitive ? to wrapper class
 *  2. get method handle
 *  
 *  no default:
 *   h = wrapper class hashcode  -> reqc.getDoubleParam("");
 *   ReqSetter
 *   
 *  if has default value 
 *  h = * wrapper class hashcode * 1231 -> reqc.getDoubleParam("", 0.0);
 *  ReqParamDefaultValSetter
 *  
 * 
 * </pre>
 * @author BowenCai
 *
 */
public class SetterFactory {
	
	
	private static void setAccessible(Field field) {
		
		if (!field.isAccessible())
			try {
				field.setAccessible(true);
			} catch (Exception e) {
				throw new RuntimeException(
					"Could not set not accessible for field [" + field +"]");
			}
	}
	/**
	 * field will be set accessible if necessary
	 * @param field
	 * @return null if no correspondent annotation found
	 */
	@Nullable
	public static IStateSetter createSetter(Field field) {
		
		/**
		 * Inject
		 */
		if (field.isAnnotationPresent(Inject.class)
				|| field.isAnnotationPresent(Named.class)) {
			setAccessible(field);
			return new BeanSetter(field
					, AppContext.beanAssembler
					, named(field, field.getName())
					, nullable(field));
			
			/**
			 * ContextAttr: request context
			 */
		} else if (field.isAnnotationPresent(ContextAttr.class)) {

			setAccessible(field);
			ContextAttr ann = field.getAnnotation(ContextAttr.class);
			MethodHandle handle = anno2HandleMap.get(ContextAttr.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
			/**
			 * CookieVal: value from cookie
			 */
		} else if (field.isAnnotationPresent(CookieVal.class)) {

			setAccessible(field);
			CookieVal ann = field.getAnnotation(CookieVal.class);
			MethodHandle handle = anno2HandleMap.get(CookieVal.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
			/**
			 * ReqAttr: this request
			 */
		} else if (field.isAnnotationPresent(ReqAttr.class)) {

			setAccessible(field);
			ReqAttr ann = field.getAnnotation(ReqAttr.class);
			MethodHandle handle = anno2HandleMap.get(ReqAttr.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
			/**
			 * SessionAttr
			 */
		} else if (field.isAnnotationPresent(SessionAttr.class)) {

			setAccessible(field);
			SessionAttr ann = field.getAnnotation(SessionAttr.class);
			MethodHandle handle = anno2HandleMap.get(SessionAttr.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
		}
		// rest API not supported
//		else if (field.isAnnotationPresent(PathVal.class)) {
//			PathVal ann = field.getAnnotation(PathVal.class);
//			MethodHandle handle = anno2HandleMap.get(PathVal.class);
//			return Str.Utils.notBlank(ann.defaultVal()) ? 
//					new PathValDefaltSetter(handle, field, required(field) && ann.required(), , p)
//			
//		}
		/**
		 * ReqParam
		 */
		else if (field.isAnnotationPresent(ReqParam.class)) {

			setAccessible(field);
			
			ReqParam ann = field.getAnnotation(ReqParam.class);
			Class<?> cls = field.getType();
			if (cls.isPrimitive())
				cls = Klass.primitiveToWrapper(cls);
			int hash = cls.hashCode();
			boolean hasDefault = false;
			if (Str.Utils.notBlank(ann.defaultVal())) {
				hasDefault = true;
				hash *= 1231;
			}
			
			MethodHandle handle = anno2HandleMap.get(hash);
			return hasDefault ?
					new ReqParamDefaultValSetter(handle
					, named(field, ann.value())
					, field
					, nullable(field) || ann.required()
					, Converter.slient.translateStr(ann.defaultVal(), field.getType()))
			
			: new ReqSetter(handle
					, named(field, ann.value())
					, field
					, nullable(field) && ann.required());
		}
		return null;
//		throw new IllegalArgumentException("Could not build setter for [" + field + "] : no specified annotation found");
	}

	/**
	 * create setters for field annotated with:
	 * 
	 * @ContextAttr
	 * @CookieVal
	 * @ReqAttr
	 * @SessionAttr
	 * 
	 */
	private static IStateSetter reqSetter(
										MethodHandle handle,
										Field field, 
										String annName, 
										String defaultVal, 
										boolean annNullable) {
		
		boolean nul = nullable(field) && annNullable;
		String name = named(field, annName);
		
		return Str.Utils.notBlank(defaultVal) ?
		 new ReqDefaultValSetter(handle
				, name, field, nul
				, Converter.slient.translateStr(
						defaultVal
						, field.getType()))
		: new ReqSetter(handle, name, field, nul);
	}
	
	/**
	 * check Nonnull and Nullable
	 * @param field
	 * @return true if no @Nonnull or @Nullable
	 */
	private static final boolean nullable(Field field) {
		if (field.isAnnotationPresent(Nonnull.class))
			return false;
		else if (field.isAnnotationPresent(Nullable.class))
			return true;
		return true;
	}
	
	/**
	 * @Named.value()
	 *   -> annotation.value()
	 *   	->field name
	 *   
	 * @param field
	 */
	private static final String named(Field field, String annName) {
		Named n = field.getAnnotation(Named.class);
		if (n != null) {
			String _t = n.value();
			if (Str.Utils.notBlank(_t))
				return _t;
		}
		return Str.Utils.notBlank(annName) ? annName : field.getName();
	}
	
	/**
	 *  required -> silent Req.get faild ? null
	 *  
	 *  precast and get default Object, do not invoke exact
	 *  nonnull -> silent Req.get failed? -> default;
	 * 
	 * 
	 *  nonull no default -> warning
	 * 
	 */
	private static final Map<Integer, MethodHandle> 
	anno2HandleMap = new HashMap<Integer, MethodHandle>(64);
	
	static {
		try {
		
		Class<RequestContext> klass = RequestContext.class;
//		Converter.slient.translateStr("", Integer.class);
		Lookup look = MethodHandles.publicLookup();
//		public<T> T contextAttr(String id) {
		anno2HandleMap.put(ContextAttr.class.hashCode(), look.unreflect(
				klass.getMethod("contextAttr", String.class)
				));

//		public<T> T sessionAttr(String id) {
		anno2HandleMap.put(SessionAttr.class.hashCode(), look.unreflect(
				klass.getMethod("sessionAttr", String.class)
				));

//		public String cookieVal(String id) {
		anno2HandleMap.put(CookieVal.class.hashCode(), look.unreflect(
				klass.getMethod("cookieVal", String.class)
				));

//		public<T> T attr(String key) {
		anno2HandleMap.put(ReqAttr.class.hashCode(), look.unreflect(
				klass.getMethod("attr", String.class)
				));

		// 6 * 4 = 24
//        bool hashCode: return value ? 1231 : 1237;
		// non-null ? Multiply 1231
//		RequestContext reqc = null;
//		reqc.getBoolParam("");
		anno2HandleMap.put(Boolean.class.hashCode(), look.unreflect(
				klass.getMethod("getBoolParam", String.class)
				));
//		reqc.getBoolParam("", true);
		anno2HandleMap.put(Boolean.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getBoolParam", String.class, boolean.class)
				));
//		reqc.getBoolsParam("");
		anno2HandleMap.put(Boolean[].class.hashCode(), look.unreflect(
				klass.getMethod("getBoolsParam", String.class)
				));
//		reqc.getBoolsParam("", Collections.EMPTY_LIST);
		anno2HandleMap.put(Boolean[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getBoolsParam", String.class, List.class)
				));
		
//		reqc.getDoubleParam("");
		anno2HandleMap.put(Double.class.hashCode(), look.unreflect(
				klass.getMethod("getDoubleParam", String.class)
				));
//		reqc.getDoubleParam("", 0.0);
		anno2HandleMap.put(Double.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getDoubleParam", String.class, double.class)
				));
//		reqc.getDoublesParam("");
		anno2HandleMap.put(Double[].class.hashCode(), look.unreflect(
				klass.getMethod("getDoublesParam", String.class)
				));
//		reqc.getDoublesParam("", Collections.EMPTY_LIST);
		anno2HandleMap.put(Double[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getDoublesParam", String.class, List.class)
				));
		
//		reqc.getFloatParam("");
		anno2HandleMap.put(Float.class.hashCode(), look.unreflect(
				klass.getMethod("getFloatParam", String.class)
				));
//		reqc.getFloatParam("", 0.0F);
		anno2HandleMap.put(Float.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getFloatParam", String.class, float.class)
				));
//		reqc.getFloatsParam("");
		anno2HandleMap.put(Float[].class.hashCode(), look.unreflect(
				klass.getMethod("getFloatsParam", String.class)
				));
//		reqc.getFloatsParam("", Collections.EMPTY_LIST);
		anno2HandleMap.put(Float[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getFloatsParam", String.class, List.class)
				));
		
//		reqc.getIntParam("");
		anno2HandleMap.put(Integer.class.hashCode(), look.unreflect(
				klass.getMethod("getIntParam", String.class)
				));
//		reqc.getIntParam("", 0);
		anno2HandleMap.put(Integer.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getIntParam", String.class, int.class)
				));
//		reqc.getIntsParam("");
		anno2HandleMap.put(Integer[].class.hashCode(), look.unreflect(
				klass.getMethod("getIntsParam", String.class)
				));
//		reqc.getIntsParam("", Collections.EMPTY_LIST);
		anno2HandleMap.put(Integer[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getIntsParam", String.class, List.class)
				));
		//------- long
//		reqc.getLongParam("");
		anno2HandleMap.put(Long.class.hashCode(), look.unreflect(
				klass.getMethod("getLongParam", String.class)
				));
//		reqc.getLongParam("", 0L);
		anno2HandleMap.put(Long.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getLongParam", String.class, long.class)
				));
//		reqc.getLongsParam("");
		anno2HandleMap.put(Long[].class.hashCode(), look.unreflect(
				klass.getMethod("getLongsParam", String.class)
				));
//		reqc.getLongsParam("", Collections.EMPTY_LIST);
		anno2HandleMap.put(Long[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getLongsParam", String.class, List.class)
				));
		
//		reqc.getStrParam("");
		anno2HandleMap.put(String.class.hashCode(), look.unreflect(
				klass.getMethod("getStrParam", String.class)
				));
//		reqc.getStrParam("", "default");
		anno2HandleMap.put(String.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getStrParam", String.class, String.class)
				));
		//
//		reqc.getStrArrayParam("");
		anno2HandleMap.put(String[].class.hashCode(), look.unreflect(
				klass.getMethod("getStrArrayParam", String.class)
				));
		
//		reqc.getStrArrayParam("", null);
		anno2HandleMap.put(String[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getStrArrayParam", String.class, String[].class)
				));
		
		} catch (Exception e) {
			throw new RuntimeException("Could not construct SetterFactory: no getter methodHandle found from RequestContext", e);
		}
	}
}
