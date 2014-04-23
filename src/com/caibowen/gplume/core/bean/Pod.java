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

import javax.annotation.Nullable;

import org.w3c.dom.Element;

/**
 * manage bean life circle
 * 
 * @author BowenCai
 *
 */
public class Pod {

	private int lifeSpan;
	private int age;
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
	Pod(@Nullable String id, Element d, Object instance, int lifeSp) {
		
		if (d != null && instance != null || d == null && instance == null) {
			throw new IllegalStateException("cannot decide whether bean is singleton");
		}
		this.beanId = id;
		this.description = d;
		this.instance = instance;
		this.lifeSpan = lifeSp;
		this.age = 0;
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
		if (instance != null && instance instanceof DisposableBean) {
			((DisposableBean)instance).destroy();
		}
	}
	
	void setInstance(Object instance) {
		this.instance = instance;
	}
	
	Element getDescription() {
		return description;
	}
	
	/**
	 * get bean instance internal, with out add age
	 * @return
	 */
	Object getInstanceINternal() {
		return instance;
	}
//---------------------------------------------------------
	synchronized public Object getInstance() {
		if(instance != null) {
			age++;
		}
		return instance;
	}

	public String getBeanId() {
		return beanId;
	}
	
	public boolean isSingleton() {
		return instance != null;
	}
	
	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @return the lifeSpan
	 */
	public int getLifeSpan() {
		return lifeSpan;
	}
}
