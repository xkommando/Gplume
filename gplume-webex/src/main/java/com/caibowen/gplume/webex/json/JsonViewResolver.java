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

import com.caibowen.gplume.web.IView;
import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.inject.Inject;

/**
 *
 * @author bowen.cbw
 * @since 8/20/2014.
 */
public class JsonViewResolver implements IViewResolver {

    @Inject private String jsonPrefix;
    @Inject private Boolean doPrettyPrint = Boolean.TRUE;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void resolve(RequestContext ctx, IView view) throws Exception {
        JsonView v = (JsonView)view;
        v.mapper = this.mapper;
        v.prefix = this.jsonPrefix;
        v.resolve(ctx);
    }

    public void setDoPrettyPrint(Boolean doPrettyPrint) {
        this.doPrettyPrint = doPrettyPrint;
        this.mapper.configure(SerializationFeature.INDENT_OUTPUT, this.doPrettyPrint);
    }

    public void setJsonPrefix(String jsonPrefix) {
        this.jsonPrefix = jsonPrefix;
    }
}
