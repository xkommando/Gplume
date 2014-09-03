/*******************************************************************************
 * Copyright 2014 Bowen Cai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.caibowen.gplume.web.misc;

import com.caibowen.gplume.context.InputStreamProvider;

import java.io.InputStream;
import java.io.Serializable;

import javax.servlet.ServletContext;



/**
 *
 * @author BowenCai
 *
 */
public class ServletContextInputStreamProvider implements InputStreamProvider, Serializable {

    private static final long serialVersionUID = -3973425249230485036L;
    ServletContext context;

    public ServletContextInputStreamProvider(ServletContext context) {
        this.context = context;
    }

    @Override
    public InputStream getStream(String path) {
        return context.getResourceAsStream(path);
    }

    @Override
    public String getRealPath(String p) {
        return context.getRealPath(p);
    }

}