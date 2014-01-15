package com.caibowen.gplume.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import com.caibowen.gplume.except.NoSuchBeanException;

public class Injector implements IBeanFactoryAware {

	private IBeanFactory beanFactroy;
	
	@Override
	public void setBeanFactroy(IBeanFactory factory) {
		beanFactroy = factory;
	}
	

	/**
	 * 
	 * NoSuchFieldException is impossible
	 * 
	 * for non-public setter 
	 * throws IllegalAccessException 
	 * 
	 * 
	 * @param object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws NoSuchBeanException 
	 */
	public void inject(Object object) throws IllegalAccessException, 
										IllegalArgumentException,
										InvocationTargetException, 
										NoSuchMethodException, 
										NoSuchBeanException {
		
		// get all fields including private ones, 
		// properties are set with the corresponding public setters.
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			if (field.isAnnotationPresent(Inject.class)) {

				String fieldName = field.getName();
				Method setter = TypeTraits.findSetter(clazz, fieldName);

				if (setter != null) {
					Object prop = beanFactroy.getBean(fieldName);
					if (prop != null) {
						setter.invoke(object, prop);
					} else {
						throw new NoSuchBeanException("no bean with id ["
								+ fieldName + "] found");
					}

				} else {
					throw new NoSuchMethodException("cannot find setter for "
							+ fieldName);
				}
			}
			
		} // foreach field
		
	}
	
	
}
