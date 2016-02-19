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

package com.caibowen.gplume.context;

import com.caibowen.gplume.annotation.Internal;
import com.caibowen.gplume.context.bean.AssemblerAwareBean;
import com.caibowen.gplume.context.bean.ClassLoaderAwareBean;
import com.caibowen.gplume.context.bean.IDAwareBean;
import com.caibowen.gplume.context.bean.InitializingBean;
import com.caibowen.gplume.core.BeanEditor;
import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.caibowen.gplume.misc.Str.Utils.isBlank;
import static com.caibowen.gplume.misc.Str.Utils.notBlank;

/**
 *  auxiliary for xml parsing
 *
 * @author bowen.cbw
 * @since 8/16/2014.
 */
@Internal
public class DefaultBeanBuilder implements IBeanBuilder {

    private ClassLoader classLoader;

    private ConfigCenter configCenter;
    private IBeanAssembler assembler;

    protected @Nonnull Class<?> getClass(String name) throws Exception {
        return classLoader.loadClass(name);
    }

    protected @Nonnull Class<?> getClass(Element element) throws Exception {
        String clazzName = element.getAttribute(XMLTags.BEAN_CLASS);
        if (Str.Utils.isBlank(clazzName)) {
            throw new IllegalArgumentException("Empty class name at [" + element.toString() + "]");
        }
        clazzName = configCenter.replaceIfPresent(clazzName.trim());
        return getClass(clazzName);
    }

    /**
     * For one field, there are 3 notation:
     *
     * 1. <field name="Xyz"><val="Xyz"/></field>
     *
     * 2. <field name="Xyz" val="Xyz"/>
     *
     * 3. no property is needed.
     */
    @Override
    @Nonnull public
    Object buildBean(Element beanElem, @Nullable String beanID) throws Exception {

        Class<?> bnClass = getClass(beanElem);
        Object beanObj = construct(bnClass, beanElem);

        LOG.debug("bean class[{}] created", bnClass.getName());

        beforeProcess(beanObj, beanID);
        NodeList _propLs = beanElem.getElementsByTagName(XMLTags.BEAN_PROP);
        if (_propLs == null || _propLs.getLength() == 0) {
            // no property
            afterProcess(beanObj, null);
            return beanObj;
        }

        // get top level properties only, skip properties in sub beans
        Node next = beanElem.getFirstChild();
        while (next.getNextSibling() != null) {
            Element prop;
            next = next.getNextSibling();

            if (next.getNodeType() == Node.ELEMENT_NODE
                    && next.getNodeName().equals(XMLTags.BEAN_PROP)) {
                prop = (Element) next;
            } else  continue;

            String propName = prop.getAttribute(XMLTags.PROP_NAME);
            propName = configCenter.replaceIfPresent(propName.trim());
            if (!notBlank(propName))
                throw new NullPointerException(
                        "Property name is empty. NodeName: ["
                                + prop.getNodeName() + "]");

//System.out.println("class [" + bnClass.getSimpleName() + "] prop[" + propName + "]");

            NodeList varList = prop.getChildNodes();
            if (varList == null || varList.getLength() == 0) {
                Object _v = inTag(prop, true, Klass.findType(beanObj.getClass(), propName));
                BeanEditor.setProperty(beanObj, propName, _v);
                continue;
            }

            // properties outside

            // check if is list, map, set or single value
            // 1 -> single, 2 -> list, 3 -> set, 4 -> map
            int container = 0;
            /**
             *
             * prop
             * |
             * first child -> next
             * 					|
             * 				    <list> or <value>
             */
            Node iter = prop.getFirstChild().getNextSibling();
            if (iter == null)
                continue;

            if (XMLTags.PROP_LIST.equals(iter.getNodeName()))
                container = 2;
            else if (XMLTags.PROP_SET.equals(iter.getNodeName()))
                container = 3;
            else if (XMLTags.PROP_MAP.equals(iter.getNodeName()))
                container = 4;

            switch (container) {
                case 2:
                    // set list
                    iter = iter.getFirstChild().getNextSibling();
                    List<Object> beanList1 = buildList(iter);
                    BeanEditor.setListProperty(beanObj, propName, beanList1);
                    break;
                case 3: // set
                    iter = iter.getFirstChild().getNextSibling();
                    List<Object> beanList2 = buildList(iter);
                    BeanEditor.setSetProperty(beanObj, propName, beanList2);
                    break;
                case 4:
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
                    break;
                default:
                    List<Object> beanList3 = buildList(iter);
                    if (beanList3.size() == 1) {
                        BeanEditor.setProperty(beanObj, propName, beanList3.get(0));
                    } else {
                        throw new IllegalArgumentException(
                                "Bean number miss match for property[" + propName
                                        + "] in class ["
                                        + bnClass.getName() + "]\n"
                                        + "needs 1 actual " + beanList3.size()
                                        + "beans : " + beanList3.toString());
                    }
            }

        } // for properties
        return beanObj;
    }

