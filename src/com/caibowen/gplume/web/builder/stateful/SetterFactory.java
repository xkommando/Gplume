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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.annotation.ContextAttr;
import com.caibowen.gplume.web.annotation.CookieVal;
import com.caibowen.gplume.web.annotation.ReqAttr;
import com.caibowen.gplume.web.annotation.ReqParam;
import com.caibowen.gplume.web.annotation.SessionAttr;
import com.caibowen.gplume.web.builder.stateful.setters.BeanSetter;
import com.caibowen.gplume.web.builder.stateful.setters.ReqDefaultValSetter;
import com.caibowen.gplume.web.builder.stateful.setters.ReqParamDefaultValSetter;
import com.caibowen.gplume.web.builder.stateful.setters.ReqSetter;


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
 * nullable ? 
 * annotated with @Nonnull && nullable
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
	
	/**
	 * field will be set accessible if necessary
	 * @param field
	 * @return null if no correspondent annotation found
	 */
	@Nullable
	public static IStateSetter createSetter(Field field) {
		
		/**
		 * setAccessible
		 */
		if (!field.isAccessible())
			try {
				field.setAccessible(true);
			} catch (Exception e) {
				throw new RuntimeException(
					"cannot set not accessaible for field ["
						+ field.getName() + "] in class ["
						+ field.getDeclaringClass().getName() + "]");
			}
		
		/**
		 * Inject
		 */
		if (field.isAnnotationPresent(Inject.class)
				|| field.isAnnotationPresent(Named.class)) {

			return new BeanSetter(field
					, AppContext.beanAssembler
					, named(field, field.getName())
					, nullable(field));
			
			/**
			 * ContextAttr
			 */
		} else if (field.isAnnotationPresent(ContextAttr.class)) {
			
			ContextAttr ann = field.getAnnotation(ContextAttr.class);
			MethodHandle handle = annoHandleMap.get(ContextAttr.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
			/**
			 * CookieVal
			 */
		} else if (field.isAnnotationPresent(CookieVal.class)) {

			CookieVal ann = field.getAnnotation(CookieVal.class);
			MethodHandle handle = annoHandleMap.get(CookieVal.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
			/**
			 * ReqAttr
			 */
		} else if (field.isAnnotationPresent(ReqAttr.class)) {

			ReqAttr ann = field.getAnnotation(ReqAttr.class);
			MethodHandle handle = annoHandleMap.get(ReqAttr.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
			/**
			 * SessionAttr
			 */
		} else if (field.isAnnotationPresent(SessionAttr.class)) {

			SessionAttr ann = field.getAnnotation(SessionAttr.class);
			MethodHandle handle = annoHandleMap.get(SessionAttr.class.hashCode());
			
			return reqSetter(handle, field, ann.value(), ann.defaultVal(), ann.nullable());
			
		}
		
//		else if (field.isAnnotationPresent(PathVal.class)) {
//			PathVal ann = field.getAnnotation(PathVal.class);
//			MethodHandle handle = annoHandleMap.get(PathVal.class);
//			return Str.Utils.notBlank(ann.defaultVal()) ? 
//					new PathValDefaltSetter(handle, field, nullable(field) && ann.nullable(), , p)
//			
//		}
		
		/**
		 * ReqParam
		 */
		else if (field.isAnnotationPresent(ReqParam.class)) {
			ReqParam ann = field.getAnnotation(ReqParam.class);
			Class<?> cls = field.getType();
			if (cls.isPrimitive())
				cls = Klass.primitiveToWrapper(cls);
			int hash = cls.hashCode();
			boolean hasDefault = false;
			if (Str.Utils.notBlank(ann.defaultVal())) {
				hash *= 1231;
				hasDefault = true;
			}
			
			MethodHandle handle = annoHandleMap.get(hash);
			return hasDefault ? new ReqParamDefaultValSetter(handle
					, named(field, ann.value())
					, field
					, nullable(field) && ann.nullable()
					, Converter.slient.translateStr(ann.defaultVal(), field.getType()))
			
			: new ReqSetter(handle, named(field, ann.value()), field, nullable(field) && ann.nullable());
		}
		return null;
	}

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
	 * 
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
	 * @Named
	 *   -> annotation.value()
	 *   	->field name
	 *   
	 * @param field
	 */
	private static final String named(Field field, String annName) {
		Named n = field.getAnnotation(Named.class);
		if (n != null) {
			String _ = n.value();
			if (Str.Utils.notBlank(_))
				return _;
		}
		return Str.Utils.notBlank(annName) ? annName : field.getName();
	}
	
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
	private static final Map<Integer, MethodHandle> 
	annoHandleMap = new HashMap<Integer, MethodHandle>(64);
	
	static {
		try {
			
		
		Class<RequestContext> klass = RequestContext.class;
//		Converter.slient.translateStr("", Integer.class);
		Lookup look = MethodHandles.publicLookup();
//		public<T> T contextAttr(String id) {
		annoHandleMap.put(ContextAttr.class.hashCode(), look.unreflect(
				klass.getMethod("contextAttr", String.class)
				));

//		public<T> T sessionAttr(String id) {
		annoHandleMap.put(SessionAttr.class.hashCode(), look.unreflect(
				klass.getMethod("sessionAttr", String.class)
				));

//		public String cookieVal(String id) {
		annoHandleMap.put(CookieVal.class.hashCode(), look.unreflect(
				klass.getMethod("cookieVal", String.class)
				));

//		public<T> T attr(String key) {
		annoHandleMap.put(ReqAttr.class.hashCode(), look.unreflect(
				klass.getMethod("attr", String.class)
				));

		// 6 * 4 = 24
//        bool hashCode: return value ? 1231 : 1237;
		// non-null ? Multiply 1231
//		RequestContext reqc = null;
//		reqc.getBoolParam("");
		annoHandleMap.put(Boolean.class.hashCode(), look.unreflect(
				klass.getMethod("getBoolParam", String.class)
				));
//		reqc.getBoolParam("", true);
		annoHandleMap.put(Boolean.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getBoolParam", String.class, boolean.class)
				));
//		reqc.getBoolsParam("");
		annoHandleMap.put(Boolean[].class.hashCode(), look.unreflect(
				klass.getMethod("getBoolsParam", String.class)
				));
//		reqc.getBoolsParam("", Collections.EMPTY_LIST);
		annoHandleMap.put(Boolean[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getBoolsParam", String.class, List.class)
				));
		
//		reqc.getDoubleParam("");
		annoHandleMap.put(Double.class.hashCode(), look.unreflect(
				klass.getMethod("getDoubleParam", String.class)
				));
//		reqc.getDoubleParam("", 0.0);
		annoHandleMap.put(Double.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getDoubleParam", String.class, double.class)
				));
//		reqc.getDoublesParam("");
		annoHandleMap.put(Double[].class.hashCode(), look.unreflect(
				klass.getMethod("getDoublesParam", String.class)
				));
//		reqc.getDoublesParam("", Collections.EMPTY_LIST);
		annoHandleMap.put(Double[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getDoublesParam", String.class, List.class)
				));
		
//		reqc.getFloatParam("");
		annoHandleMap.put(Float.class.hashCode(), look.unreflect(
				klass.getMethod("getFloatParam", String.class)
				));
//		reqc.getFloatParam("", 0.0F);
		annoHandleMap.put(Float.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getFloatParam", String.class, float.class)
				));
//		reqc.getFloatsParam("");
		annoHandleMap.put(Float[].class.hashCode(), look.unreflect(
				klass.getMethod("getFloatsParam", String.class)
				));
//		reqc.getFloatsParam("", Collections.EMPTY_LIST);
		annoHandleMap.put(Float[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getFloatsParam", String.class, List.class)
				));
		
//		reqc.getIntParam("");
		annoHandleMap.put(Integer.class.hashCode(), look.unreflect(
				klass.getMethod("getIntParam", String.class)
				));
//		reqc.getIntParam("", 0);
		annoHandleMap.put(Integer.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getIntParam", String.class, int.class)
				));
//		reqc.getIntsParam("");
		annoHandleMap.put(Integer[].class.hashCode(), look.unreflect(
				klass.getMethod("getIntsParam", String.class)
				));
//		reqc.getIntsParam("", Collections.EMPTY_LIST);
		annoHandleMap.put(Integer[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getIntsParam", String.class, List.class)
				));
		//------- long
//		reqc.getLongParam("");
		annoHandleMap.put(Long.class.hashCode(), look.unreflect(
				klass.getMethod("getLongParam", String.class)
				));
//		reqc.getLongParam("", 0L);
		annoHandleMap.put(Long.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getLongParam", String.class, long.class)
				));
//		reqc.getLongsParam("");
		annoHandleMap.put(Long[].class.hashCode(), look.unreflect(
				klass.getMethod("getLongsParam", String.class)
				));
//		reqc.getLongsParam("", Collections.EMPTY_LIST);
		annoHandleMap.put(Long[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getLongsParam", String.class, List.class)
				));
		
//		reqc.getStrParam("");
		annoHandleMap.put(String.class.hashCode(), look.unreflect(
				klass.getMethod("getStrParam", String.class)
				));
//		reqc.getStrParam("", "default");
		annoHandleMap.put(String.class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getStrParam", String.class, String.class)
				));
		
		//
//		reqc.getStrArrayParam("");
		annoHandleMap.put(String[].class.hashCode(), look.unreflect(
				klass.getMethod("getStrArrayParam", String.class)
				));
		
//		reqc.getStrArrayParam("", null);
		annoHandleMap.put(String[].class.hashCode() * 1231, look.unreflect(
				klass.getMethod("getStrArrayParam", String.class, String[].class)
				));
		
		} catch (Exception e) {
			throw new RuntimeException("cannot init SetterFactory", e);
		}
	}
}
