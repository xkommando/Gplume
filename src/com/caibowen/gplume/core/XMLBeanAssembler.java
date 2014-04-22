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
package com.caibowen.gplume.core;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.core.IBeanAssembler;
import com.caibowen.gplume.core.IBeanAssemblerAware;
import com.caibowen.gplume.core.TypeTraits;
import com.caibowen.gplume.misc.Str;

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
public class XMLBeanAssembler implements IBeanAssembler {

	/**
	 * these properties serves as the XLD
	 */
	public static final class XLD {

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
		public static final String PROPERTY_VALUE = "value";
		public static final String PROPERTY_REF = "ref";
	}
	
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
	
	
	protected Map<String, Pod>	podMap = new ConcurrentHashMap<>(64);
	protected ClassLoader classLoader;
	
	private static XMLBeanAssembler handle = null;
	private XMLBeanAssembler() {}
	
	synchronized public static XMLBeanAssembler getInstance() {
		if(handle == null) {
			handle = new XMLBeanAssembler();
		}
		return handle;
	}


	@Override
	public void setClassLoader(ClassLoader loader) {
		this.classLoader = loader;
	}
	
	public void assemble(final InputSource in) throws Exception {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(in);
		doc.getDocumentElement().normalize();
		doAssemble(doc);
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
	 * xml bean factory being singleton implies that this function is not
	 * reenterable, thus it is thread safe
	 * 
	 * @param beanList
	 * @throws Exception
	 */
	protected void doAssemble(Document doc) throws Exception {

		NodeList nodeList = doc.getChildNodes();
		Node beanNode = null;
		
		// escape comments
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node tn = nodeList.item(i);
			if (tn != null && tn.getNodeType() == Node.ELEMENT_NODE) {
				beanNode = tn.getFirstChild();
				break;
			}
		}
		if (beanNode == null) {
			throw new IllegalArgumentException("no bean definition found");
		}
		
		while (beanNode.getNextSibling() != null) {

			beanNode = beanNode.getNextSibling();
			Element beanElem;
			if (beanNode.getNodeType() == Node.ELEMENT_NODE) {
				beanElem = (Element) beanNode;
			} else {
				continue;
			}

			String bnId = beanElem.getAttribute(XLD.BEAN_ID);
			String bnScope = beanElem.getAttribute(XLD.BEAN_SINGLETON);
			// System.out.println("id[" + bnId + "]");

			Pod pod = null;

			boolean isSingleton = true;
			if (Str.Utils.notBlank(bnScope)) {
				if ("false".equalsIgnoreCase(bnScope.trim())) {
					isSingleton = false;
				}
			}

			if (isSingleton) {
				Object bean = buildBean(beanElem);
				pod = new Pod(bnId, null, bean);

			} else {
				pod = new Pod(bnId, beanElem, null);
			}

			if (Str.Utils.notBlank(bnId)) {
				podMap.put(bnId, pod);
			}
		}
	}
	
