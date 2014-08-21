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

package com.caibowen.gplume.webex.json;

import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

    ObjectMapper mapper = new ObjectMapper();


    @Override
    public int fitness(Class klass) {
        return JsonResult.class.equals(klass) ? 1
                : Klass.isAssignable(klass, JsonResult.class) ? 0 : -1;
    }

    @Override
    public void resolve(RequestContext ctx, Object view) throws Exception {

        JsonResult jr = (JsonResult) view;
        JsonEncoding encoding = JsonEncoding.UTF8;

        JsonGenerator jsonGenerator =
                this.mapper.getFactory().createGenerator(
                        ctx.response.getOutputStream(),
                        encoding);

        if (this.mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
            jsonGenerator.useDefaultPrettyPrinter();
        }
        if (this.jsonPrefix != null) {
            jsonGenerator.writeRaw(this.jsonPrefix);
        }
        this.mapper.writeValue(jsonGenerator, jr.data);
    }

    public void setDoPrettyPrint(Boolean doPrettyPrint) {
        this.doPrettyPrint = doPrettyPrint;
        this.mapper.configure(SerializationFeature.INDENT_OUTPUT, this.doPrettyPrint);
    }

    public void setJsonPrefix(String jsonPrefix) {
        this.jsonPrefix = jsonPrefix;
    }
}
