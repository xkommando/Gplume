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


/**
 * 
 * these properties serves as the XLD
 * @author BowenCai
 *
 */
public class XMLTags {

	public static final String ROOT_BEANS = "beans";
	
	public static final String BEAN = "bean";

	public static final String IMPROT = "import";
	
	public static final String BEAN_ID = "id";
	public static final String BEAN_CLASS = "class";
	public static final String PROPERTIES = "properties";
	public static final String BEAN_PROPERTY = "property";

	/**
	 * specify singleton bean, false by default
	 */
	public static final String BEAN_SINGLETON = "singleton";
	/**
	 * 
	 */
	public static final String BEAN_LIFE_SPAN = "lifespan";
	
	public static final String PROPERTY_NAME = "name";

	public static final String PROPERTY_INSTANCE = "instance";
	public static final String PROPERTY_LIST = "list";
	public static final String PROPERTY_MAP  = "props";
	public static final String PROPERTY_MAP_PROP  = "prop";
	public static final String PROPERTY_MAP_KEY  = "key";
	public static final String PROPERTY_VALUE = "value";
	public static final String PROPERTY_REF = "ref";
}
