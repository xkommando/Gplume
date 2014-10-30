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

import com.caibowen.gplume.context.AppContext;
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
 * 2. @Named() no value set  	-> get by field id
 * 3. @Inject 					-> get by field class
 * 4. @Inject 					-> get by field id
 * 
 * 	still not found -> failed !
 * 
 * @author BowenCai
 *
 */
public class Injector implements Serializable {

    private static final long serialVersionUID = -5041870626361257009L;
	private static final Logger LOG = LoggerFactory.getLogger(Injector.class);

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
	public void inject(@Nonnull Object object) {
		
		Set<Field> fields = Klass.getEffectiveField(object.getClass());
		
		for (Field field : fields) {
			// first try Named, if successed, we continue to next field
			if (field.isAnnotationPresent(Named.class)) {
				withNamed(object, field);
			}
			
			// if Named failed, try inject, last attempt
			else if (field.isAnnotationPresent(Inject.class)) {
				withInject(object, field);
			}
			
			// pass off not annotated fields
		} // foreach field
		
	}

	void withInject(Object object, Field field) {
		
		Object var = null;
		Set<Object> vars = AppContext.beanAssembler.getBeans(field.getType());
		if (vars.size() != 1) {
			try {
				BeanEditor.setProperty(object, field.getName(), 
											vars.iterator().next());
			} catch (Exception e) {
				throw new RuntimeException(
						MessageFormat.format("error inject field {0} of type {1}  in class {2}"
								, field.getName(), field.getType()
								, field.getDeclaringClass()));
			}
			
		} else if (null != (var = AppContext.beanAssembler.getBean(field.getName()))) {
			if (field.getType().isAssignableFrom(var.getClass())){
				LOG.warn("cannot find bean for field [{0}] in class [{1}]"
						+ "\r\n But find bean in beanAssemble with the same ID as the field id[{2}]"
						+ "\r\n Setting field with this bean[{4}]"
						, field.getName()
						, object.getClass().getName()
						, field.getName()
						, var.getClass().getName());
				
				try {
					BeanEditor.setProperty(object, field.getName(), var);
				} catch (Exception e) {
					throw new RuntimeException(
							MessageFormat.format("error inject field {0} of type {1}  in class {2}"
									, field.getName(), field.getType()
									, field.getDeclaringClass()));
				}
			}
		} else {
			throw new NoSuchElementException("faild to set field[" 
					+ field.getName() + "] in class[" + object.getClass().getName() +"]"
					+ "\r\n cannot find bean with the assignale type or specified id for this field");
		}	
	}
	
	void withNamed(@Nonnull Object object, @Nonnull Field field) {
		Named anno = field.getAnnotation(Named.class);
		String curID = anno.value();
		if (!Str.Utils.notBlank(curID)) {
			curID = field.getName();
		}					
		Object var = AppContext.beanAssembler.getBean(curID);
		if (var == null) {
			throw new NullPointerException("cannot find bean with id [" 
					+ curID + "] for property[" + field.getName() 
					+ "] in class [" + object.getClass().getName() + "]");
		}
		
		try {
			BeanEditor.setProperty(object, field.getName(), var);
		} catch (Exception e) {
			throw new RuntimeException(
					MessageFormat.format("error inject field {0} of type {1} named {2} in class {3}"
							, field.getName(), field.getType(), curID
							, field.getDeclaringClass()));
		}
	}
	
}

//
//
//	static class B {
//		private int bb = 5;
//		public void setBb(int bb) {
//			this.bb = bb;
//		}
//		public void id() {
//			System.out.println(bb);
//		}
//	}
//	static class C extends B {
//		private int bb = 6;
//		private int cc = 7;
//		public void setCc(int cc) {
//			this.cc = cc;
//		}
//		@Override
//		public void id() {
//			System.out.println( bb + "  " + cc);
//		}
//	}
//	
//	static class D extends C {
//		private short bb = 8;
//		private int cc = 9;
//		private int dd = 10;
//		public void setDd(int dd) {
//			this.dd = dd;
//		}		
//		public void setBb(Short bb) {
//			this.bb = bb;
//		}
//		@Override
//		public void id() {
//			System.out.println( bb + "  " + cc + "   " + dd);
//		}
//	}
//	
//	public static void main(String...a) throws IllegalArgumentException, IllegalAccessException {
//		B b = new B(); b.setBb(45);
//		C c = new C(); c.setCc(46);
//		D d = new D(); d.setDd(47); d.setBb(48);
//		for (Field field : Injector.getEffectiveField(D.class)) {
//			System.out.print(field.getDeclaringClass().getSimpleName() + "  " + field.getName() + "   ");
//			field.setAccessible(true);
//			System.out.println(field.getInt(d));
//		}
//	}





