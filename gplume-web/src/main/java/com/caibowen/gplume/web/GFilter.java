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
package com.caibowen.gplume.web;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caibowen.gplume.context.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * dispatch request.
 * 
 * Note that all other filters must be declared ahead of GFilter in web.xml
 * Because the filter chain will not continue after this one
 * 
 * @author BowenCai
 *
 */
public class GFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(GFilter.class);

	/**
	 *  required component
	 */
	private BaseControlCenter controlCenter;

	
	@Override
	public void init(FilterConfig fconfig) throws ServletException {

		Set ctrols = AppContext.beanAssembler.getBeans(BaseControlCenter.class);
		if (ctrols.size() != 1)
			throw new IllegalArgumentException("wrong number of control center, expect 1, find [" + ctrols.size() + "]");
		controlCenter = (BaseControlCenter)ctrols.iterator().next();
		try {
			controlCenter.init(fconfig.getServletContext());
		} catch (Throwable e) {
            LOG.error("error construct control center", e);
		}
	}

	
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		
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
