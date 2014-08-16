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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.caibowen.gplume.context.InputStreamCallback;
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
public abstract class XMLBeanAssemblerBase extends XMLAux {

	protected static final String LOGGER_NAME = "BeanAssembler";
	
	private static final Logger LOG = LoggerFactory.getLogger(LOGGER_NAME);

	protected Map<String, Pod> podMap = new ConcurrentHashMap<>(64);


	public void setClassLoader(ClassLoader loader) {
		this.classLoader = loader;
	}
	
	@Nonnull
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
			case XMLTags.CONFIG:
				handleConfig(elem);
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

    void handleBean(Element elem) throws Exception {

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
            bean = super.buildBean(elem);
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
    void handleProperties(Element elem) {
        configCenter.scanXMLElem(elem);
    }

    /**
     * build beans from other config file
     * @param elem
     * @throws Exception
     */
    void handleConfig(Element elem) throws Exception {
        String loc = elem.getTextContent().trim();
        final DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();

        configCenter.withPath(loc, new InputStreamCallback() {
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

		if (beanObj instanceof BeanClassLoaderAware) {
			((BeanClassLoaderAware)beanObj).setBeanClassLoader(this.classLoader);
			LOG.info(
					"BeanClassLoaderAware bean[" 
					+ beanObj.getClass().getSimpleName() 
					+ "] ClassLoader setted");
		}
	}

 }