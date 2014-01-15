package com.caibowen.gplume.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


/**
 * For Object:
 * 
 * Setters can only set byte/Byte, short/Short, int/Integer ... double/Double
 * cannot set byte[] char[] or any other array type
 * 
 * For XML files:
 * 
 * 1. List property can be String only
 * 2. bean inside property will not be added to factory, 
 * thus you cannot get it later
 * 
 * e.g. even there is an id="c++11", you cannot getBean("c++11");
 * 
 * 	<bean id="course" class="model.Course">
		<property name="name" value="Programming"/>
		<property name="referenceBook" ref="jcip"/>
		
		<property name="mainBook">
			<bean  id="c++11" class="model.Book"><!-- can be built, but you cannot getBean("c++11")-- >
				<property name="name" value="the c++ programming Language"/>
				<property name="author" value="B.S."/>
				<property name="publisher">
					<ref>
						publisher
					</ref>
				</property>
			</bean>
		</property>
	</bean>
*/

/**
 * 
 * @author BowenCai
 * @version 1.0
 * @since 2013-12-24
 * 
 */
public class XMLBeanFactory implements IBeanFactory {

	private static final long serialVersionUID = 1895612360389006713L;
	
	private static final Logger LOG = Logger.getLogger(XMLBeanFactory.class.getName());
	

	/**
	 * This is a compile flag, When this flag is enabled, 
	 * fields that do not have a correspondent setter 
	 * will be set directly, regardless of its qualifier.
	 * 
	 * 
	 * However, In some environment, e.g., Google App Engine, 
	 * you cannot reflect on private field due to different security policy.
	 * 
	 */
	public static final boolean	REFLECT_ON_PRIVATE = false;
	
	protected Map<String, Pod>	podMap = new ConcurrentHashMap<>(32);
	
	protected List<Element>		beanList;
	
	
	private static XMLBeanFactory handle = null;
	private XMLBeanFactory() {}
	
	synchronized public static XMLBeanFactory getInstance() {
		
		if(handle == null) {
			handle = new XMLBeanFactory();
		}
		return handle;
	}
	
