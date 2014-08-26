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

package com.caibowen.gplume.webex.test;

import com.caibowen.gplume.common.ImmutableArrayMap;
import com.caibowen.gplume.webex.json.Jsons;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Auther bowen.cbw
 * @since 8/20/2014.
 */
public class JacksonTest {


    @Test
    public void t1() {
//        for (JsonEncoding e : JsonEncoding.values())
//            System.out.println(e.getJavaName());
//
//        Charset s = Charset.defaultCharset();
//        System.out.println(s.name());

        ImmutableArrayMap map = new ImmutableArrayMap(new Object[][]{
                {"key1", "val1"},
                {"key2", 456},
                {"789", "val3"}
        });
        String js = Jsons.serial(map);
        Map m = Jsons.deserial(js, HashMap.class);
        ImmutableArrayMap mm = new ImmutableArrayMap(m);
        System.out.println(js);
        System.out.println(mm.toJson());
        System.out.println(mm);
    }
}
