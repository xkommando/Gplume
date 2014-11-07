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

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @Auther bowen.cbw
 * @since 8/20/2014.
 */
public class JacksonTest {


    @Test
    public void t0() {
        System.out.println(Integer.MAX_VALUE / 1000 / 3600);
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.array();
    }

//    @Test
    public void t1() throws Throwable {
//        for (JsonEncoding e : JsonEncoding.values())
//            System.out.println(e.getJavaName());
//
//        Charset s = Charset.defaultCharset();
//        System.out.println(s.name());

//        ImmutableArrayMap map = new ImmutableArrayMap(new Object[][]{
//                {"key1", "val1"},
//                {"key2", 456},
//                {"789", "val3"}
//        });
//        String js = Jsons.serial(map);
//        Map m = Jsons.deserial(js, HashMap.class);
//        ImmutableArrayMap mm = new ImmutableArrayMap(m);
//        System.out.println(js);
//        System.out.println(mm.toJson());
//        System.out.println(mm);
    }
}
