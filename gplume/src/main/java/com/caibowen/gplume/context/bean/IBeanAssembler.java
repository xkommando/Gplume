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


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	
	void setClassLoader(@Nonnull ClassLoader loader);
	@Nonnull
	ClassLoader getClassLoader();

    void setConfigCenter(ConfigCenter configCenter);
    ConfigCenter getConfigCenter();
	
	/**
	 * build all beans.
	 * 
	 * Because this function is invoked only at the very beginning of the application
	 *   no exception is handled in the assembling of java beans 
	 * exception is thrown directly to the higher level.
	 * 
	 * @throws Exception 
	 */
	void 			assemble(@Nonnull final InputStream in) throws Exception;
	void			assemble(@Nonnull final File file) throws Exception;
	
	/**
	 * this path will be passed to InputStreamProvider to get input stream
	 * 
	 * @param path
	 * @throws Exception
	 */
	void 			assemble(@Nonnull final String path) throws Exception;
	
	/**
	 * 
	 * @param id
	 * @return null if not found or exception is thrown in creating non-singleton bean
	 * @throws Exception when creating beans (if bean is not singleton)
	 */
	@Nullable
	public<T> T 		getBean(@Nonnull String id);
	

	/**
     * will not increase the bean age
     *
	 * @param clazz
	 * @return a set of beans of that is instance of this class (including derived)
	 */
	@Nullable
	Set<Object>	getBeans(@Nonnull Class<?> clazz);
	
	/**
	 * 
	 * @param key
	 * @return key value configuration from xml or porperties file
	 */
	@Nonnull
    ConfigCenter configCenter();

	
	/**
	 * default life Integer.MAX_VALUE
	 * @param id
	 * @param bean
	 * @return
	 */
	boolean 		addBean(@Nonnull String id, @Nonnull Object bean);
	
	/**
	 * @param id
	 * @return bean reference if found, null if not found
	 * @throws Exception 
	 */
	void 		    removeBean(@Nonnull String id);
	
	/**
	 * update singleton
	 * @param id
	 * @param bean
	 * @throws Exception
	 */
	public<T> void 		updateBean(@Nonnull String id, @Nonnull T bean); 
	
	/**
	 * if contains bean of this id
	 * @param id
	 * @return
	 */
	boolean 		contains(@Nonnull String id);
	
	boolean 		isSingletion(@Nonnull String id);

	boolean 		contains(@Nonnull Class<?> clazz);

	/**
	 * 
	 * @param visitor
	 * @throws Exception when creating beans (if bean is not singleton)
	 */
	void inTake(@Nonnull IAssemlberVisitor visitor);

}
