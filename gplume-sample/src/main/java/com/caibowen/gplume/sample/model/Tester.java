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

package com.caibowen.gplume.sample.model;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Auther bowen.cbw
 * @since 8/16/2014.
 */
public class Tester {

    Tester(String s) {
        System.out.println("constructing Tester str : " + s);
    }
    Tester(Integer s) {
        System.out.println("constructing Tester int : " + s);
    }

    Tester(Object s) {
        System.out.println("constructing Tester Object : " + s);
    }

    Tester(Properties p) {
        System.out.println("constructing Tester map : " + p);
    }

    Tester(List s) {
        System.out.println("constructing Tester list : " + s);
    }

    void init() {
        System.out.println(" Tester after property set ");
    }


    public void setMap(Map<Object, Object> m) {
        for (Map.Entry<Object, Object> e : m.entrySet()) {
            System.out.println(e.getKey() + "   " + e.getValue().getClass().getSimpleName() + "  " + e.getValue());
        }
    }
}
