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

import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import org.w3c.dom.Element;

import javax.annotation.Nullable;

/**
 * manage bean life circle
 * 
 * @author BowenCai
 *
 */
public class Pod {
	
	private static final Logger LOG 
		= LoggerFactory.getLogger(Pod.class);

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
     *
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
				LOG.info(
						"bean [" + bean.getClass().getSimpleName()
						+ "] initialized");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * destroy this bean
	 * @throws Exception
	 */
	void destroy() throws Exception {
		if (instance != null && instance instanceof DisposableBean) {
			((DisposableBean)instance).destroy();
		}
		instance = null;
		description = null;
		this.beanId = null;

        LOG.info(
                "bean id[{0}] class[" + (instance != null ? instance.getClass().getSimpleName() : "unknown")
                + "] destroyed", beanId);
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
	Object getInternal() {
		return instance;
	}
//---------------------------------------------------------
	
	public Object getInstance() {
		return instance;
	}

	public String getBeanId() {
		return beanId;
	}
	
	public boolean isSingleton() {
		return instance != null;
	}

}
