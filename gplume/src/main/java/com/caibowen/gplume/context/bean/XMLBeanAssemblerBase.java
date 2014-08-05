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

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.caibowen.gplume.context.InputStreamCallback;
import com.caibowen.gplume.context.InputStreamSupport;
import com.caibowen.gplume.core.BeanEditor;
import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;


/**
 * base class of XMLBeanAssemble
 * parse xml and build beans
 * 
 * @author BowenCai
 *
 */
public abstract class XMLBeanAssemblerBase extends InputStreamSupport implements IBeanAssembler {

	protected static final String LOGGER_NAME = "BeanAssembler";
	
	private static final Logger LOG = LoggerFactory.getLogger(LOGGER_NAME);

	protected Map<String, Pod> podMap = new ConcurrentHashMap<>(64);
	protected Map<String, String> globlaProperties = new HashMap<String, String>(32);
	
	protected ClassLoader classLoader;

	@Override
	public void setClassLoader(ClassLoader loader) {
		this.classLoader = loader;
	}
	
	@Nonnull
	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}
	
	/**
	 * xml bean factory being singleton implies that this function is not
	 * reenterable, thus it is thread safe
	 * 
	 * @throws Exception
	 */
	protected void doAssemble(Document doc) throws Exception {

		NodeList nodeList = doc.getChildNodes();
		Node node = null;
		
		// escape comments etc, advance to the first node
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node tn = nodeList.item(i);
			if (tn != null && tn.getNodeType() == Node.ELEMENT_NODE) {
				node = tn.getFirstChild();
				break;
			}
		}

		if (node == null) { //  no bean definition
			throw new IllegalArgumentException("no bean definition found");
		}
		
		while (node.getNextSibling() != null) {

			node = node.getNextSibling();
			Element elem;
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				elem = (Element) node;
			} else {
				continue;
			}

			switch (elem.getNodeName()) {
			case XMLTags.IMPROT:
				handleImport(elem);
				break;
				
			case XMLTags.PROPERTIES:
				handleProperties(elem);
				break;
				
			case XMLTags.BEAN:
                handleBean(elem);
                break;

			default:
				break;
			}
		}
		
	}


    protected void handleBean(Element elem) throws Exception {

        int lifeSpan;
        String strLife = elem.getAttribute(XMLTags.BEAN_LIFE_SPAN);
        if (Str.Utils.notBlank(strLife)) {
            lifeSpan = Converter.toInteger(strLife);
        } else {
            lifeSpan = Integer.MAX_VALUE;
        }

        String bnId = elem.getAttribute(XMLTags.BEAN_ID);
        Pod pod = null;

        String bnScope = elem.getAttribute(XMLTags.BEAN_SINGLETON);

        boolean isSingleton = true;
        if (Str.Utils.notBlank(bnScope)) {
            isSingleton = Converter.toBool(bnScope);
        }
        Object bean = null;
        if (isSingleton) {
            bean = buildBean(elem);
            pod = new Pod(bnId, null, bean, lifeSpan);
        } else {
            pod = new Pod(bnId, elem, null, lifeSpan);
        }

        if (Str.Utils.notBlank(bnId)) {
            podMap.put(bnId, pod);
        }

        LOG.info("Add Bean: id[{0}] of class[{1}] singleton ? {2}  lifeSpan {3}",
                bnId, (bean != null ? bean.getClass().getName() : "unknown")
                , isSingleton
                , lifeSpan
        );
    }
