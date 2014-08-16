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

import com.caibowen.gplume.web.RequestContext;

/**
 *
 * add a prefix and a suffix to the jsp name to make a complete path
 *
 * @author BowenCai
 *
*/
public class JspPrefixSuffixResolver extends JspSuffixResolver {

    private static final long serialVersionUID = 4685236015290815491L;

    public final String prefix;

    public JspPrefixSuffixResolver(String prefix, String suffix) {
        super(suffix);
        this.prefix = prefix;
    }

    @Override
    public void resolve(RequestContext ctx, String ret) {
        ctx.render(prefix + ret + suffix);
    }
}