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
package com.caibowen.gplume.context;


/**
 *
 * @author BowenCai
 *
 */
public final class XMLTags {
	private XMLTags(){}


	public static final String ROOT_BEANS = "beans";
	public static final String NAMESPACE = "namespace";
	public static final String USING_NS = "using";
	public static final String NS_OPEN = "open";
	public static final String NS_DELI = "::";

	public static final String BEAN = "bean";

	public static final String REQUIRED = "required";

    public static final String TYPE = "type";

    public static final String CONFIG = "config";
    public static final String SCOPE = "scope";
	public static final String IMPORT = "import";
	
	public static final String BEAN_ID = "id";
	public static final String BEAN_CLASS = "class";
	public static final String BEAN_PROXY = "proxy";
    public static final String BEAN_CONSTRUCT = "construct";
    public static final String BEAN_AFTER_CALL = "aftercall";


	public static final String DEFINE = "define";
	public static final String BEAN_PROP = "prop";

	/**
	 * specify singleton bean, false by default
	 */
	public static final String BEAN_SINGLETON = "singleton";
	
	public static final String PROP_NAME = "name";

	public static final String PROP_INSTANCE = "instance";
	public static final String PROP_LIST = "list";
	public static final String PROP_MAP = "map";
	public static final String PROP_MAP_KEY = "key";
	public static final String PROP_SET = "set";
	public static final String PROP_VALUE = "val";
	public static final String PROP_REF = "ref";
}
