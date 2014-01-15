package com.caibowen.gplume.core;


import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;

public interface IBeanFactory extends Serializable {

	public static final String ROOT = "beans";
	
	public static final String BEAN = "bean";
	public static final String BEAN_ID = "id";
	public static final String BEAN_CLASS = "class";
	public static final String BEAN_PROPERTY = "property";

	/**
	 * specify singleton in bean, false by default
	 */
	public static final String BEAN_SINGLETON = "singleton";
	
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_LIST = "list";
	public static final String PROPERTY_VALUE = "value";
	public static final String PROPERTY_REF = "ref";
	
	/**
	 * load XML manifest
	 * 
	 */
	public void load(Reader reader);
	public void load(InputStream reader);
	public void load(File reader);
	public void load(URL reader);
	/**
	 * build all beans
	 */
	public void build();

	public<T> T getBean(String id);
	public void addBean(String id, Object bean);
	public<T> T removeBean(String id);
	public<T> void updateBean(String id, T bean); 
	
	public boolean contains(String id);
	public boolean isSingletion(String id);
	
	public static interface Visitor {
		
		public void visit(Object bean);
	}
	
	public void inTake(Visitor visitor);

}