	protected Object buildBean(Element beanElem) throws Exception {
		/**
		 * For each property, there are 3 notation:
		 * 
		 * 1. <property name="Xyz"><value="Xyz"/></porperty>
		 * 
		 * 2. <property name="Xyz" value="Xyz"/>
		 * 
		 * 3. no property is needed.
		 */

		Class<?> bnClass = getClass(beanElem);
		int modi = bnClass.getModifiers();

		if (Modifier.isAbstract(modi)) {
			throw new IllegalStateException("class[" + bnClass.getName()
					+ "] is abstract and cannot be instantiated");
		} else if (Modifier.isInterface(modi)) {
			throw new IllegalStateException("class[" + bnClass.getName()
					+ "] is interface and cannot be instantiated");
		}

		Object beanObj = bnClass.newInstance();

		/**
		 * All hooks here
		 */
		if (beanObj instanceof IBeanAssemblerAware) {
			((IBeanAssemblerAware) beanObj).setBeanAssembler(this);
		}

		NodeList propLs = beanElem.getElementsByTagName(XLD.BEAN_PROPERTY);
		if (propLs == null || propLs.getLength() == 0) {
			// no property
			return beanObj;
		}
		
		// get top level properties only, skip properties in sub beans
		Node next = beanElem.getFirstChild();
		while (next.getNextSibling() != null) {
			Element prop;
			next = next.getNextSibling();

			if (next.getNodeType() == Node.ELEMENT_NODE) {
				prop = (Element) next;
			} else {
				continue;
			}

			String propName = prop.getAttribute(XLD.PROPERTY_NAME);
			if (!Str.Utils.notBlank(propName)) {
				throw new NullPointerException(
						"Property name is empty.Element: ["
								+ prop.getNodeName() + "]");
			}
			propName = propName.trim();
			
//System.out.println("class [" + bnClass.getSimpleName() + "] prop[" + propName);

			NodeList varList = prop.getChildNodes();
			if (varList == null || varList.getLength() == 0) {
				// property inside, one string value or one ref
				
				String varStr = prop.getAttribute(XLD.PROPERTY_VALUE);
				String varRef = prop.getAttribute(XLD.PROPERTY_REF);
				
				if (Str.Utils.notBlank(varStr)) {
					setStrProperty(bnClass, beanObj, propName, varStr.trim());
					continue;
				} else if (Str.Utils.notBlank(varRef)) {
					Object ref = getBean(varRef.trim());
					setBeanProperty(bnClass, beanObj, propName, ref);
					continue;
				} else {
					throw new IllegalArgumentException("No value for property["
							+ propName + "] in class [" + bnClass.getName()
							+ "]");
				}

			} else {// properties outside
				
				// check if is list
				boolean isList = false;
				/**
				 * 
				 * prop
				 * |
				 * first child -> next
				 * 					|
				 * 				    <list> or <value>
				 */
				Node iter = prop.getFirstChild().getNextSibling();

				if (XLD.PROPERTY_LIST.equals(iter.getNodeName())) {
					isList = true;
					iter = iter.getFirstChild().getNextSibling();
				}
				
//System.out.println(propName + "  " + isList);
				
				ArrayList<Object> beanList = new ArrayList<Object>(8);

				while (iter != null && iter.getNodeType() == Node.ELEMENT_NODE) {
					
					Element elemBn = (Element) iter;
					
					if (XLD.BEAN.equals(elemBn.getNodeName())) {
						beanList.add(buildBean(elemBn));

					} else if (XLD.PROPERTY_REF.equals(elemBn.getNodeName())) {
						beanList.add(getBean(elemBn.getTextContent().trim()));

					} else if (XLD.PROPERTY_VALUE.equals(elemBn.getNodeName())) {
						beanList.add(elemBn.getTextContent().trim());
					} else {
						throw new IllegalArgumentException("Unknown property["
								+ iter.getNodeName() + "]");
					}
					// skip node of #text
					iter = iter.getNextSibling().getNextSibling();
				}// while
				
				if (isList) {
					setListProperty(bnClass, beanObj, propName, beanList);
					
				} else {
					if (beanList.size() == 1) {
						setBeanProperty(bnClass, beanObj, propName, beanList.get(0));
						
					} else {
						throw new IllegalArgumentException(
								"Bean number miss match for property[" + propName
										+ "] in class ["
										+ bnClass.getName() + "]\n"
										+ "needs 1 actual " + beanList.size()
										+ "beans : " + beanList.toString());
					}
				}
				
				
			} // props

		} // for properties

		return beanObj;
	}
	
	protected static void setBeanProperty(Class<?> bnClass,
									Object bean,
									String propName,
									Object var) throws Exception {
		
		Method method = TypeTraits.findSetter(bnClass, propName);
		if (method == null) {
			if (REFLECT_ON_PRIVATE) {
				TypeTraits.assignField(bean, propName, var, true);
				return;
			} else {
				throw new IllegalStateException(
						"in class [" + bnClass.getName()
						+ "]  cannot find setter for [" + propName + "]");
			}
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length == 1) {

			method.invoke(bean, var);

		} else {
			throw new IllegalStateException(
					"in class [" + bnClass.getName()
					 +"]   setter [" + method.getName()
						+ "] has more than one parameters");
		}
	}
	
	protected static void setStrProperty(Class<?> bnClass,
									Object bean,
									String propName,
									String varStr) throws Exception {

		Method method = TypeTraits.findSetter(bnClass, propName);
		
		if (method == null) {
			if (REFLECT_ON_PRIVATE) {
				TypeTraits.assignField(bean, propName, varStr, true);
				return;
			} else {
				throw new IllegalStateException(
						"in class [" + bnClass.getName()
						+ "]  cannot find setter for [" + propName + "]");
			}
		}
		
		Class<?>[] paramTypes = method.getParameterTypes();
		
		if (paramTypes.length == 1) {
//System.out.println(bnClass.getSimpleName() + "   " + varStr + "    " + method.getName() + "   " + paramTypes[0].getName());
			Object var = Converter.castStr(varStr, paramTypes[0]);
			method.invoke(bean, var);

		} else {
			throw new IllegalStateException(
					"in class [" + bnClass.getName()
					 +"]  setter [" + method.getName()
						+ "]  has more than one parameters");
		}
	}

