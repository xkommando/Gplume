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
package com.caibowen.gplume.context;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.resource.InputStreamCallback;
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
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import static com.caibowen.gplume.misc.Str.EMPTY;


/**
 * base class of XMLBeanAssemble
 * parse xml and build beans:
 *
 * IBeanAssembler: interface
 *              \
 *      XMLAssemblerBase: organize beans, processing xml. |  IBeanBuilder: Build bean based on XML node information, hsa config center
 *                      \
 *
 *                  XMLAssembler:  implement/extend interface and bean CRUD method
 * @author BowenCai
 *
 */
public abstract class XMLAssemblerBase implements IBeanAssembler {
	
	private static final Logger LOG = LoggerFactory.getLogger(XMLAssemblerBase.class);

    protected final SpaceTree<Pod> tree;

    protected String currentNamespace;

    protected String refNamespace;
    protected IBeanBuilder beanBuilder;
    protected Map<String, ProxyBean> deferred;

    public XMLAssemblerBase() {
        tree = new SpaceTree<>();
        currentNamespace = EMPTY;
        refNamespace = EMPTY;
        deferred = new TreeMap<>();
        beanBuilder = new DefaultBeanBuilder();
        beanBuilder.setAssembler(this);
    }

    public XMLAssemblerBase(IBeanBuilder builder) {
        tree = new SpaceTree<>();
        currentNamespace = EMPTY;
        refNamespace = EMPTY;
        deferred = new TreeMap<>();
        beanBuilder = builder;
    }

    /**
     * create proxy for this bean
     *
     * @param id
     * @param createNew
     * @param tgtClass
     * @return
     */
    @Nonnull
    public Object getForBuild(@Nonnull String id, boolean createNew,
                              @Nonnull Class<?> tgtClass) {
        String _id = tree.createFullPath(id, currentNamespace);
        Object bn = tree.find(_id);
        if (bn != null)
            return bn;
        ProxyBean pb = deferred.get(_id);
        if (pb == null && createNew) {
            pb = new ProxyBean(tgtClass);
            deferred.put(_id, pb);
            return Proxy.newProxyInstance(beanBuilder.getClassLoader(), new Class[]{tgtClass}, pb);
        } else
            throw new NoSuchElementException("cannot find proxy bean of id[" + id + "] for class[" + tgtClass + "]");
    }

    protected void prepareAssemble(Document doc) {
        LOG.trace("Initializing assembling");
    }

    protected void finishAssemble(Document doc) {
        for (Map.Entry<String, ProxyBean> e : deferred.entrySet()) {
            String id = e.getKey();
            ProxyBean pb = e.getValue();
            Object realBean = getBean(id);
            if (realBean == null)
                throw new IllegalStateException("Could not find bean[" + id + "]");
            if (pb.inited())
                throw new IllegalStateException("Proxy Bean already has value");
            pb.init(realBean);
        }
        LOG.trace("Assembling finished, {} beans, created", tree.size());
    }
    /**
	 * xml bean factory being singleton implies that this function is not
	 * reenterable, thus it is thread safe
	 * 
	 * @throws Exception
	 */
	synchronized protected void doAssemble(Document doc) throws Exception {
        prepareAssemble(doc);

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
        refNamespace = getRefNS(rootElem);

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

                case XMLTags.DEFINE:
                    handleDefProperties(elem);
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

        finishAssemble(doc);
	}

    protected static String getCurrentNS(Element rootX) {
        if (rootX == null)
            throw new IllegalArgumentException("cannot find <beans>");
        String nsStr = rootX.getAttribute(XMLTags.NAMESPACE);
        if (Str.Utils.isBlank(nsStr))
            return EMPTY;
        return nsStr;
    }

    protected static String getRefNS(Element rootX) {
        if (rootX == null)
            throw new IllegalArgumentException("cannot find <beans>");
        String nsStr = rootX.getAttribute(XMLTags.USING_NS);
        if (Str.Utils.isBlank(nsStr))
            return EMPTY;
        return nsStr;
    }

    protected void handleBean(Element elem) throws Exception {

        String bnId = elem.getAttribute(XMLTags.BEAN_ID);

        boolean addBean = true;
        if (Str.Utils.notBlank(bnId)) {
            bnId = tree.createFullPath(bnId, currentNamespace);
            if (tree.find(bnId) != null)
                throw new IllegalArgumentException("Duplicated bean definition [" + bnId + "]");

        } else addBean = false;


        String bnScope = elem.getAttribute(XMLTags.BEAN_SINGLETON);

        boolean isSingleton = true;
        if (Str.Utils.notBlank(bnScope)) {
            isSingleton = Converter.toBool(bnScope);
        }
        Object bean = null;
        if (isSingleton) {
            bean = beanBuilder.buildBean(elem, bnId);
            if (addBean)
                tree.put(bnId, new Pod(bnId, null, bean));

            beanBuilder.afterProcess(bean, elem);

        } else {
            if (addBean)
                tree.put(bnId, new Pod(bnId, elem, null));
        }

        LOG.trace("Add Bean: id[{}] of class[{}] singleton ? {}",
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
    protected void handleDefProperties(Element elem) {
        beanBuilder.getConfigCenter().scanXMLElem(elem);
    }

    /**
     * build beans from other config file
     * @throws Exception
     */
    protected void handleConfig(Element elem) throws Exception {
        String loc = elem.getTextContent().trim();
        final DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();

        String oldCur = currentNamespace;
        String oldRef = refNamespace;
        int oldsz = tree.size();
        beanBuilder.getConfigCenter().withPath(loc, new InputStreamCallback() {
            @Override
            public void doInStream(InputStream stream) throws Exception {
                Document doc = builder.parse(stream);
                doc.getDocumentElement().normalize();
                doAssemble(doc);
            }
        });

        currentNamespace = oldCur;
        refNamespace = oldRef;
        LOG.trace("importing configuration from {}, {} beans created", loc, (tree.size() - oldsz));
    }

    void handleRequire(Element elem) {

        throw new UnsupportedOperationException();
    }


    @Override
    public void setClassLoader(@Nonnull ClassLoader loader) {
        beanBuilder.setClassLoader(loader);
    }

    @Override
    @Nonnull
    public ClassLoader getClassLoader() {
        return beanBuilder.getClassLoader();
    }

    @Override
    public String getCurrentNamespace() {
        return currentNamespace;
    }

    @Override
    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    @Override
    public String getRefNamespace() {
        return refNamespace;
    }

    @Override
    public void setRefNamespace(String refNamespace) {
        this.refNamespace = refNamespace;
    }

    @Override
    public void setConfigCenter(ConfigCenter configCenter) {
        beanBuilder.setConfigCenter(configCenter);
    }

    @Override
    public ConfigCenter getConfigCenter() {
        return beanBuilder.getConfigCenter();
    }
}
