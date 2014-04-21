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


import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Set;

/**
 * Assemble java beans
 * 
 * @author BowenCai
 *
 */
public interface IBeanAssembler extends Serializable {
	
	public void setClassLoader(ClassLoader loader);
	
	/**
	 * build all beans.
	 * 
	 * Because this function is invoked only at the very beginning of the application
	 *   no exception is handled in the assembling of java beans 
	 * exception is thrown directly to the higher level.
	 * 
	 * @throws Exception 
	 */
	public void 		assemble(final InputStream in) throws Exception;
	public void			assemble(final File file) throws Exception;
	/**
	 * 
	 * @param id
	 * @return
	 * @throws Exception when creating beans (if bean is not singleton)
	 */
	public<T> T 		getBean(String id);
	
	/**
	 * @param clazz
	 * @return beans of this type
	 */
	public Set<Object>	getBeans(Class<?> clazz);
	
	/**
	 * the newly added bean is singleton
	 * @param id
	 * @param bean
	 * @return true bean added, false cannot add bean(already exists)
	 */
	public boolean 		addBean(String id, Object bean);
	
	/**
	 * 
	 * @param id
	 * @return bean reference if found, null if not found
	 */
	public<T> T 		removeBean(String id);
	
	/**
	 * update singleton
	 * @param id
	 * @param bean
	 * @throws Exception
	 */
	public<T> void 		updateBean(String id, T bean) throws Exception; 
	
	/**
	 * if contains bean of this id
	 * @param id
	 * @return
	 */
	public boolean 		contains(String id);
	
	public boolean 		isSingletion(String id);

	public boolean 		contains(Class<?> clazz);
	
	public static interface Visitor extends Serializable {
		public void visit(Object bean);
	}
	
	/**
	 * 
	 * @param visitor
	 * @throws Exception when creating beans (if bean is not singleton)
	 */
	public void inTake(Visitor visitor);

}