    @Override
    public void beforeProcess(Object bean, @Nullable String beanID) throws Exception {
        if (bean instanceof ClassLoaderAwareBean) {
            ((ClassLoaderAwareBean)bean).setBeanClassLoader(this.classLoader);
            LOG.debug(
                    "classLoader aware  bean ["
                            + bean.getClass().getSimpleName()
                            + "] set classLoader [" + this.classLoader);
        }
        if (bean instanceof IDAwareBean) {
            ((IDAwareBean)bean).setBeanID(beanID);
            LOG.debug(
                    "classLoader aware  bean ["
                            + bean.getClass().getSimpleName()
                            + "] setting classLoader [" + this.classLoader);
        }
        if (bean instanceof AssemblerAwareBean) {
            ((AssemblerAwareBean)bean).setAssembler(assembler);
            LOG.debug(
                    "AssemblerAwareBean bean ["
                            + bean.getClass().getSimpleName()
                            + "] setting assembler [" + this.classLoader);
        }

    }

    @Override
    public void afterProcess(Object bean, @Nullable Element beanElem) throws Exception {

        Object realBean = Proxy.isProxyClass(bean.getClass()) ?
                Proxy.getInvocationHandler(bean) : bean;

        if (realBean instanceof InitializingBean) {
            ((InitializingBean) realBean).afterPropertiesSet();
            LOG.debug(
                    "bean [" + realBean.getClass().getSimpleName()
                            + "] initialized");
        }

        if (beanElem == null)
            return;

        String initName = configCenter.replaceIfPresent(
                beanElem.getAttribute(XMLTags.BEAN_AFTER_CALL));
        if (isBlank(initName))
            return;

        Method m = realBean.getClass().getMethod(initName.trim());
        if (!m.isAccessible())
            m.setAccessible(true);
        if (Modifier.isStatic(m.getModifiers()))
            m.invoke(null);
        else
            m.invoke(realBean);
    }

    protected Object inTag(Element prop, boolean notNull, @Nullable Class<?> tgtClass) throws Exception {

        String varInstance = prop.getAttribute(XMLTags.PROP_INSTANCE);
        String varStr = prop.getAttribute(XMLTags.PROP_VALUE);
        String varRef = prop.getAttribute(XMLTags.PROP_REF);

        if (notBlank(varStr)) {
            // e.g., <property id="number" value="5"/>
            // str value will casted to param type if needed
            String type = prop.getAttribute(XMLTags.TYPE);
            if (notBlank(type)) {
                type = configCenter.replaceIfPresent(type.trim());
                Object ret = Converter.slient.translateStr(varStr, Converter.getClass(type));
                if (notNull && ret == null)
                    throw new IllegalArgumentException("Could not cast ["
                            + varStr + "] to type[" + type + "]");

                return ret;
            } else
                return varStr;

        } else if (notBlank(varRef)) {
            // e.g., <property id="bean" ref="someOtherBean"/>
            String _name = configCenter.replaceIfPresent(varRef.trim());
            Object bn = assembler.getBean(_name);
            if (bn != null)
                return bn;

            if (tgtClass == null) throw new NullPointerException();
            return assembler.getForBuild(_name, true, tgtClass);

        } else if (notBlank(varInstance)) {
            // e.g. <property id="injector" instance="com.caibowen.gplume.context.bean.Injector"/>
            Class<?> klass = this.classLoader.loadClass(configCenter.replaceIfPresent(varInstance));
            Object obj = klass.newInstance();
            String propName = prop.getAttribute(XMLTags.PROP_NAME);
            beforeProcess(obj, propName);
            afterProcess(obj, null);
            return obj;

        } else if (notNull)
            throw new IllegalStateException("Could not find required bean from XML element[" + prop + "]");
        else
            return null;
    }


