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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * manage bean life circle
 * 
 * @author BowenCai
 *
 */
public class Pod {
	
	private static final Logger LOG 
		= LoggerFactory.getLogger(Pod.class);
	
	private final int lifeSpan;
	private AtomicInteger age;
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
		this.age = new AtomicInteger(0);

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
	}
	
	void addAge(int i) {
		this.age.addAndGet(i);
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
		if(age.get() < lifeSpan) {
			age.incrementAndGet();
			return instance;
		} else {
			try {
				this.destroy();
			} catch (Exception e) {
				throw new RuntimeException(
						"Error destroying bean pod id[" + beanId +"]", e);
			}
			return null;
		}
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
		return age.get();
	}

	/**
	 * @return the lifeSpan
	 */
	public int getLifeSpan() {
		return lifeSpan;
	}
}
