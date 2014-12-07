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

import com.caibowen.gplume.misc.Klass;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Traits type information, as well as providing reflection utilities
 * 
 * @author BowenCai
 *
 */
public final class TypeTraits {
	private TypeTraits() {}
	/**
	 * @param klass
	 * @param fieldName
	 * @return generic type actual type arg
	 * @throws NoSuchFieldException
	 */
	public static List<Class<?>> 
	findParamTypes(Class<?> klass, String fieldName) throws NoSuchFieldException {
		
		Set<Field> fields = Klass.getEffectiveField(klass);
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				Type type = field.getGenericType();
				if (type instanceof ParameterizedType) {
					Type[] argTps = ((ParameterizedType)type).getActualTypeArguments();
					ArrayList<Class<?>> klasses = new ArrayList<>(argTps.length);
					for (Type t : argTps) {
						klasses.add((Class<?>)t);
					}
					return klasses;
				}
			}
		}
		// no such field
		throw new NoSuchFieldException(
				"cannnot find field of id[" + fieldName 
				+ "] in class [" + klass.getName() + "]");
	}

	public static Class<?> getClass(Type type, int i) {
		
        if (type instanceof ParameterizedType) {
            return getGenericClass((ParameterizedType) type, i);
        } else if (type instanceof TypeVariable) {
            return (Class<?>) getClass(((TypeVariable<?>) type).getBounds()[0], 0);
        } else {
            return (Class<?>) type;
        }
    }

    public static Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {

        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        
        if (genericClass instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericClass).getRawType();
            
        } else if (genericClass instanceof GenericArrayType) {
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
            
        } else if (genericClass instanceof TypeVariable) {

            return (Class<?>) getClass(((TypeVariable<?>) genericClass).getBounds()[0], 0);
            
        } else {
            return (Class<?>) genericClass;
        }
    }

    public static void assignField(Object object,
    								String fieldName, 
    								Object var) throws IllegalAccessException, NoSuchFieldException {
    	
    	assignField(object, fieldName, var, false);
    }
	
    /**
     * set field of this id with this var
     * @param object
     * @param fieldName
     * @param var
     * @param refPrivate whether set accessble or not
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static void assignField(Object object,
    								String fieldName, 
    								Object var,
    								boolean refPrivate) throws IllegalAccessException, NoSuchFieldException {
    	
    	Class<?> clazz = object.getClass();
    	Field[] fields = clazz.getDeclaredFields();
    	
    	for (Field field : fields) {
			if (field.getName().equals(fieldName)) {

		    	Class<?> fieldClazz = field.getType();
//System.out.println(" field class " + fieldClazz.getName());
				Object realVar = null;
				
				if (var instanceof String) {
					realVar = Converter.slient.translateStr((String)var, fieldClazz);
				} else if (var instanceof Number) {
					realVar = Converter.castNumber((Number)var, fieldClazz);
				} else {
					realVar = var;
				}
				if (Klass.isAssignable(fieldClazz, realVar.getClass())) {
					if ( !field.isAccessible()) {
						if (refPrivate) {
							field.setAccessible(true);
						} else {
							throw new IllegalAccessException("field is private");
						}
					}
					field.set(object, realVar);
				} else {
					throw new IllegalArgumentException(
							"cannot assign " + realVar + " to field " + fieldName);
				}
				return;
			}
		}
    	throw new NoSuchFieldException(
    			"cannot find [" + fieldName + "] in class [" + clazz.getName() + "]");
	}

}







