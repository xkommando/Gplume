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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Usage:
 * 
 * 1. For Object
 * 
 * Setters can only set bean, short/Short, int/Integer ... double/Double, Date
 * List<String>, 
 * List<Object>
 * 
 * For XML files:
 * 
 * 1. bean inside property will not be added to factory, even an id is specified
 * and you cannot get it later
 * 	e.g. even there is an id="c++11" in a subBean, you cannot call getBean("c++11")// returns null;
 * 
 * 3. top level bean without an id will be build, but will not be added to Ioc container.
 * 
 * 4. addBean("id", obj) returns false if id already exists
 * 
 * example
 * 	<bean id="course" class="model.Course">
		<property name="courseName" value="Programming"/>
		<property name="referenceBook" ref="jcip"/>
		<property name="mainBooks">
		  <list>
			<bean  id="c++11" class="model.Book"><!-- will not be registered in factory >
				<property name="name" value="the c++ programming Language"/>
				<property name="author" value="B.S."/>
				<property name="publisher">
					<ref>
						publisher
					</ref>
				</property>
			</bean>
			<ref>someOtherBook</ref>
		</list>
		</property>
	</bean>
*/

/**
 * 
 * @author BowenCai
 * 
 * @version 1.0
 * @since 2013-12-24
 * 
 */
public class XMLBeanAssembler extends XMLBeanAssemblerBase
								implements Serializable {
	
	private static final long serialVersionUID = 1895612360389006713L;
	
//	private static final Logger LOG = Logger.getLogger(XMLBeanFactory.class.getName());

	/**
	 * This is a compile flag.
	 * When this flag is enabled,fields that do not have a correspondent setter 
	 * will be set directly, regardless of its qualifier.
	 * 
	 * However, In some environment, e.g., Google App Engine, 
	 * you cannot reflect on private field on some classes 
	 * due to different security policy.
	 * So it is recommanded that this flag is not open.
	 * 
	 */
	public static final boolean	REFLECT_ON_PRIVATE = false;
	
	private static IBeanAssembler handle = null;
	private XMLBeanAssembler() {}
	
	synchronized public static IBeanAssembler getInstance() {
		if(handle == null) {
			handle = new XMLBeanAssembler();
		}
		return handle;
	}

	
	public void assemble(final InputSource in) throws Exception {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(in);
		doc.getDocumentElement().normalize();
		super.doAssemble(doc);
	}
	
	@Override
	public void assemble(final InputStream in) throws Exception {
		assemble(new InputSource(in));
	}
	
	@Override
	public void	assemble(final File file) throws Exception{
		assemble(new InputSource(file.toURI().toASCIIString()));
	}
	
	/**
	 * @return null if exception is thrown in creating non-singleton bean
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(String id) {

		Pod pod = super.podMap.get(id);
		if (pod == null) {
			return null;
		}
		if (pod.isSingleton()) {
			return (T) pod.getInstance();
		} else {
			try {
				return (T) buildBean(pod.getDescription());
			} catch (Exception e) {
				throw new RuntimeException(
						"faild building non-singleton bean of id[" + id + "]", e);
			}
		}
	}

	@Override
	public Set<Object> getBeans(Class<?> clazz) {
		Set<Object> set = new HashSet<>(16);
		for (Pod pod : super.podMap.values()) {
			Object bean = pod.getInstance();
			if (bean.getClass().equals(clazz)) {
				set.add(bean);
			}
		}
		return set;
	}
	
	@Override
	public boolean contains(Class<?> clazz) {
		Set<Object> beans = getBeans(clazz);
		return beans != null && beans.size() > 0;
	}

	
	@Override
	public void removeBean(String id) {
		Pod pod = super.podMap.remove(id);
		if (pod != null) {
			try {
				pod.destroy();
			} catch (Exception e) {
				throw new RuntimeException(
						"faild destroy bean of id[" + id + "]", e);
			}
		}
	}

	@Override
	public <T> void updateBean(String id, T bean) {

		Pod oldPod = super.podMap.get(id);
		if (oldPod == null || oldPod.getInstance() == null) {
			throw new NullPointerException("cannot find bean[" + id + "]");
		}
		
		oldPod.setInstance(bean);
		super.podMap.put(id, oldPod);

	}

	@Override
	public boolean addBean(String id, Object bean) {
		return addBean(id, bean, Integer.MAX_VALUE);
	}
	
	@Override
	public boolean addBean(String id, Object bean, int lifeSpan) {
		if (contains(id)) {
			return false;
		}
		Pod pod = new Pod(id, null, bean, lifeSpan);
		super.podMap.put(id, pod);
		return true;
	}
	
	@Override
	public boolean contains(String id) {
		return null != super.podMap.get(id);
	}

	@Override
	public boolean isSingletion(String id) {
		Pod pod = super.podMap.get(id);
		if (pod != null) {
			return super.podMap.get(id).isSingleton();
		} else {
			throw new NullPointerException("cannot find bean[" + id + "]");
		}
	}
	
	/**
	 * @param visitor
	 * @throws Exception 
	 */
	@Override
	public void inTake(IAssemlberVisitor visitor) {
		Exception ex = null;
		String id = null;
		for (Map.Entry<String, Pod> entry : super.podMap.entrySet()) {
			Pod pod = entry.getValue();
			/**
			 * create singleton bean
			 */
			try {
				visitor.visit(getBean(pod.getBeanId()));
			} catch (Exception e) {
				/**
				 * continue visiting.
				 * log only the first exception.
				 */
				if (ex != null) {
					ex = e;
					id = pod.getBeanId();
				}
			}
		}
		if (ex != null) {
			throw new RuntimeException(
					"exception when visiting bean of id[" + id + "]", ex);
		}
	}

}
