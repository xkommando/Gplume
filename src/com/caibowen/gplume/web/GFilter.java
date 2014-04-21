/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caibowen.gplume.core.context.AppContext;


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

	/**
	 * necessary component
	 */
	private AbstractControlCenter controlCenter;

	
	@Override
	public void init(FilterConfig arg0) throws ServletException {

		controlCenter = AppContext.beanAssembler.getBean("controlCenter");
		
		try {
			
			controlCenter.init(arg0.getServletContext());

		} catch (Throwable e) {
			// no exception will be thrown at production,
			//so remove this line after test!

			// if debug, print it
			e.printStackTrace();
			throw new RuntimeException("error init control center", e);
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
		controlCenter.destroy();
		controlCenter = null;
	}

}
