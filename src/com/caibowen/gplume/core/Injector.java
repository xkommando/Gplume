/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

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
		
		Set<Field> fields = getEffectiveField(clazz);
		
			
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
	
	/**
	 * compare java.lang.reflect.field by its name, if two field, 
	 * although at different levels of inheritance, have the same name and type,
	 * are considered the same.
	 * 
	 * And during comparison, parent field will generally be covered (shadowed) by 
	 * child field.
	 */
	private static final Comparator<Field> FIELD_COMP = new Comparator<Field>() {
		@Override
		public int compare(Field o1, Field o2) {
			int cmp = o1.getName().compareTo(o2.getName());
			return cmp != 0 ? cmp 
					:  o1.getType().equals(o2.getType()) ? 0 : 1;
		}
	};
	
	/**
	 * get all filed (public private) in the class inheritance tree
	 * parent field with the same name and type will be covered (shadowed) by 
	 * child field, only one copy is returned.
	 * 
	 * @param clazz
	 * @return
	 */
	public static final Set<Field> getEffectiveField(Class<?> clazz) {
		
		Class<?> clazRef = clazz;
		TreeSet<Field> fieldSet = new TreeSet<Field>(FIELD_COMP);
		while (!clazRef.equals(Object.class)) {
			for (Field field : clazRef.getDeclaredFields()) {
				fieldSet.add(field);
			}
			clazRef = clazRef.getSuperclass();
		}
		return fieldSet;
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





