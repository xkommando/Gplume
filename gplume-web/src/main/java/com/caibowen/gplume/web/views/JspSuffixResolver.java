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

package com.caibowen.gplume.web.views;

import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;

import java.io.Serializable;

/**
 *  add a suffix to the jsp name to make a complete path
 *
 * @author BowenCai
 *
*/
public class JspSuffixResolver implements IViewResolver, Serializable {

    private static final long serialVersionUID = 6433472763677545676L;

    public final String suffix;

    public JspSuffixResolver(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public int fitness(Class val) {
        return val == String.class ? 1 : -1;
    }

    @Override
    public void resolve(RequestContext ctx, Object ret) throws Exception {
        ctx.render((String)ret + suffix);
    }
}
