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
package com.caibowen.gplume.context.bean;

import com.caibowen.gplume.core.BeanEditor;
import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implementation of JSR 330 @Inject and @Named
 * 
 * Inject properties for objects based on @Inject and @Named
 * 
 * The Injector set properties using public setters only.
 * 
 * The inject look up properties based on the field class, or id,
 * 
 * Note that inherited fields that are marked @Inject and 
 * contains a correspondent public setter
 * will be injected as well. 
 * 
 * for non-public filed without public setter 
 * throws IllegalAccessException 
 * else try
 * 1. @Named("id") 				-> get by ${id}
 * 2. @Named() no value set  	-> get by field name
 * 3. @Inject 					-> get by field class
 * 4. @Inject 					-> get by field id
 * 
 * 	still not found -> failed !
 * 
 * @author BowenCai
 *
 */
class XMLInjector implements Injector, Serializable {

    private static final long serialVersionUID = -5041870626361257009L;
	private static final Logger LOG = LoggerFactory.getLogger(XMLInjector.class);

	final XMLBeanAssembler assembler;

	public XMLInjector(XMLBeanAssembler assembler) {
		this.assembler = assembler;
	}

	/**
	 * 
	 * for non-public filed without public setter throws IllegalAccessException
	 * else try 
	 * 1. @Named("id") 					-> get by ${id} 
	 * 2. @Named() no value set 		-> get by field id 
	 * 3. @Inject 						-> get by field class 
	 * 4. @Inject 						-> get by field id
	 * 
	 * still not found -> failed !
	 * 
	 * @param object
	 * @throws Exception
	 */
	@Override
	public void inject(@Nonnull Object object) {
		doInject(object, false);
	}

	/**
	 *
	 * inject, if value currently not available(circular dependency), create proxy.
	 *
	 * @param object
	 */
	@Override
	public void injectMediate(@Nonnull Object object) {
		doInject(object, true);
	}

	protected void doInject(Object object, boolean withinProcess) {
		Set<Field> fields = Klass.getEffectiveField(object.getClass());

		for (Field field : fields) {
			// first try Named, if successed, we continue to next field
			if (field.isAnnotationPresent(Named.class))
				withNamed(object, field, withinProcess);

				// if Named failed, try inject, last attempt
			else if (field.isAnnotationPresent(Inject.class))
				withInject(object, field, withinProcess);

			// pass off not annotated fields
		} // foreach field
	}

	public void withInject(Object object, Field field, boolean withinProcess) {

		Set<Object> vars = assembler.getBeans(field.getType());
		Object var = null;
		if (vars.size() == 1)
			var = vars.iterator().next();
		else if (null == var && withinProcess
				&& null == (var = assembler.getForBuild(field.getName(), true, field.getDeclaringClass())))
		throw new NoSuchElementException("Could not set field["
				+ field.getName() + "] in class[" + object.getClass().getName() +"]"
				+ "\r\n cannot find bean with the assignable type or specified id for this field");

		try {
			BeanEditor.setProperty(object, field.getName(), var);
		} catch (Exception e) {
			throw new RuntimeException(
					MessageFormat.format("Could not inject field {} of type {}  in class {}"
							, field.getName(), field.getType()
							, field.getDeclaringClass()));
		}
	}

	public void withNamed(@Nonnull Object object, @Nonnull Field field, boolean withinProcess) {
		Named anno = field.getAnnotation(Named.class);
		String curID = anno.value();
		if (Str.Utils.isBlank(curID)) {
			curID = field.getName();
		}
		Object var;
		if (null == (var = assembler.getBean(curID))
				&& withinProcess
					&& null == (var = assembler.getForBuild(curID, true, field.getDeclaringClass())))
			throw new NullPointerException("cannot find bean with id ["
					+ curID + "] for property[" + field.getName() 
					+ "] in class [" + object.getClass().getName() + "]");
		
		try {
			BeanEditor.setProperty(object, field.getName(), var);
		} catch (Exception e) {
			throw new RuntimeException(
					MessageFormat.format("error inject field {} of type {} named {} in class {}"
							, field.getName(), field.getType(), curID
							, field.getDeclaringClass()));
		}
	}
	
}