    protected  @Nonnull
    Properties buildMap(Node iter, String propName) {
        Properties properties = new Properties();
        while (iter != null) {
            Element elemBn;
            if (iter.getNodeType() == Node.ELEMENT_NODE)
                elemBn = (Element)iter;
            else continue;

            String mapK = configCenter.replaceIfPresent(elemBn.getTagName().trim());
            if (properties.containsKey(mapK))
                throw new IllegalArgumentException(
                        "duplicated map key for property[" + propName + "]");

            String _v = elemBn.getTextContent();
            Assert.hasText(_v);
            Object mapV = configCenter.replaceIfPresent(_v);

            String tgtType;
            if (notBlank(tgtType = elemBn.getAttribute(XMLTags.TYPE))) {
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
    protected @Nonnull List<Object> buildList(Node iter) throws Exception {
        List<Object> beanList = new ArrayList<>(16);

        while (iter != null && iter.getNodeType() == Node.ELEMENT_NODE) {
            Element elemBn = (Element) iter;
            String tagType = elemBn.getNodeName();

            switch (tagType) {
                case XMLTags.BEAN:
                    beanList.add(buildBean(elemBn, null));
                    break;
                case XMLTags.PROP_REF:
                    String _s = elemBn.getTextContent();
                    if (isBlank(_s))
                        throw new IllegalArgumentException("Empty Reference");

                    beanList.add(assembler.getBean(
                                    configCenter.replaceIfPresent(
                                            _s.trim())
                            )
                    );
                    break;
                case XMLTags.PROP_VALUE:
                    String lit = configCenter.replaceIfPresent(
                            elemBn.getTextContent().trim());

                    String tgtType;
                    if (notBlank(tgtType = elemBn.getAttribute(XMLTags.TYPE))) {
                        Class k = Converter.getClass(configCenter.replaceIfPresent(tgtType.trim()));
                        beanList.add(
                                Converter.slient.translateStr(lit, k));
                    } else beanList.add(lit);
                    break;
                case XMLTags.PROP_LIST:
                    Element _prop = (Element)elemBn.getFirstChild().getNextSibling();
                    beanList.add(buildList(_prop));
                    break;
                case XMLTags.PROP_MAP:
                    Element _propM = (Element)elemBn.getFirstChild().getNextSibling();
                    Map m = buildMap(_propM, "Constructor:");
                    beanList.add(m);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown property["
                            + iter.getNodeName() + "]");
            }
            // skip node of #text
            iter = iter.getNextSibling().getNextSibling();
        }// while

        return beanList;
    }


    /**
     * if <construct /> exists :
     *  if has content , move to content
     *  else return original position
     *
     * @param beanElem
     * @return
     */
    private static  @Nullable Element
    findCtorElem(Element beanElem) {
        Element ctorElem = null;
        Node _n = beanElem.getFirstChild();
        while (_n != null && _n.getNextSibling() != null) {
            _n = _n.getNextSibling();
            if (_n.getNodeName().equals(XMLTags.BEAN_CONSTRUCT)) {
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
     * build new bean, if proxy specified,
     * <construct></construct> is used for instantiating the invocation handler
     * @param klass
     * @param beanElem
     * @return
     * @throws Exception
     */
    protected Object construct(@Nonnull Class klass,
                               @Nonnull Element beanElem) throws Exception {

        int _c = 0;
        String poxVal = beanElem.getAttribute(XMLTags.BEAN_PROXY);
        String poxRef = beanElem.getAttribute(XMLTags.PROP_REF);
        _c += isBlank(poxVal) ? 0 : 1;
        _c += isBlank(poxRef) ? 0 : 3;
        switch (_c) {
            case 0 : return newInstance(klass, findCtorElem(beanElem));
            case 1 :
                String poxHM = configCenter.replaceIfPresent(poxVal.trim());
                Class poxHandle = getClass(poxHM);
                if (! InvocationHandler.class.isAssignableFrom(poxHandle))
                    throw new IllegalArgumentException("[" + poxHM + "] from [" + poxVal + "]is not a InvocationHandler");
                // xml construct invocation handler
                InvocationHandler handler = (InvocationHandler)newInstance(poxHandle, findCtorElem(beanElem));
                return Proxy.newProxyInstance(classLoader, new Class[]{klass}, handler);
            case 3:
                return assembler.getForBuild(configCenter.replaceIfPresent(poxRef.trim()), true, klass);
            default:
                throw new IllegalArgumentException("wrong config of InvocationHandler");
        }
    }

    protected Object newInstance(@Nonnull Class klass,
                               @Nullable Element prop) throws Exception {

        if (prop == null) {
            try {
                Constructor<?> ctor = klass.getDeclaredConstructor();
                if (!ctor.isAccessible()) {
                    ctor.setAccessible(true);
                }
                return ctor.newInstance();
            } catch (Exception e) {
                throw new BeanAssemblingException("Could not find default constructor");
            }
        }
        // 2 try in tag
        Object _tagVal = null;
        try {
            _tagVal = inTag(prop, false, null);
        } catch (NullPointerException e) {
            // referred bean not available yet
            List<Class> cs = Klass.findCtorParam(klass);
            if (cs.size() != 0) {
                throw new IllegalStateException("Could not determine constructor parameter for [" + klass + "]");
            }
            _tagVal = inTag(prop, false, cs.get(0));
        }

        if (null != _tagVal)
            return BeanEditor.construct(klass, _tagVal);

        // 3 try parse xml
        if (prop.getNodeName().equals(XMLTags.PROP_MAP)) {
            prop = (Element) prop.getFirstChild().getNextSibling();
            Map m = buildMap(prop, "Constructor:" + klass.getName());
            return BeanEditor.construct(klass, m);

        } else if (prop.getNodeName().equals(XMLTags.PROP_LIST)) {
            prop = (Element) prop.getFirstChild().getNextSibling();
            List ls = buildList(prop);
            return BeanEditor.construct(klass, ls);
        }

        List ls = buildList(prop);
        return BeanEditor.construct(klass, ls.toArray());
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ConfigCenter getConfigCenter() {
        return configCenter;
    }

    @Override
    public void setConfigCenter(ConfigCenter configCenter) {
        this.configCenter = configCenter;
    }

    @Override
    public IBeanAssembler getAssembler() {
        return assembler;
    }

    @Override
    public void setAssembler(IBeanAssembler assembler) {
        this.assembler = assembler;
    }
}
