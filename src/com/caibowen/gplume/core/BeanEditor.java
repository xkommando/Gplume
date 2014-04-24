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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 
 * @author BowenCai
 *
 */
public class BeanEditor {

	/**
	 * set property, 
	 * first look for public setter for this name
	 * second, look for public field of this name
	 * the property can be virtual, that is, no underlying field exists.
	 * like : 
	 * public void setProp(Type var) {
	 *      // there is no Type prop.
	 * 		// doSomeThingElse
	 * }
	 * @param bean
	 * @param propName
	 * @param var
	 * @throws Exception
	 */
	public static void setBeanProperty(@Nullable Object bean,
										@Nonnull String propName, 
										Object var) throws Exception {

		Class<?> bnClass = bean.getClass(); 
		Method setter = TypeTraits.findSetter(bnClass, propName);
		if (setter == null) {
			setField(bean, propName, var);
		} else {
			callSetter(bean, propName, var);
		}
	}

	/**
	 * assign list or array
	 * 
	 * @param bnClass
	 * @param bean
	 * @param propName
	 * @param varList
	 * @throws Exception
	 */
	public static void setListProperty(@Nullable Object bean,
										@Nonnull String propName, 
										List<?> varList) throws Exception {

		Class<?> bnClass = bean.getClass(); 
		Method setter = TypeTraits.findSetter(bnClass, propName);

		if (setter == null) {
			Field field = bnClass.getField(propName);
			if (field.getType().isAssignableFrom(varList.getClass())) {
				setField(bean, propName, varList);
				
			} else {
				@SuppressWarnings("unchecked")
				Object var = Converter.translateList((List<String>) varList,
														bnClass, propName);
				setField(bean, propName, var);
			}
			
		} else {
			Class<?>[] paramTypes = setter.getParameterTypes();
			if (paramTypes != null && paramTypes.length == 1) {
				if (paramTypes[0].isAssignableFrom(varList.getClass())) {
					setter.invoke(bean, varList);
				} else {
					@SuppressWarnings("unchecked")
					Object var = Converter.translateList((List<String>) varList,
															bnClass, propName);
					callSetter(bean, propName, var);
				}
			}
		}
		
	}
	
	/**
	 * set property by set public filed
	 * @param obj
	 * @param fieldName
	 * @param var
	 */
	public static void setField(@Nonnull Object obj, 
								@Nonnull String fieldName, 
								Object var) {

		try {
			Field field = obj.getClass().getField(fieldName);
			if (var instanceof String) {
				var = Converter.slient.translateStr((String)var, field.getType());
				if (var == null) {
					throw new IllegalArgumentException("cannot translate[" 
							+ var + "] to [" + field.getType() + "] in field[" 
							+ fieldName + "] in class[" + obj.getClass().getName() +"]");
				}
			}
			if (Modifier.isStatic(field.getModifiers())) {
				field.set(null, var);
			} else {
				field.set(obj, var);
			}
		} catch (Exception e) {
			throw new IllegalStateException(
			"in class [" + obj.getClass().getName()
			+ "]  cannot find public setter for [" + fieldName 
			+ "] and cannot find public field of this name", e);
		}
	}

	/**
	 * set property by public setter
	 * @param obj
	 * @param propName
	 * @param var
	 * @throws NoSuchMethodException
	 */
	public static void callSetter(@Nullable Object obj, 
									@Nonnull String propName, 
									Object var) throws NoSuchMethodException {
		
		Method setter = TypeTraits.findSetter(obj.getClass(), propName);
		
		Class<?>[] paramTypes = setter.getParameterTypes();
		if (paramTypes.length != 1) {
			throw new IllegalStateException("in class [" + obj.getClass().getName()
					+ "]   setter [" + setter.getName()
					+ "] has more than one parameters");
		}
		try {
			if (var instanceof String) {
				var = Converter.slient
							.translateStr((String) var, paramTypes[0]);
				if (var == null) {
					throw new IllegalArgumentException("cannot translate["
							+ var + "] to ["
							+ paramTypes[0].getClass().getName()
							+ "] in field[" + propName + "] in class["
							+ obj.getClass().getName() + "]");
				}
			}
			if (Modifier.isStatic(setter.getModifiers())) {
				setter.invoke(null, var);
			} else {
				setter.invoke(obj, var);
			}
		} catch (Exception e) {
			throw new RuntimeException(
				"cannot set bean [" 
				+ obj.getClass().getSimpleName() 
				+  "] with setter [" + setter.getName() + "]"
				+ " require parameter type[" + paramTypes[0].getSimpleName() 
				+ "] get property [" + (var == null ? "null" : var.getClass().getSimpleName())
				+ "]", e);
			}
	}
	
}
