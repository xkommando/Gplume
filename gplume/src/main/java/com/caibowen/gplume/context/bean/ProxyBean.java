package com.caibowen.gplume.context.bean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author BowenCai
 * @since 21/11/2014.
 */
class ProxyBean implements InvocationHandler {

    Object realBean;

    boolean inited() {
        return realBean != null;
    }

    void init(Object rb) {
        this.realBean = rb;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(realBean, args);
    }
}