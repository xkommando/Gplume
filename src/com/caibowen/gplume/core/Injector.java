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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import com.caibowen.gplume.context.bean.IBeanAssembler;
import com.caibowen.gplume.context.bean.IBeanAssemblerAware;
import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.misc.Str;

/**
 * Implementation of JSR 330 @Inject and @Named
 * 
 * Inject properties for objects based on @Inject and @Named
 * 
 * The Injector set properties using public setters only.
 * 
 * The inject look up properties based on the field class, or name,
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
 * 4. @Inject 					-> get by field name
 * 
 * 	still not found -> failed !
 * 
 * @author BowenCai
 *
 */
public class Injector implements IBeanAssemblerAware {

	private static final Logger LOG = Logger.getLogger(Inject.class.getName());
	
	private IBeanAssembler beanAssembler;
	@Override
	public void setBeanAssembler(IBeanAssembler beanFactroy) {
		this.beanAssembler = beanFactroy;
	}

	/**
	 * 
	 * for non-public filed without public setter throws IllegalAccessException
	 * else try 
	 * 1. @Named("id") 					-> get by ${id} 
	 * 2. @Named() no value set 		-> get by field name 
	 * 3. @Inject 						-> get by field class 
	 * 4. @Inject 						-> get by field name
	 * 
	 * still not found -> failed !
	 * 
	 * @param object
	 * @throws Exception
	 */
	@Named
	public void inject(Object object) throws Exception {
		
		Class<?> clazz = object.getClass();
		
		Set<Field> fields = Klass.getEffectiveField(clazz);
			
		for (Field field : fields) {
			// first try Named, if successed, we continue to next field
			if (field.isAnnotationPresent(Named.class)) {
				Named anno = field.getAnnotation(Named.class);
				String id = anno.value();
				if (!Str.Utils.notBlank(id)) {
					id = field.getName();
				}
				Object var = beanAssembler.getBean(id);
				if (var == null) {
					throw new NullPointerException("cannot find bean with id [" 
							+ id + "] for property[" + field.getName() 
							+ "] in class [" + object.getClass().getName() + "]");
				}
				BeanEditor.setBeanProperty(object, field.getName(), var);
				continue;
			}
			
			// if Named failed, try inject, last attempt
			if (field.isAnnotationPresent(Inject.class)) {
				Set<Object> vars = beanAssembler.getBeans(field.getType());
				if (vars.size() != 1) {
					BeanEditor.setBeanProperty(object, field.getName(), 
												vars.iterator().next());
					
				} else if (null != beanAssembler.getBean(field.getName())) {
					Object var = beanAssembler.getBean(field.getName());
					if (field.getType().isAssignableFrom(var.getClass())){
						BeanEditor.setBeanProperty(object, field.getName(), var);
						LOG.warning("cannot find bean for field [" 
							+ field.getName() + "] in class [" 
							+ object.getClass().getName() + "]"
							+ "\r\n But find bean in beanAssemble with the same ID as the field name[" + field.getName()
							+"]\r\n Setting field with this bean[" + var.getClass().getName() + "]");
						continue;
					}
					
				} else {
					throw new NoSuchElementException("faild to set field[" 
							+ field.getName() + "] in class[" + object.getClass().getName() +"]"
							+ "\r\n cannot find bean with the assignale type or specified name for this field");
				}	
			}
			
			// pass not annotated fields
			
		} // foreach field
		
	}

}

//
//
//	static class B {
//		private int bb = 5;
//		public void setBb(int bb) {
//			this.bb = bb;
//		}
//		public void name() {
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
//		public void name() {
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
//		public void name() {
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





