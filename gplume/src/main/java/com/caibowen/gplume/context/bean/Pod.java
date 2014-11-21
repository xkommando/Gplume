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

import com.caibowen.gplume.annotation.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.annotation.Nullable;

/**
 * manage bean life circle
 * 
 * @author BowenCai
 *
 */
@Internal
class Pod {
	
	private static final Logger LOG 
		= LoggerFactory.getLogger(Pod.class);

	public final String beanId;
	
	/**
	 * if singleton, log description, instance == null
	 */
	Element description;
	
	/**
	 * if singleton, description == null
	 */
	Object instance;

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
            ((BeanIDAware) bean).setBeanID(id);
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

        LOG.trace(
                "bean id[{0}] class[" + (instance != null ? instance.getClass().getSimpleName() : "unknown")
                + "] destroyed", beanId);
	}
	
	public boolean isSingleton() {
		return instance != null;
	}

}