	protected static void setListProperty(Class<?> bnClass, Object bean,
			String propName, List<?> varArray) throws Exception {
		
//		NodeList vars = elemList.getElementsByTagName(XLD.PROPERTY_VALUE);
//		List<String> varArray = new ArrayList<>(vars.getLength());
//		for (int i = 0; i < vars.getLength(); i++) {
//			varArray.add(vars.item(i).getTextContent());
//		}

		Method method = TypeTraits.findSetter(bnClass, propName);

		if (method == null) {
			if (REFLECT_ON_PRIVATE) {
				TypeTraits.assignField(bean, propName, varArray, REFLECT_ON_PRIVATE);
				return;
			} else {
				throw new IllegalStateException(
						"in class [" + bnClass.getName()
						+ "]  cannot find setter for [" + propName + "]");
			}
		}

		Class<?>[] paramTypes = method.getParameterTypes();

		if (paramTypes != null && paramTypes.length == 1) {
			if (paramTypes[0].isAssignableFrom(varArray.getClass())) {
				// System.out.println("invoking " + method.getName());
				method.invoke(bean, varArray);

			} else {

				if (REFLECT_ON_PRIVATE) {
					TypeTraits.assignField(bean, propName, varArray, true);
					return;
				} else {
					throw new IllegalStateException(
							"in class "
							+ bnClass.getName()
							+ " cannot assign ArrayList to parameter ["
							+ propName + "]");
				}
			}

		} else {
			throw new IllegalStateException(
					"in class [" + bnClass.getName()
					 +"], setter[" + method.getName()
						+ "] has more than one parameters");
		}

	}
	
	protected  Class<?> getClass(Element element) throws ClassNotFoundException {
		String clazzName = element.getAttribute(XLD.BEAN_CLASS).trim();
		return this.classLoader.loadClass(clazzName);
	}

	/**
	 * @return null if exception is thrown in creating non-singleton bean
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(String id) {

		Pod pod = podMap.get(id);
		if (pod == null) {
			return null;
		}
		if (pod.isSingleton()) {

			return (T) pod.getInstance();

		} else {
			
			try {
				return (T) buildBean(pod.getDescription());
			} catch (Exception e) {
			}
			return null;
		}
	}

	@Override
	public Set<Object> getBeans(Class<?> clazz) {
		Set<Object> set = new HashSet<>(16);
		for (Pod pod : podMap.values()) {
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

	
	@SuppressWarnings("unchecked")
	@Override
	public<T> T removeBean(String id) {
		Pod pod = podMap.remove(id);
		if (pod != null) {
			return (T) pod.getInstance();
		} else {
			return null;
		}
	}

	@Override
	public <T> void updateBean(String id, T bean) {

		Pod oldPod = podMap.get(id);
		if (oldPod == null || oldPod.getInstance() == null) {
			throw new NullPointerException("cannot find bean[" + id + "]");
		}
		
		oldPod.setInstance(bean);
		podMap.put(id, oldPod);

	}

	@Override
	public boolean addBean(String id, Object bean) {
		if (contains(id)) {
			return false;
		}
		Pod pod = new Pod(id, null, bean);
		podMap.put(id, pod);
		return true;
	}
	
	@Override
	public boolean contains(String id) {
		return null != podMap.get(id);
	}

	@Override
	public boolean isSingletion(String id) {
		Pod pod = podMap.get(id);
		if (pod != null) {
			return podMap.get(id).isSingleton();
		} else {
			throw new NullPointerException("cannot find bean[" + id + "]");
		}
	}
	
	/**
	 * @param visitor
	 * @throws Exception 
	 */
	@Override
	public void inTake(Visitor visitor) {
		
		for (Map.Entry<String, Pod> entry : podMap.entrySet()) {
			Pod pod = entry.getValue();
			/**
			 * create singleton bean
			 */
			visitor.visit(getBean(pod.getBeanId()));
		}
	}
	
	protected static class Pod {

		private String beanId;
		
		/**
		 * if singleton, log description, instance == null
		 */
		private Element description;
		
		/**
		 * if singleton, description == null
		 */
		private Object instance;

		Pod(String id, Element d, Object instance) {
			if (d != null && instance != null || d == null && instance == null) {
				throw new IllegalStateException("cannot decide whether bean is singleton");
			}
			this.beanId = id;
			this.description = d;
			this.instance = instance;
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

}
