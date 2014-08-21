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

import java.io.PrintWriter;

/**
 * @author bowen.cbw
 * @since 8/21/2014.
 */
public class TextViewResolver implements IViewResolver {

    @Override
    public int fitness(Class klass) {
        return klass == TextView.class ? 1 : -1;
    }

    @Override
    public void resolve(RequestContext ctx, Object view) throws Exception {
        TextView tv = (TextView)view;
        try {
            ctx.response.setContentType(PageAttributes.Type.TEXT);
            ctx.response.setCharacterEncoding(tv.encoding);
            PrintWriter writer = ctx.response.getWriter();
            writer.write(tv.content);

        } catch (Exception e) {
            throw new RuntimeException("Error writing JSP", e);
        }
    }
}