//	public static void main(String...a) {
//		replaceIfPresent("hahaha ${name sad} ooo ${second-hahaha} back-back");
//	}

    /**
     * support multi property in one string
     * hahaha ${name sad} ooo ${second-hahaha} back-back
     *
     * do not support nested properties
     *
     * hahaha ${name sad${second-hahaha}} ooo  back-back -> goes wrong
     *
     *
     * @param name
     * @return
     */
    @Nonnull
    protected String replaceIfPresent(@Nonnull String name) {
        name = name.trim();
        int lq = 0;
        int rq = 0;
        int lastL = 0;
        StringBuilder b = new StringBuilder(name.length() * 3);

        lq = name.indexOf("${", rq);
        while (lq != -1) {
            rq = name.indexOf('}', lq);
            if (rq == -1)
                throw new IllegalArgumentException(
                        "configuration: unclosed property [" + name + "]");

            b.append(name.substring(lastL, lq));

            String k = name.substring(lq + 2, rq);
            b.append(globlaProperties.get(k.trim()));
            lq = name.indexOf("${", rq);
            lastL = rq + 1;
        }
        b.append(name.substring(lastL, name.length()));

        return b.toString();
    }


	/**
	 * <pre>
	 * add to globlaProperties from:
	 * 1. <keyname> value </keyname>
	 * 
	 * 2. <properties import="classpath:hahaha.porperties">
	 * 		<keyname> this pair will be added too </keyName>
	 * 	  </properties>	
	 * </pre>
	 * 
	 * imported file with extension ".xml" -> properties.loadFromXML
	 * otherwise -> properties.load
	 * 
	 * Note that same key in properties file with be covered by key in config xml
	 * e.g, form example above, the value for "keyname" will be "this pair will be added too "
	 * 
	 * @param elem
	 */
	protected void handleProperties(Element elem) {
		
		final String loc = elem.getAttribute(XMLTags.IMPROT).trim();
		final Properties p = new Properties();
		withPath(loc, new InputStreamCallback() {
			@Override
			public void doInStream(InputStream stream) throws Exception {
				if (loc.endsWith(".xml"))	
					p.loadFromXML(stream);
				else
					p.load(stream);
			}
		});
		
		for (Map.Entry<?, ?> e: p.entrySet()) {
			Object k = e.getKey();
			Object v = e.getValue();
			if (k instanceof String && v instanceof String)
				globlaProperties.put((String)k, (String)v);
		}
		
		NodeList nls = elem.getChildNodes();
		for (int i = 0; i < nls.getLength(); i++) {
			Node nn = nls.item(i);
			if (nn.getNodeType() == Node.ELEMENT_NODE) {
                Element ne = (Element) nn;
                String k = ne.getTagName().trim();
                String v = ne.getTextContent().trim();
                if (!globlaProperties.containsKey(k)) {
                    globlaProperties.put(k, v);
                    LOG.info("add property:  \"{0}\" : \"{1}\"", k, v);
                } else
					throw new IllegalArgumentException(
						"duplicated key [" + k
						+ "]\r\n first defined in properties file[" 
								+ loc + "] as [" + globlaProperties.get(k) 
						+ "]\r\n second defined in config xml as [" + v + "]");

			}
		}
	}
	
	/**
	 * build beans from other config file 
	 * @param elem
	 * @throws Exception 
	 */
	protected void handleImport(Element elem) throws Exception {
		String loc = elem.getTextContent().trim();
		final DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		withPath(loc, new InputStreamCallback() {
			@Override
			public void doInStream(InputStream stream) throws Exception {
				Document doc = builder.parse(stream);
				doc.getDocumentElement().normalize();
				doAssemble(doc);
			}
		});
		
		LOG.info("importing configuration from {0}, {1} beans created", loc, podMap.size());
	}
	
	/**
	 * after process (often with life circle managemetn )is done in Pod
	 * @see Pod
	 * @param beanObj
	 */
	protected void preprocess(Object beanObj) {
		if (beanObj instanceof IBeanAssemblerAware) {
			((IBeanAssemblerAware) beanObj).setBeanAssembler(this);
			LOG.info(
					"IBeanAssemblerAware bean[" 
					+ beanObj.getClass().getSimpleName() 
					+ "] beanAssembler setted");
		}
		if (beanObj instanceof BeanClassLoaderAware) {
			((BeanClassLoaderAware)beanObj).setBeanClassLoader(this.classLoader);
			LOG.info(
					"BeanClassLoaderAware bean[" 
					+ beanObj.getClass().getSimpleName() 
					+ "] ClassLoader setted");
		}
	}
	
	/**
	 * For each property, there are 3 notation:
	 * 
	 * 1. <property id="Xyz"><value="Xyz"/></porperty>
	 * 
	 * 2. <property id="Xyz" value="Xyz"/>
	 * 
	 * 3. no property is needed.
	 */
	protected Object buildBean(Element beanElem) throws Exception {

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
		preprocess(beanObj);
		
		LOG.info("bean class[{0}] created", bnClass.getSimpleName());
		
		NodeList propLs = beanElem.getElementsByTagName(XMLTags.BEAN_PROPERTY);
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

			String propName = prop.getAttribute(XMLTags.PROPERTY_NAME);
			propName = replaceIfPresent(propName);
			if (!Str.Utils.notBlank(propName)) {
				throw new NullPointerException(
						"Property id is empty. \r\n NodeName: ["
								+ prop.getNodeName() + "]");
				
			}
			
//System.out.println("class [" + bnClass.getSimpleName() + "] prop[" + propName + "]");

			NodeList varList = prop.getChildNodes();
			if (varList == null || varList.getLength() == 0) {
				// property inside, one string value or one ref
				String varObj = replaceIfPresent(
						prop.getAttribute(XMLTags.PROPERTY_INSTANCE));

				String varStr = replaceIfPresent(
						prop.getAttribute(XMLTags.PROPERTY_VALUE));
				String varRef = replaceIfPresent(
						prop.getAttribute(XMLTags.PROPERTY_REF));
				
				if (Str.Utils.notBlank(varStr)) {
					// e.g., <property id="number" value="5"/>
					// str value will casted to param type if needed
					BeanEditor.setProperty(beanObj, propName, varStr.trim());
					continue;
					
				} else if (Str.Utils.notBlank(varRef)) {
					// e.g., <property id="bean" ref="someOtherBean"/>
					Object ref = getBean(varRef.trim());
					BeanEditor.setProperty(beanObj, propName, ref);
					continue;
					
				} else if (Str.Utils.notBlank(varObj)) {
					// e.g. <property id="injector" object="com.caibowen.gplume.core.Injector"/>
					Class<?> klass = this.classLoader.loadClass(varObj);
					Object obj = klass.newInstance();
					preprocess(obj);
					BeanEditor.setProperty(beanObj, propName, obj);
					continue;
					
				} else {
					throw new IllegalArgumentException("No value for property["
							+ propName + "] in class [" + bnClass.getName()
							+ "]");
				}

			} else {// properties outside
				
				// check if is list
				boolean isList = false;
				boolean isMap = false;
				/**
				 * 
				 * prop
				 * |
				 * first child -> next
				 * 					|
				 * 				    <list> or <value>
				 */
				Node iter = prop.getFirstChild().getNextSibling();

				if (XMLTags.PROPERTY_LIST.equals(iter.getNodeName())) {
					isList = true;
					isMap = false;
				} else if (XMLTags.PROPERTY_MAP.equals(iter.getNodeName())) {
					isList = false;
					isMap = true;
				}

				if (isMap) {
					/**
					 * <list> or <map>
					 * |
					 * first child -> next
					 * 					|
					 * 				    <key> or <value>
					 */
					iter = iter.getFirstChild().getNextSibling();
					Properties properties = new Properties();
					
					while (iter != null && iter.getNodeType() == Node.ELEMENT_NODE) {
						Element elemBn = (Element) iter;
						if (!XMLTags.PROPERTY_MAP_PROP.equals(elemBn.getNodeName())) {
							throw new IllegalArgumentException(
							"cannot have [" + elemBn.getNodeName() + "] under tag <props>, must be <prop> only");
						}
						String mapK = replaceIfPresent(
								elemBn.getAttribute(XMLTags.PROPERTY_MAP_KEY));
						
						if (!Str.Utils.notBlank(mapK)) {
							throw new NullPointerException("empty map key for property[" + propName + "]");
						}
						String mapV = replaceIfPresent(
								elemBn.getTextContent());
						
						if (!Str.Utils.notBlank(mapV)) {
							mapV = replaceIfPresent(
									elemBn.getAttribute(XMLTags.PROPERTY_VALUE));
							
							if (!Str.Utils.notBlank(mapV)) {
								throw new NullPointerException(
										"empty map value of key [" 
											+ mapK + "] for property[" + propName + "]");
							}
						}
						properties.setProperty(mapK, mapV);
						// skip node of #text
						iter = iter.getNextSibling().getNextSibling();
					}
					BeanEditor.setProperty(beanObj, propName, properties);
					
				} else { // single value or list
					if (isList) {
						iter = iter.getFirstChild().getNextSibling();
					}
					// first get all no matter is string literal, ref or new beans
					ArrayList<Object> beanList = new ArrayList<Object>(8);
					while (iter != null && iter.getNodeType() == Node.ELEMENT_NODE) {

						Element elemBn = (Element) iter;
						
						if (XMLTags.BEAN.equals(elemBn.getNodeName())) {
							beanList.add(buildBean(elemBn));

						} else if (XMLTags.PROPERTY_REF.equals(elemBn.getNodeName())) {
							beanList.add(getBean(
									replaceIfPresent(
											elemBn.getTextContent())
												)
										);

						} else if (XMLTags.PROPERTY_VALUE.equals(elemBn.getNodeName())) {
							beanList.add(replaceIfPresent(
											elemBn.getTextContent()
											)
										);
						} else {
							throw new IllegalArgumentException("Unknown property["
									+ iter.getNodeName() + "]");
						}
						// skip node of #text
						iter = iter.getNextSibling().getNextSibling();
					}// while
					
					// second, use this list
					if (isList) {
						// set list or array
						BeanEditor.setListProperty(beanObj, propName, beanList);
						
					} else {
						if (beanList.size() == 1) {
							BeanEditor.setProperty(beanObj, propName, beanList.get(0));
							
						} else {
							throw new IllegalArgumentException(
									"Bean number miss match for property[" + propName
											+ "] in class ["
											+ bnClass.getName() + "]\n"
											+ "needs 1 actual " + beanList.size()
											+ "beans : " + beanList.toString());
						}
					}
				}

			} // props

		} // for properties

		return beanObj;
	}
	

	protected @Nonnull Class<?> getClass(Element element) {
		String clazzName = element.getAttribute(XMLTags.BEAN_CLASS).trim();
		clazzName = replaceIfPresent(clazzName);
		try {
			return this.classLoader.loadClass(clazzName);
		} catch (Exception e) {
			throw new RuntimeException(
				MessageFormat.format("cannot load class defined in XML " + element,
								clazzName)
								, e);
		}
	}
}
