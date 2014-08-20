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
import com.caibowen.gplume.web.RequestContext;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.inject.Inject;

/**
 *
 * @author bowen.cbw
 * @since 8/20/2014.
 */
public class JsonView implements IView {

    // inject by json resolver
    @Inject ObjectMapper mapper;
    @Inject String prefix;

    // TODO get encoding from response !!!
    JsonEncoding encoding;

    Object val;

    @Override
    public void resolve(RequestContext context) throws Exception {

        JsonGenerator jsonGenerator =
                this.mapper.getFactory().createJsonGenerator(
                        context.response.getOutputStream(),
                        encoding);

        if (this.mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
            jsonGenerator.useDefaultPrettyPrinter();
        }

        if (this.prefix != null) {
            jsonGenerator.writeRaw(this.prefix);
        }
        this.mapper.writeValue(jsonGenerator, val);
    }

}
