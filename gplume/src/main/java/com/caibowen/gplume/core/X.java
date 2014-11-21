package com.caibowen.gplume.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author BowenCai
 * @since 13-11-2014.
 */
public class X implements InvocationHandler {

    private Object actor;
    private HashSet<String> methodNames = new HashSet<>(64);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret;
        if (methodNames.contains(method.getName())) {

            ret = method.invoke(actor, args);

        } else {
            ret = method.invoke(actor, args);
        }

        return ret;
    }

    public Object getActor() {
        return actor;
    }

    public void setActor(Object actor) {
        this.actor = actor;
    }
}
