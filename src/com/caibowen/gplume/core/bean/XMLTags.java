package com.caibowen.gplume.core.bean;


/**
 * 
 * these properties serves as the XLD
 * @author BowenCai
 *
 */
public class XMLTags {

	public static final String ROOT = "beans";
	
	public static final String BEAN = "bean";
	public static final String BEAN_ID = "id";
	public static final String BEAN_CLASS = "class";
	public static final String BEAN_PROPERTY = "property";

	/**
	 * specify singleton bean, false by default
	 */
	public static final String BEAN_SINGLETON = "singleton";
	
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_LIST = "list";
	public static final String PROPERTY_MAP  = "props";
	public static final String PROPERTY_MAP_PROP  = "prop";
	public static final String PROPERTY_MAP_KEY  = "key";
	public static final String PROPERTY_VALUE = "value";
	public static final String PROPERTY_REF = "ref";
}
