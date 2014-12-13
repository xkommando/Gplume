package com.caibowen.gplume.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BowenCai
 * @since 12/12/2014.
 */
public interface IBeanBuilder {

    static final Logger LOG = LoggerFactory.getLogger(DefaultBeanBuilder.class);


    void beforeProcess(Object bean, @Nullable String beanID) throws Exception;

    @Nonnull
    Object buildBean(Element beanElem, @Nullable String beanID) throws Exception;


    void afterProcess(Object bean, @Nullable Element beanElem) throws Exception;

    ClassLoader getClassLoader();

    void setClassLoader(ClassLoader classLoader);

    ConfigCenter getConfigCenter();

    void setConfigCenter(ConfigCenter configCenter);

    IBeanAssembler getAssembler();

    void setAssembler(IBeanAssembler assembler);
}
