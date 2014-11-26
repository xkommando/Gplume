package com.caibowen.gplume.context.bean;

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

    public ProxyBean(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    boolean inited() {
        return realBean != null;
    }

    void init(@Nonnull Object rb) {
        this.realBean = rb;
//        targetClass = rb.getClass();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(realBean, args);
    }
}
