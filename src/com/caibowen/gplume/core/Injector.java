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
import java.util.Set;

import javax.inject.Inject;

import com.caibowen.gplume.core.bean.IBeanAssembler;
import com.caibowen.gplume.core.bean.IBeanAssemblerAware;
import com.caibowen.gplume.misc.Klass;

/**
 * 
 * Inject properties for objects based on @Inject.
 * 
 * The Injector set properties using public setters only.
 * 
 * The inject look up properties based on the field name,
 * e.g., @Inject prop123, will be injected with beans whose id is "prop123"
 * 
 * 
 * Note that inherited fields that are marked @Inject and 
 * contains a correspondent public setter
 * will be injected as well. 
 * 
 * @author BowenCai
 *
 */
public class Injector implements IBeanAssemblerAware {

	private IBeanAssembler beanFactory;
	@Override
	public void setBeanAssembler(IBeanAssembler beanFactroy) {
		this.beanFactory = beanFactroy;
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
	 * @throws Exception 
	 */
	public void inject(Object object) throws Exception {
		
		Class<?> clazz = object.getClass();
		
		Set<Field> fields = Klass.getEffectiveField(clazz);
		
			
		for (Field field : fields) {
			if (field.isAnnotationPresent(Inject.class)) {

				String fieldName = field.getName();
				Method setter = TypeTraits.findSetter(clazz, fieldName);

				if (setter != null) {
					Object prop = beanFactory.getBean(fieldName);
					if (prop != null) {
						setter.invoke(object, prop);
					} else {
						throw new NullPointerException("no bean with id ["
								+ fieldName + "] found. Class [" + clazz.getName() + "]");
					}

				} else {
					throw new NoSuchMethodException("cannot find setter for ["
							+ fieldName + "] in class [" + clazz.getName() + "]");
				}
			}
			
		} // foreach field
		
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
}





