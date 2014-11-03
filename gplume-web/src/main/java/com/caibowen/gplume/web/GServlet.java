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

package com.caibowen.gplume.web;

import com.caibowen.gplume.context.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * alternative to GFilter, aim at server before Servlet 2.3
 * @author BowenCai
 */
public class GServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(GServlet.class);

    /**
     * necessary component
     */
    private AbstractControlCenter controlCenter;

    @Override
    public void init(ServletConfig config) throws ServletException {
        controlCenter = AppContext.beanAssembler.getBean("controlCenter");

        try {

            controlCenter.init(config.getServletContext());

        } catch (Throwable e) {
            LOG.error("could not construct control center", e);
        }

        super.init(config);
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws IOException, ServletException {

        controlCenter.service(request, response);
    }

    @Override
    public void destroy() {
        try {
            controlCenter.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        controlCenter = null;
    }
}
