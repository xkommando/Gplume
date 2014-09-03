/*
 * *****************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.context.bean;

import com.caibowen.gplume.core.BeanEditor;
import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *  auxiliary for xml parsing
 *
 * @author bowen.cbw
 * @since 8/16/2014.
 */
abstract class XMLAux implements IBeanAssembler {

    private static final Logger LOG = LoggerFactory.getLogger(XMLAux.class);

    protected ClassLoader classLoader;

    protected ConfigCenter configCenter;
    @Override
    public void setConfigCenter(ConfigCenter configCenter) {
        this.configCenter = configCenter;
    }

    @Override
    public ConfigCenter getConfigCenter() {
        return configCenter;
    }

    protected @Nonnull Class<?> getClass(Element element) {
        String clazzName = element.getAttribute(XMLTags.BEAN_CLASS).trim();
        clazzName = configCenter.replaceIfPresent(clazzName);
        try {
            return this.classLoader.loadClass(clazzName);
        } catch (Exception e) {
            throw new RuntimeException(
                    MessageFormat.format("cannot load class defined in XML " + element,
                            clazzName)
                    , e);
        }
    }

    /**
     * if <construct /> exists :
     *  if has content , move to content
     *  else return original position
     *
     * @param beanElem
     * @return
     */
    private @Nullable Element
    findCtorElem(Element beanElem) {
        Element ctorElem = null;
        Node _n = beanElem.getFirstChild();
        while (_n != null && _n.getNextSibling() != null) {
            _n = _n.getNextSibling();
            if (_n.getNodeName().equals(XMLTags.BEAN_CONSTRUCT)){
                Node iter;
                if (_n.getFirstChild() != null
                        && null != (iter = _n.getFirstChild().getNextSibling()))
                    ctorElem = (Element)iter;
                else
                    ctorElem = (Element)_n;
                break;
            }
        }
        return ctorElem;
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
    protected @Nonnull Object
    buildBean(Element beanElem) throws Exception {

        Class<?> bnClass = getClass(beanElem);
        int modi = bnClass.getModifiers();

        if (Modifier.isAbstract(modi)) {
            throw new IllegalStateException("class[" + bnClass.getName()
                    + "] is abstract and cannot be instantiated");
        } else if (Modifier.isInterface(modi)) {
            throw new IllegalStateException("class[" + bnClass.getName()
                    + "] is interface and cannot be instantiated");
        }

        Object beanObj = construct(bnClass, findCtorElem(beanElem));

        LOG.info("bean class[{0}] created", bnClass.getSimpleName());


        NodeList _propLs = beanElem.getElementsByTagName(XMLTags.BEAN_PROPERTY);
        if (_propLs == null || _propLs.getLength() == 0) {
            // no property
            return beanObj;
        }

        // get top level properties only, skip properties in sub beans
        Node next = beanElem.getFirstChild();
        while (next.getNextSibling() != null) {
            Element prop;
            next = next.getNextSibling();

            if (next.getNodeType() == Node.ELEMENT_NODE
                    && next.getNodeName().equals(XMLTags.BEAN_PROPERTY)) {
                prop = (Element) next;
            } else {
                continue;
            }

            String propName = prop.getAttribute(XMLTags.PROPERTY_NAME);
            propName = configCenter.replaceIfPresent(propName);
            if (!Str.Utils.notBlank(propName)) {
                throw new NullPointerException(
                        "Property id is empty. NodeName: ["
                                + prop.getNodeName() + "]");

            }

//System.out.println("class [" + bnClass.getSimpleName() + "] prop[" + propName + "]");

            NodeList varList = prop.getChildNodes();
            if (varList == null || varList.getLength() == 0) {
                Object _v = inTag(prop);
                if (_v == null)
                    throw new IllegalArgumentException("No value for property["
                            + propName + "] in bean [" + beanObj
                            + "]");
                else
                    BeanEditor.setProperty(beanObj, propName, _v );

            } else {// properties outside

                // check if is list, map or single value
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
                    Properties properties = buildMap(iter, propName);
                    BeanEditor.setProperty(beanObj, propName, properties);

                } else { // single value or list

                    // second, use this list
                    if (isList) {
                        // set list or array
                        iter = iter.getFirstChild().getNextSibling();
                        List<Object> beanList = buildList(iter);
                        BeanEditor.setListProperty(beanObj, propName, beanList);

                    } else {
                        List<Object> beanList = buildList(iter);
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

        String afterCall = configCenter.replaceIfPresent(
                beanElem.getAttribute(XMLTags.BEAN_AFTER_CALL));

        afterProcess(beanObj, afterCall);
        return beanObj;
    }

    private Object inTag(Element prop) throws Exception {

        String varObj = configCenter.replaceIfPresent(
                prop.getAttribute(XMLTags.PROPERTY_INSTANCE));

        String varStr = configCenter.replaceIfPresent(
                prop.getAttribute(XMLTags.PROPERTY_VALUE));

        String varRef = configCenter.replaceIfPresent(
                prop.getAttribute(XMLTags.PROPERTY_REF));

        if (Str.Utils.notBlank(varStr)) {
            // e.g., <property id="number" value="5"/>
            // str value will casted to param type if needed
            String type;
            if (null != (type = prop.getAttribute(XMLTags.TYPE))) {
                type = configCenter.replaceIfPresent(type.trim());
                return Converter.slient.translateStr(varStr, Converter.getClass(type));
            } else
                return varStr;

        } else if (Str.Utils.notBlank(varRef)) {
            // e.g., <property id="bean" ref="someOtherBean"/>
            Object ref = getBean(varRef.trim());
            return ref;

        } else if (Str.Utils.notBlank(varObj)) {
            // e.g. <property id="injector" instance="com.caibowen.gplume.core.Injector"/>
            Class<?> klass = this.classLoader.loadClass(varObj);
            Object obj = construct(klass, null);
            afterProcess(obj, null);
            return obj;

        } else {
            return null;
        }
    }


    private @Nonnull
    Properties buildMap(Node iter, String propName) {
        Properties properties = new Properties();
        while (iter != null) {
            Element elemBn;
            if (iter.getNodeType() == Node.ELEMENT_NODE)
                elemBn = (Element)iter;
            else
                continue;

            String mapK = configCenter.replaceIfPresent(elemBn.getTagName().trim());
            if (properties.containsKey(mapK))
                throw new IllegalArgumentException(
                        "duplicated map key for property[" + propName + "]");

            Object mapV = configCenter.replaceIfPresent(
                    elemBn.getTextContent());

            String tgtType;
            if (null != (tgtType = elemBn.getAttribute(XMLTags.VALUE_TYPE))) {
                Class k = Converter.getClass(configCenter.replaceIfPresent(tgtType.trim()));
                mapV = Converter.slient.translateStr((String)mapV, k);
            }
            properties.put(mapK, mapV);
            // skip node of #text
            iter = iter.getNextSibling().getNextSibling();
        }
        return properties;
    }

    /**
     *
     * <list>
     *     <bean></bean>
     *     <value></value>
     *     <ref></ref>
     * </list>
     *
     * @param iter
     * @return
     * @throws Exception
     */
    private @Nonnull List<Object> buildList(Node iter) throws Exception {
        List<Object> beanList = new ArrayList<>(16);
        while (iter != null && iter.getNodeType() == Node.ELEMENT_NODE) {

            Element elemBn = (Element) iter;

            if (XMLTags.BEAN.equals(elemBn.getNodeName())) {
                beanList.add(buildBean(elemBn));

            } else if (XMLTags.PROPERTY_REF.equals(elemBn.getNodeName())) {
                beanList.add(getBean(
                                configCenter.replaceIfPresent(
                                        elemBn.getTextContent())
                        )
                );

            } else if (XMLTags.PROPERTY_VALUE.equals(elemBn.getNodeName())) {
                beanList.add(configCenter.replaceIfPresent(
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
        return beanList;
    }

    protected void afterProcess(Object bean, String initName) throws Exception {
        if (bean instanceof BeanClassLoaderAware) {
            ((BeanClassLoaderAware)bean).setBeanClassLoader(this.classLoader);
            LOG.info(
                    "classLoader aware  bean ["
                            + bean.getClass().getSimpleName()
                            + "] set classLoader [" + this.classLoader);
        }

        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
            LOG.info(
                    "bean [" + bean.getClass().getSimpleName()
                            + "] initialized");
        }


        if (Str.Utils.isBlank(initName))
            return;

        Method m = bean.getClass().getDeclaredMethod(initName.trim());
        if (!m.isAccessible())
            m.setAccessible(true);
        if (Modifier.isStatic(m.getModifiers()))
            m.invoke(null);
        else
            m.invoke(bean);
    }

    protected Object construct(@Nonnull Class klass,
                               @Nullable Element prop) throws Exception {

        Object val = null;
        if (prop == null) {
            Constructor<?> ctor = klass.getDeclaredConstructor();
            if (!ctor.isAccessible()) {
                ctor.setAccessible(true);
            }
            val = ctor.newInstance();

        } else {
            Object inTag;
            if (null != (inTag = inTag(prop))) {
                val = BeanEditor.construct(klass, inTag);

            } else if (prop.getNodeName().equals(XMLTags.PROPERTY_MAP)) {
                prop = (Element) prop.getFirstChild().getNextSibling();
                Map m = buildMap(prop, "Constructor:" + klass.getName());
                val = BeanEditor.construct(klass, m);

            } else {
                if (prop.getNodeName().equals(XMLTags.PROPERTY_LIST)) {
                    prop = (Element) prop.getFirstChild().getNextSibling();
                    List ls = buildList(prop);
                    val = BeanEditor.construct(klass, ls);

                } else {
                    List ls = buildList(prop);
                    if (ls.size() == 1)
                        val = BeanEditor.construct(klass, ls.get(0));
                    else
                        throw new IllegalArgumentException(
                                "Bean number miss match , construct ["
                                        + "] in class ["
                                        + klass.getName() + "]"
                                        + "needs 1 actual " + ls.size());
                }
            }
        }
        return val;
    }
}
