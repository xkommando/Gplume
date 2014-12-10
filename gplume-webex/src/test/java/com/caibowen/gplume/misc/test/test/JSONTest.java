package com.caibowen.gplume.misc.test.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author BowenCai
 * @since 10/12/2014.
 */
public class JSONTest {

    @Test
    public void parse() {
        Object obj = (JSON.parseArray("[\n" +
                "\t\t\"com.caibowen.gplume.context.bean.Injector haha zh_CN\",\n" +
                "\t\t{\"k1\":\"v111111v\",\"k2\":\"v222222v\"},\n" +
                "\t\t\"bbbbbbbbb2222sdfgdg\",\n" +
                "\t\t{\"k1\":\"v111111\",\"k2\":\"v2222\"}\n" +
                "\t]", ArrayList.class));
        System.out.println(obj.getClass());
        System.out.println(obj);
    }
}
