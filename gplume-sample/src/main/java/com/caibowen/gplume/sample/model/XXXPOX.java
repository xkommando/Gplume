package com.caibowen.gplume.sample.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author BowenCai
 * @since 7-11-2014.
 */
public class XXXPOX implements InvocationHandler {

    final int i;
    String name;

    public XXXPOX(int i) {
        this.i = i;
    }

    public XXXPOX(String s, int t) {
        this.name = s;
        this.i = t;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().endsWith("bark")) {
            System.out.println(name + "   pox barkkkk " + i);
            return null;
        } else if (method.getName().endsWith("setName")) {
            name = (String)args[0];
            return null;
        } else if (method.getName().endsWith("toString")) {
            return "pppproxy";
        } else if (method.getName().endsWith("equals")) {
            return false;
        } else if (method.getName().endsWith("hashCode")) {
            return -1;
        }
        return method.invoke(proxy, args);
    }

}
