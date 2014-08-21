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

package com.caibowen.gplume.web.actions.builder;

import com.caibowen.gplume.web.IViewResolver;

import java.util.*;

/**
 * @author bowen.cbw
 * @since 8/21/2014.
 */
public class ViewMatcher {

    private List<IViewResolver> resolvers;

    public void setResolvers(List<IViewResolver> rs) {
        this.resolvers = rs;
    }

    private Map<Class, IViewResolver> vMap = new HashMap<>(32);

    public IViewResolver findMatch(Class retKlass) {
        if (vMap.containsKey(retKlass))
            return vMap.get(retKlass);

        TreeMap<Integer, List<IViewResolver>> candidate = new TreeMap<>();
        candidate.put(Integer.MIN_VALUE, null);
        for (IViewResolver r : resolvers) {
            int v = r.fitness(retKlass);
            if (v > -1 && v > candidate.firstKey()) {
                List<IViewResolver> rs = candidate.get(v);
                if (rs == null) {
                    rs = new ArrayList<>(6);
                    candidate.put(v, rs);
                }
                rs.add(r);
            }
        }

        List<IViewResolver> best = candidate.lastEntry().getValue();
        if (best == null || best.isEmpty() || candidate.lastEntry().getKey() < 0)
            throw new IllegalStateException("cannot find view resolver for [" + retKlass + "]");
        if (best.size() > 1) {
            StringBuilder sb = new StringBuilder(256);
            sb.append("multi match view resolver for class [" + retKlass + "], they are:\r\n");
            for (IViewResolver _r : best)
                sb.append(_r).append("\r\n");
            throw new IllegalStateException(sb.toString());
        }
        return best.get(0);
    }

}
