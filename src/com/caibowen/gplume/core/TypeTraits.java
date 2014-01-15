package com.caibowen.gplume.core;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Traits type information, as well as providing reflection utilities
 * 
 * @author BowenCai
 *
 */
public final class TypeTraits {
	
	/**
	 * no exception is thrown, but returns nullable getter
	 * 
	 * @param clazz
	 * @param fielddName
	 * @return
	 * @throws NoSuchMethodException 
	 */
	public static Method findSetter(Class<?> clazz, String fieldName) throws NoSuchMethodException {

		String setterName = String.format("set%C%s",
				fieldName.charAt(0), fieldName.substring(1));
		
		for (Method method : clazz.getMethods()) {

			if(method.getName().equals(setterName)
				&& method.getReturnType().getName().equals("void")) {
				return method;
			}
		}
		// no such field
		throw new NoSuchMethodException(
				"cannnot find setter for [" + fieldName 
				+ "] in class [" + clazz.getName() + "]");
	}
	
	/**
	 * @param clazz
	 * @param fieldName
	 * @return
	 * @throws NoSuchMethodException 
	 */
	public static Method findGetter(Class<?> clazz, String fieldName) throws NoSuchMethodException {

		Field[] fields = clazz.getDeclaredFields();
		Class<?> fieldClazz = null;

		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				fieldClazz = field.getType();
				break;
			}
		}
		
		if (fieldClazz != null) {
			// boolean: try isXyz
			if (fieldClazz.equals(Boolean.class)
					|| fieldClazz.equals(boolean.class)) {

				String getterName = String.format("is%C%s",
						fieldName.charAt(0), fieldName.substring(1));

				for (Method method : clazz.getMethods()) {

					if (method.getName().equals(getterName)
							&& method.getReturnType().equals(fieldClazz)) {
						return method;
					}
				}
				// no isXyz for bool, try getXyz
			} else {

				String getterName = String.format("get%C%s",
						fieldName.charAt(0), fieldName.substring(1));

				for (Method method : clazz.getMethods()) {

					if (method.getName().equals(getterName)
							&& method.getReturnType().equals(fieldClazz)) {
						return method;
					}
				}
			} // else
		}
		// no such field
		throw new NoSuchMethodException(
				"cannnot find getter for [" + fieldName 
				+ "] in class [" + clazz.getName() + "]");
	}
	
	public static Class<?> getClass(Type type, int i) {
		
        if (type instanceof ParameterizedType) { // 处理泛型类型
        	
            return getGenericClass((ParameterizedType) type, i);
            
        } else if (type instanceof TypeVariable) {
        	
            return (Class<?>) getClass(((TypeVariable<?>) type).getBounds()[0], 0); // 处理泛型擦拭对象<R>
        
        } else {// class本身也是type，强制转型
            return (Class<?>) type;
        }
    }

    public static Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {

        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        
        if (genericClass instanceof ParameterizedType) { // 处理多级泛型
            return (Class<?>) ((ParameterizedType) genericClass).getRawType();
            
        } else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
            
        } else if (genericClass instanceof TypeVariable) { // 处理泛型擦拭对象<R>

            return (Class<?>) getClass(((TypeVariable<?>) genericClass).getBounds()[0], 0);
            
        } else {
            return (Class<?>) genericClass;
        }
    }

    public static void assignField(Object object,
    								String fieldName, 
    								Object var) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
    	
    	assignField(object, fieldName, var, false);
    }
	
    public static void assignField(Object object,
    								String fieldName, 
    								Object var,
    								boolean refPrivate) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
    	
    	Class<?> clazz = object.getClass();
    	Field[] fields = clazz.getDeclaredFields();
    	
    	for (Field field : fields) {
			if (field.getName().equals(fieldName)) {

		    	Class<?> fieldClazz = field.getType();
				Object realVar = null;
				
				if (var instanceof String) {
					realVar = Converter.to((String)var, fieldClazz);
				} else if (var instanceof Number) {
					realVar = Converter.to((Number)var, fieldClazz);
				} else {
					realVar = var;
				}
				if (fieldClazz.isAssignableFrom(realVar.getClass())) {
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
    
	private TypeTraits(){}
}
