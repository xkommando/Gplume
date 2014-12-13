package com.caibowen.gplume.context;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author BowenCai
 * @since 21/11/2014.
 */
class ProxyBean implements InvocationHandler {

    Class<?> targetClass;
    Object realBean;
    ProxyBean(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.realBean = null;
    }

    boolean inited() {
        return realBean != null;
    }

    void init(@Nonnull Object rb) {
        this.realBean = rb;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(realBean, args);
    }
}
