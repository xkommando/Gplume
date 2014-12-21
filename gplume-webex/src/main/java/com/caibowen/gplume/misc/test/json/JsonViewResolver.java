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

package com.caibowen.gplume.misc.test.json;

import com.alibaba.fastjson.JSON;
import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;

import javax.inject.Inject;

/**
 * @author bowen.cbw
 * @since 8/20/2014.
 */
public class JsonViewResolver implements IViewResolver {

    @Inject
    private String jsonPrefix;
    @Inject
    private Boolean doPrettyPrint = Boolean.TRUE;

    @Override
    public int fitness(Class klass) {
        return JsonResult.class.equals(klass) ? 1
                : Klass.isAssignable(klass, JsonResult.class) ? 0 : -1;
    }

    @Override
    public void resolve(RequestContext ctx, Object view) throws Exception {

        JsonResult jr = (JsonResult) view;
        JSON.toJSONString(jr.data, doPrettyPrint);
//        JsonEncoding encoding = JsonEncoding.UTF8;
//
//        JsonGenerator jsonGenerator =
//                this.mapper.getFactory().createGenerator(
//                        ctx.response.getOutputStream(),
//                        encoding);
//
//        if (this.mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
//            jsonGenerator.useDefaultPrettyPrinter();
//        }
//        if (this.jsonPrefix != null) {
//            jsonGenerator.writeRaw(this.jsonPrefix);
//        }
//        this.mapper.writeValue(jsonGenerator, jr.data);
    }

    public void setDoPrettyPrint(Boolean doPrettyPrint) {
        this.doPrettyPrint = doPrettyPrint;
    }

    public void setJsonPrefix(String jsonPrefix) {
        this.jsonPrefix = jsonPrefix;
    }
}
