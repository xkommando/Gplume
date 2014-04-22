package com.caibowen.gplume.core.bean;

import javax.annotation.Nullable;

import org.w3c.dom.Element;

/**
 * manage bean life circle
 * 
 * @author BowenCai
 *
 */
class Pod {

	private String beanId;
	
	/**
	 * if singleton, log description, instance == null
	 */
	private Element description;
	
	/**
	 * if singleton, description == null
	 */
	private Object instance;

	/**
	 * 
	 * @param id if empty, this bean will not be stored
	 * @param d
	 * @param instance
	 */
	Pod(@Nullable String id, Element d, Object instance) {
		
		if (d != null && instance != null || d == null && instance == null) {
			throw new IllegalStateException("cannot decide whether bean is singleton");
		}
		this.beanId = id;
		this.description = d;
		this.instance = instance;
		process(beanId, instance);
	}
	
	/**
	 * process bean after creation and properties set.
	 * @param id
	 * @param bean
	 */
	static void process(String id, Object bean) {
		if (bean != null && bean instanceof BeanIDAware) {
			((BeanIDAware)bean).setBeanID(id);
		}

		if (bean != null && bean instanceof InitializingBean) {
			try {
				((InitializingBean)bean).afterPropertiesSet();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	void destroy() throws Exception {
//		if (instance != null && instance instanceof DisposableBean) {
//			((DisposableBean)instance).destroy();
//		}
	}
	
	void setInstance(Object instance) {
		this.instance = instance;
	}
	
	boolean isSingleton() {
		return instance != null;
	}

	Object getInstance() {
		return instance;
	}

	Element getDescription() {
		return description;
	}
	
	String getBeanId() {
		return beanId;
	}
}