	@Override
	public void load(File config) {

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(config);
		} catch (JDOMException | IOException e) {
			LOG.log(Level.SEVERE, "Error Reading XML file:" + e.getClass().getName(), e);
			return;
		}
		beanList = doc.getRootElement().getChildren(BEAN);
	}
	
	@Override
	public void load(InputStream config) {

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(config);
		} catch (JDOMException | IOException e) {

			LOG.log(Level.SEVERE, "Error Reading XML file:" + e.getClass().getName(), e);
			return;
		}
		beanList = doc.getRootElement().getChildren(BEAN);
	}
	@Override
	public void load(URL config) {

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(config);
		} catch (JDOMException | IOException e) {
			LOG.log(Level.SEVERE, "Error Reading XML file:" + e.getClass().getName(), e);
			return;
		}
		beanList = doc.getRootElement().getChildren(BEAN);
	}	
	public void load(InputStream config, String id) {

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(config, id);
		} catch (JDOMException | IOException e) {
			LOG.log(Level.SEVERE, "Error Reading XML file:" + e.getClass().getName(), e);
			return;
		}
		beanList = doc.getRootElement().getChildren(BEAN);
	}
	@Override
	public void load(Reader config) {

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(config);
		} catch (JDOMException | IOException e) {
			LOG.log(Level.SEVERE, "Error Reading XML file:" + e.getClass().getName(), e);
			return;
		}
		beanList = doc.getRootElement().getChildren(BEAN);
	}
	
	public void load(Reader config, String id) {
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(config, id);
		} catch (JDOMException | IOException e) {
			LOG.log(Level.SEVERE, "Error Reading XML file:" + e.getClass().getName(), e);
			return;
		}
		beanList = doc.getRootElement().getChildren(BEAN);
	}


	@Override
	synchronized public void build() {
		
		try {
			for (Element beanElem : beanList) {
				
				String bnId = beanElem.getAttributeValue(BEAN_ID);
				String bnScope = beanElem.getAttributeValue(BEAN_SINGLETON);
				
				boolean isSingleton = 
						(bnScope == null
							|| (bnScope != null && Boolean.valueOf(bnScope.trim()))
						);

				if (bnId != null)  {
					
					Pod pod = null;
					
					if (isSingleton) {
						
						Object bean = buildBean(beanElem);
						pod = new Pod(bnId, true, null, bean);
						
					} else {
						pod = new Pod(bnId, false, beanElem, null);
					}
					podMap.put(bnId, pod);
				}
			}
		} catch (Exception e) {

			LOG.log(Level.SEVERE, "Error Beans:" + e.getMessage(), e);
			return;
		}
	}
	
	protected Object buildBean(Element beanElem) throws Exception {
		/**
		 * For each property, there are 3 condition:
		 * 
		 * 1. <property name="Xyz"><value="Xyz"/></porperty>
		 * 
		 * 2. <property name="Xyz" value="Xyz"/>
		 * 
		 * 3. no property is needed.
		 */

		List<Element> propLs = beanElem.getChildren(BEAN_PROPERTY);
		Class<?> bnClass = getClass(beanElem);
		Object bean = bnClass.newInstance();

		/**
		 * All hooks here
		 */
		if (bean instanceof IBeanFactoryAware) {
			((IBeanFactoryAware)bean).setBeanFactroy(this);
		}
		
		
		if (propLs != null && propLs.size() > 0) {

			for (Element prop : propLs) {

				String propName = prop.getAttributeValue(PROPERTY_NAME).trim();

//				Element elem = prop.getChildren();
				Element elemVar = prop.getChild(PROPERTY_VALUE);// <value></value>
				Element elemRef = prop.getChild(PROPERTY_REF);// <ref></ref>
				Element elemBean = prop.getChild(BEAN);// <bean></bean>
				Element elemList = prop.getChild(PROPERTY_LIST);// <List></list>

				if (elemVar != null) {
					setStrProperty(bnClass, bean, propName, elemVar.getValue()
							.trim());

				} else if (elemBean != null) {
					/**
					 * build recursively
					 */

					Object nBean = buildBean(elemBean);
					setBeanProperty(bnClass, bean, propName, nBean);

				} else if (elemRef != null) {

					// Object refBn =
					// podMap.get(elemRef.getValue().trim()).getInstance();
					Object refBn = getBean(elemRef.getValue().trim());

					setBeanProperty(bnClass, bean, propName, refBn);

				} else if (elemList != null) {

					List<Element> vars = elemList.getChildren();
					// org.jdom2.Attribute attribute = vars.get(0).getAttribute(
					// PROPERTY_VALUE);
					// System.out.println(attribute.getQualifiedName());
					// if (vars != null && vars.size() > 0) {
					//
					// List<Object> varArray = new ArrayList<>(vars.size());
					// for (Element var : vars) {
					// String lsValue =
					// vars.get(0).getAttributeValue(PROPERTY_VALUE);
					// String lsRef =
					// vars.get(0).getAttributeValue(PROPERTY_REF);
					// String lsBean = vars.get(0).getAttributeValue(BEAN);
					// if (null != var.getAttributeValue(PROPERTY_VALUE)) {
					//
					// } else if (null != var.getAttributeValue(PROPERTY_REF)) {
					//
					// } else if (null != var.getAttributeValue(BEAN)) {
					//
					// }
					// varArray.add(var.getValue().trim());
					// }
					// }

					List<String> varArray = new ArrayList<>(vars.size());
					for (Element var : vars) {
						varArray.add(var.getValue().trim());
					}
					/*
					 * set list
					 */
					setListProperty(bnClass, bean, propName, varArray);

				} else { // value and ref is inside <property>

					String propVar = prop.getAttributeValue(PROPERTY_VALUE);
					String propRef = prop.getAttributeValue(PROPERTY_REF);

					if (propVar != null) {
						/**
						 * set new
						 */
						setStrProperty(bnClass, bean, propName, propVar.trim());

					} else if (propRef != null) {

						Object refBean = getBean(propRef);

						setBeanProperty(bnClass, bean, propName, refBean);
					}

				} // value inside

			} // for properties
		}
		return bean;
	}
	
	protected static void setListProperty(Class<?> bnClass,
									Object bean,
									String propName,
									List<String> ls) throws Exception{
		
		Method method = TypeTraits.findSetter(bnClass, propName);
		
		if (method == null) {
			if (REFLECT_ON_PRIVATE) {
				TypeTraits.assignField(bean, propName, ls, true);
				return;
			} else {
				LOG.severe("cannot find setter for " + propName);
				return;
			}
		}
		
		Class<?>[] paramTypes = method.getParameterTypes();
		
		if (paramTypes != null && paramTypes.length == 1) {
			if (paramTypes[0].isAssignableFrom(ls.getClass())) {
//System.out.println("invoking " + method.getName());
				method.invoke(bean, ls);
				
			} else {
				
				if (REFLECT_ON_PRIVATE) {
					TypeTraits.assignField(bean, propName, ls, true);
					return;
				} else {

					LOG.severe(" Line : " + new Throwable().getStackTrace()[0].getLineNumber()
							+ "in class " + bnClass.getName()
							+" cannot assigning ArrayList to parameter " + propName);
					return;
				}
			}

		} else {
			LOG.severe(
					"in class : " + bnClass.getName()
					 +" setter: " + method.getName()
						+ "has more than one parameters");
			return;
		}
		
	}
	
	protected static void setBeanProperty(Class<?> bnClass,
									Object bean,
									String propName,
									Object var)throws Exception {
		
		Method method = TypeTraits.findSetter(bnClass, propName);
		if (method == null) {
			if (REFLECT_ON_PRIVATE) {
				TypeTraits.assignField(bean, propName, var, true);
				return;
			} else {
				LOG.severe(
						"in class " + bnClass.getName()
						+ "cannot find setter for " + propName);
				return;
			}
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length == 1) {

			method.invoke(bean, var);

		} else {
			LOG.severe(
					"in class : " + bnClass.getName()
					 +" setter: " + method.getName()
						+ "has more than one parameters");
			return;
		}
	}
	
	protected  void setStrProperty(Class<?> bnClass,
									Object bean,
									String propName,
									String varStr) throws Exception {

		Method method = TypeTraits.findSetter(bnClass, propName);
		if (method == null) {
			if (REFLECT_ON_PRIVATE) {
				TypeTraits.assignField(bean, propName, varStr, true);
				return;
			} else {
				LOG.severe(
						"in class : " + bnClass.getName()
						 +"cannot find setter for property: " + propName);
				return;
			}
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		
		if (paramTypes.length == 1) {

			Object var = Converter.to(varStr, paramTypes[0]);
			
			method.invoke(bean, var);

		} else {
			
			LOG.severe(
					"in class : " + bnClass.getName()
					 +" setter: " + method.getName()
						+ "has more than one parameters");
			return;
		}
	}

	protected Class<?> getClass(Element element) throws ClassNotFoundException {
		String clazzName = element.getAttributeValue(BEAN_CLASS).trim();
//System.out.println(clazzName);
		return Class.forName(clazzName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public<T> T getBean(String string) {

		Pod pod = podMap.get(string);
		if (pod == null) {
			throw new NullPointerException("cannot find bean[" + string + "]");
		}
		if (pod.isSingleton()) {

			return (T) pod.getInstance();

		} else {
			
			try {
				return (T) buildBean(pod.getDescription());
			} catch (Exception e) {
				LOG.severe(
					"cannot build non-singleton java bean.\nError : " 
				+ e.getClass().getName()
				+ "\n" + e.getMessage() + "\n" + e.getCause());
				return null;
			}
		}
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
	public<T> void updateBean(String id, T bean) {
		
		Pod oldPod = podMap.get(id);
		if (oldPod == null) {
			throw new NullPointerException("cannot find bean[" + id + "]");
		}
		if (oldPod.getInstance() != null) {
			oldPod.setInstance(bean);
			podMap.put(id, oldPod);
		} else {
			LOG.severe(
					"bean of id [" + id + "] does not exsits");
		}
	}

	@Override
	public void addBean(String id, Object bean) {
		if (contains(id)) {
			throw new IllegalArgumentException("bean [" + id + "] alreadt exesits");
		}
		Pod pod = new Pod(id, false, null, bean);
		podMap.put(id, pod);
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
	 */
	@Override
	public void inTake(Visitor visitor) {
		
		for (Map.Entry<String, Pod> entry : podMap.entrySet()) {

			Pod pod = entry.getValue();
			visitor.visit(getBean(pod.getBeanId()));
		}
	}
	
	protected static class Pod {

		private String beanId;

		private boolean singleton;
		private Element description;

		private Object instance;



		public Pod(String id, boolean singleton, Element d, Object instance) {
			this.beanId = id;
			this.singleton = singleton;
			this.description = d;
			this.instance = instance;
		}


		public void setInstance(Object instance) {
			this.instance = instance;
		}
		
		public boolean isSingleton() {
			return singleton;
		}

		public Object getInstance() {
			return instance;
		}

		public Element getDescription() {
			return description;
		}
		
		public String getBeanId() {
			return beanId;
		}
	}

}
