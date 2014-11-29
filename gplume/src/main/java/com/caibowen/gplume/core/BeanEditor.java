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

import com.caibowen.gplume.misc.Assert;
import com.caibowen.gplume.misc.Klass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;

/**
 * set static or non-static property based on id,
 * if there is a public setter, invoke it
 * if no setter is found, find the public or private field with the same id, and set it
 * 
 * @author BowenCai
 *
 */
public class BeanEditor {

	/**
	 * This is a compile flag.
	 * When this flag is enabled,fields that do not have a correspondent setter
	 * will be set directly, regardless of its qualifier.
	 *
	 * However, In some environment, e.g., Google App Engine,
	 * you cannot reflect on private field on some classes
	 * due to different security policy.
	 * So it is recommended that this flag is not open.
	 *
	 */
	public static final boolean	REFLECT_ON_PRIVATE = true;

	public static Object construct(Class klass, Object[] params) throws Exception {
		Constructor candi = null;
		int match = Integer.MAX_VALUE;
		for (Constructor ctor : klass.getDeclaredConstructors()) {
			Class<?>[] ps = ctor.getParameterTypes();
			if (params.length != ps.length)
				continue;
			int _s = typeDiff(ps, params);
			if (_s < match) {
				match = _s;
				candi = ctor;
			}
		}
		Assert.isTrue(match < Integer.MAX_VALUE,
				"Could not resolve matching constructor for [" + klass + "] with values[" + Arrays.toString(params));

		if (!candi.isAccessible() && REFLECT_ON_PRIVATE)
			candi.setAccessible(true);
		return candi.newInstance(params);
	}
    /***
     *
     * find the best suit constructor according to the parameter type and construct an instance
     *
     * @throws Exception
     */
    public static Object construct(Class klass, Object param) throws Exception {
        TreeMap<Integer, Constructor> q = new TreeMap<>();
        for (Constructor ctor : klass.getDeclaredConstructors()) {
            Class[] ps = ctor.getParameterTypes();
            if (ps.length == 1) {
                if (ps[0].equals(param.getClass())) {
                    q.put(1, ctor);
                    break;
                } else if (ps[0].isAssignableFrom(param.getClass())) {
                    q.put(2, ctor);
                } else if (Klass.isAssignable(param.getClass(), ps[0])) {
                    q.put(3, ctor);
                }
            }
        }
		Assert.isTrue(!q.isEmpty(),
				"Could not resolve matching constructor for ["
						+ klass + "] with values[" + param + "]");

		Constructor c = q.firstEntry().getValue();
		if (!c.isAccessible() && REFLECT_ON_PRIVATE)
			c.setAccessible(true);
		return c.newInstance(param);
	}

	public static int typeDiff(Class<?>[] paramTypes, Object[] args) {
		int result = 0;
		for (int i = 0; i < paramTypes.length; i++) {
			if (!Klass.isAssignableValue(paramTypes[i], args[i])) {
				return Integer.MAX_VALUE;
			}
			if (args[i] != null) {
				Class<?> paramType = paramTypes[i];
				Class<?> superClass = args[i].getClass().getSuperclass();
				while (superClass != null) {
					if (paramType.equals(superClass)) {
						result = result + 2;
						superClass = null;
					}
					else if (Klass.isAssignable(paramType, superClass)) {
						result = result + 2;
						superClass = superClass.getSuperclass();
					}
					else {
						superClass = null;
					}
				}
				if (paramType.isInterface()) {
					result = result + 1;
				}
			}
		}
		return result;
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
		Method setter = Klass.findSetter(bnClass, propName);
		if (setter == null) {
			setField(bean, propName, var);
		} else {
			callSetter(bean, setter, var);
		}
	}

	/**
	 * assign set
	 * the string element of the set will be casted
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void setSetProperty(@Nullable Object bean,
									   @Nonnull String propName,
									   List<?> varList) throws Exception {

		Class<?> bnClass = bean.getClass();
		// try get setter
		Method setter = Klass.findSetter(bnClass, propName);
		// try convert list as String list
		Object var = null;
		try {
			var = new HashSet<>(Converter.translateList((List<String>)varList, bnClass, propName));
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

	/**
	 * assign list or array
	 * the string element of the list will be casted
     *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void setListProperty(@Nullable Object bean,
										@Nonnull String propName, 
										List<?> varList) throws Exception {

		Class<?> bnClass = bean.getClass();
		// try get setter
		Method setter = Klass.findSetter(bnClass, propName);
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

	
	
	public static void invokeIfValid(@Nonnull Method setter, 
										@Nullable Object obj, 
										@Nonnull Object var) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// get setter, check again
		Class<?>[] paramTypes = setter.getParameterTypes();
		if (paramTypes != null && paramTypes.length == 1) {
			if (Klass.isAssignable(var.getClass(), paramTypes[0])) {
				// every thing is OK
				if (Modifier.isStatic(setter.getModifiers()))
					setter.invoke(null, var);
				else setter.invoke(obj, var);
				return;
			} else throw new IllegalArgumentException(
					"cannot call setter[" 
						+ setter.getName() + "] in object [" 
						+ obj + "]"
						+ "] setter parameter type[" + paramTypes[0].getName()
						+ "] value type [" + var.getClass().getName() + "]");
		}
		throw new IllegalArgumentException(
				"more than one parameter in setter[" + setter.getName() +"]"
				+ "in object [" + obj + "]");
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
			if (!field.isAccessible() && REFLECT_ON_PRIVATE)
				field.setAccessible(true);

			if (var instanceof String) {
				var = Converter.slient.translateStr((String)var, field.getType());
				if (var == null)
					throw new IllegalArgumentException("cannot translate[" 
							+ var + "] to [" + field.getType() + "] in field[" 
							+ fieldName + "] in class[" + obj.getClass().getName() +"]");
			}
			if (Modifier.isStatic(field.getModifiers()))
				field.set(null, var);
			else field.set(obj, var);
		} catch (Exception e) {
			throw new IllegalStateException(
			"in class [" + obj.getClass().getName()
			+ "]  cannot find public setter for [" + fieldName 
			+ "] and cannot find public field of this id", e);
		}
	}


	/**
	 *
	 * find setter by name and
	 * set property with the public setter, or reflection
	 *
	 * @param obj
	 * @param setter
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
			if (Modifier.isStatic(setter.getModifiers()))
				setter.invoke(null, var);
			else setter.invoke(obj, var);
		} catch (Exception e) {
			throw new RuntimeException(
				"cannot set bean [" 
				+ obj.getClass().getSimpleName() 
				+  "] with setter [" + setter.getName() + "]"
				+ " require parameter type[" + paramTypes[0].getName()
				+ "] get property [" + (var == null ? "null" : var.getClass().getName())
				+ "]", e);
			}
	}
	
}
