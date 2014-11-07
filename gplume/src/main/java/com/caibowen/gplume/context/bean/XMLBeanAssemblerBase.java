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

import com.caibowen.gplume.context.InputStreamCallback;
import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;


/**
 * base class of XMLBeanAssemble
 * parse xml and build beans
 * 
 * @author BowenCai
 *
 */
public abstract class XMLBeanAssemblerBase extends BeanCreator {
	
	private static final Logger LOG = LoggerFactory.getLogger(XMLBeanAssemblerBase.class);

//	protected Map<String, Pod> podMap = new ConcurrentHashMap<>(64);

    protected final PodTree tree = new PodTree();

    protected String currentNamespace = Str.EMPTY;

    protected String referedNamespace = Str.EMPTY;

    public void setClassLoader(ClassLoader loader) {
		this.classLoader = loader;
	}
	@Nonnull
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}
    public String getCurrentNamespace() {
        return currentNamespace;
    }
    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }
    public String getReferedNamespace() {
        return referedNamespace;
    }
    public void setReferedNamespace(String referedNamespace) {
        this.referedNamespace = referedNamespace;
    }

    /**
	 * xml bean factory being singleton implies that this function is not
	 * reenterable, thus it is thread safe
	 * 
	 * @throws Exception
	 */
	synchronized protected void doAssemble(Document doc) throws Exception {

		NodeList _nodeList = doc.getChildNodes();
		Element rootElem = null;
        Node beanNodeIter = null;
		
		// escape comments etc, advance to the first node
		for (int i = 0; i < _nodeList.getLength(); i++) {
			Node tn = _nodeList.item(i);
			if (tn != null && tn.getNodeType() == Node.ELEMENT_NODE
                    && XMLTags.ROOT_BEANS.equals(tn.getNodeName())) {
                rootElem = (Element)tn;
				beanNodeIter = tn.getFirstChild();
				break;
			}
		}

		if (beanNodeIter == null) //  nothing
			throw new IllegalArgumentException("no configuration found");

        currentNamespace = getCurrentNS(rootElem);
        referedNamespace = getRefNS(rootElem);

        // start to process
		while (beanNodeIter.getNextSibling() != null) {

			beanNodeIter = beanNodeIter.getNextSibling();
			Element elem;
			if (beanNodeIter.getNodeType() == Node.ELEMENT_NODE)
				elem = (Element) beanNodeIter;
			else continue;

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

                case XMLTags.REQUIRED:
                    handleRequire(elem);
                    break;

                default:
                    LOG.error("unknown tag[" + elem.getNodeName() + "]");
                    break;
            }
        }

	}

    String getCurrentNS(Element rootX) {
        if (rootX == null)
            throw new IllegalArgumentException("cannot find <beans>");
        String nsStr = rootX.getAttribute(XMLTags.NAMESPACE);
        if (Str.Utils.isBlank(nsStr))
            return "";
        return nsStr;
    }

    String getRefNS(Element rootX) {
        if (rootX == null)
            throw new IllegalArgumentException("cannot find <beans>");
        String nsStr = rootX.getAttribute(XMLTags.USING_NS);
        if (Str.Utils.isBlank(nsStr))
            return "";
        return nsStr;
    }

    void handleBean(Element elem) throws Exception {

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
            pod = new Pod(bnId, null, bean);
        } else {
            pod = new Pod(bnId, elem, null);
        }

        if (Str.Utils.notBlank(bnId)) {
            if (! tree.addPod(currentNamespace + XMLTags.NS_DELI + bnId, pod))
                throw new IllegalArgumentException("duplicated bean definition [" + bnId + "]");
        }

        LOG.debug("Add Bean: id[{}] of class[{}] singleton ? {}",
                bnId, (bean != null ? bean.getClass().getName() : "unknown")
                , isSingleton
        );
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

        String oldCur = currentNamespace;
        String oldRef = referedNamespace;
        int oldsz = tree.size();
        configCenter.withPath(loc, new InputStreamCallback() {
            @Override
            public void doInStream(InputStream stream) throws Exception {
                Document doc = builder.parse(stream);
                doc.getDocumentElement().normalize();
                doAssemble(doc);
            }
        });

        currentNamespace = oldCur;
        referedNamespace = oldRef;
        LOG.debug("importing configuration from {}, {} beans created", loc, (tree.size() - oldsz));
    }

    void handleRequire(Element elem) {
        throw new UnsupportedOperationException();
    }
 }
