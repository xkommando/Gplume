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
package com.caibowen.gplume.core.bean;


import java.io.File;
import java.io.InputStream;
import java.util.Set;

/**
 * Assemble java beans
 * 
 * @author BowenCai
 *
 */
public interface IBeanAssembler {
	
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
	 * @param lifeSpan bean lifes
	 * @return true bean added, false cannot add bean(already exists)
	 */
	public boolean 		addBean(String id, Object bean, int lifeSpan);
	/**
	 * default life Integer.MAX_VALUE
	 * @param id
	 * @param bean
	 * @return
	 */
	public boolean 		addBean(String id, Object bean);
	
	/**
	 * 
	 * @param id
	 * @return bean reference if found, null if not found
	 * @throws Exception 
	 */
	public void 		removeBean(String id);
	
	/**
	 * update singleton
	 * @param id
	 * @param bean
	 * @throws Exception
	 */
	public<T> void 		updateBean(String id, T bean); 
	
	/**
	 * if contains bean of this id
	 * @param id
	 * @return
	 */
	public boolean 		contains(String id);
	
	public boolean 		isSingletion(String id);

	public boolean 		contains(Class<?> clazz);

	/**
	 * 
	 * @param visitor
	 * @throws Exception when creating beans (if bean is not singleton)
	 */
	public void inTake(IAssemlberVisitor visitor);

}
