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

import java.lang.reflect.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.caibowen.gplume.misc.Klass;

/**
 * set static or non-static property based on id,
 * if there is a public setter, invoke it
 * if no setter is found, find the public or private field with the same id, and set it
 * 
 * @author BowenCai
 *
 */
public class BeanEditor {

    public static Object construct(Class klass, Object param) throws Exception {
        for (Constructor ctor : klass.getDeclaredConstructors()) {
            Class[] ps = ctor.getParameterTypes();
            if (ps.length == 1 && Klass.isAssignable(param.getClass(), ps[0])) {
                if (!ctor.isAccessible())
                    ctor.setAccessible(true);
                return ctor.newInstance(param);
            }
        }
        throw new NoSuchMethodException("cannot find constructor for class [" + klass.getName()
                +"] that can be invoked with [" + param + "]");
    }
	/**
	 * set property, 
	 * first look for public setter for this id
	 * second, look for public field of this id
	 * the property can be virtual, that is, no underlying field exists.
	 * like : 
	 * public void setProp(Type var) {
	 *      // there is no Type prop.
	 * 		// doSomeThingElse
	 * }
	 * @param bean can be null if is static property
	 * @param propName
	 * @param var
	 * @throws Exception
	 */
	public static void setProperty(@Nullable Object bean,
										@Nonnull String propName, 
										Object var) throws Exception {

		Class<?> bnClass = bean.getClass(); 
		Method setter = TypeTraits.findSetter(bnClass, propName);
		if (setter == null) {
			setField(bean, propName, var);
		} else {
			callSetter(bean, setter, var);
		}
	}

	/**
	 * assign list or array
	 * the string element of the list will be casted
	 * 
	 * @param bnClass
	 * @param bean can be null if is static property
	 * @param propName
	 * @param varList
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void setListProperty(@Nullable Object bean,
										@Nonnull String propName, 
										List<?> varList) throws Exception {

		Class<?> bnClass = bean.getClass();
		// try get setter
		Method setter = TypeTraits.findSetter(bnClass, propName);
		// try convert list as String list
		Object var = null;
		try {
			var = Converter.translateList((List<String>)varList, bnClass, propName);
		} catch (Exception e) {
			var = null;
		}
		
		// get right var
		if (var != null) {
			if (setter != null) {
				invokeIfValid(setter, bean, var);
			} else {
				// no setter, try set public field
				setField(bean, propName, var);
			}
			
		} else {// failed convert list
			if (setter != null) {
				invokeIfValid(setter, bean, varList);
			} else {
				throw new IllegalArgumentException(
						"no public setter or filed found for [" + propName + "] in object[" + bean);
			}
			
		}
	}
//		
//		
//		
//		
//		if (setter == null) {
//			Field field = bnClass.getField(propName);
//			if (field.getType().isAssignableFrom(varList.getClass())) {
//				setField(bean, propName, varList);
//			} else {
//				@SuppressWarnings("unchecked")
//				Object var = Converter.translateList((List<String>) varList,
//														bnClass, propName);
//				setField(bean, propName, var);
//			}
//			
//		} else {
//			Class<?>[] paramTypes = setter.getParameterTypes();
//			if (paramTypes != null && paramTypes.length == 1) {
//				Class<?> targetClass = null;
//				try {
//					targetClass = bnClass.getDeclaredField(propName).getType();
//				} catch (Exception e) {}
//				if (targetClass == null) {
//					targetClass = paramTypes[0];
//				}
//				
//				if (Klass.isAssignable(varList.getClass(), targetClass)) {
//					setter.invoke(bean, varList);
//System.out.println("1BeanEditor.setListProperty()");
//				} else {
//					@SuppressWarnings("unchecked")
//					Object var = Converter.translateList((List<String>) varList,
//															bnClass, propName);
//					callSetter(bean, propName, var);
//System.out.println("2BeanEditor.setListProperty()");
//				}
//			} else {
//				throw new IllegalArgumentException(
//					"more than one parameter in setter[" + setter.getName() +"]"
//					+ "in class[" + bean.getClass().getName() + "]");
//			}
//		}
//	}
	
	
	public static void invokeIfValid(@Nonnull Method setter, 
										@Nullable Object obj, 
										@Nonnull Object var) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// get setter, check again
		Class<?>[] paramTypes = setter.getParameterTypes();
		if (paramTypes != null && paramTypes.length == 1) {
			if (Klass.isAssignable(var.getClass(), paramTypes[0])) {
				// every thing is OK
				if (Modifier.isStatic(setter.getModifiers())) {
					setter.invoke(null, var);
				} else {
					setter.invoke(obj, var);
				}
			} else {
				throw new IllegalArgumentException(
					"cannot call setter[" 
						+ setter.getName() + "] in object [" 
						+ obj + "]"
						+ "] setter parameter type[" + paramTypes[0].getName()
						+ "] value type [" + var.getClass().getName() + "]");
			}
		} else {
			throw new IllegalArgumentException(
				"more than one parameter in setter[" + setter.getName() +"]"
				+ "in object [" + obj + "]");
		}
	}
	
	/**
	 * set property by set public or private filed
	 * @param obj
	 * @param fieldName
	 * @param var
	 */
	public static void setField(@Nullable Object obj, 
								@Nonnull String fieldName, 
								Object var) {

		try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
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
			+ "] and cannot find public field of this id", e);
		}
	}

	/**
	 * find setter by id and
	 * set property with the public setter
	 * 
	 * @param obj
	 * @param propName
	 * @param var
	 * @throws NoSuchMethodException
	 */
	public static void callSetter(@Nullable Object obj, 
									@Nonnull Method setter,
									Object var) throws NoSuchMethodException {
		
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
							+ "] in field[" + setter.getName() + "] in class["
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