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

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author BowenCai
 *
 */
public class DebuggingFilter implements Filter  {

	AtomicLong count;
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		count = new AtomicLong(0L);
	}


	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;

		System.out.format("\n-------------- %d. %s, time%s --------------\n"
				,count.addAndGet(1L)
				,(arg1.isCommitted() ? "committed" : "uncommitted")
				,new Date().toString().substring(10, 20));
		
		System.out.format("Method[%s]  URI[%s]\nParam[%s]\n",

				request.getMethod(),
				request.getRequestURI(), request.getQueryString());

//		System.out.format("Type[%s]  Encoding[%s]  Locale[%s]\n",
//				request.getContentType(), 
//				request.getCharacterEncoding(),
//				request.getLocale().toString());
//
//		System.out.println("------------------------------------------------------------");
		
		
		reqHeader(request);
		long t1 = System.currentTimeMillis();
		arg2.doFilter(arg0, arg1);
		long t2 = System.currentTimeMillis() - t1;
		respHeader(response);
		
		
		System.out.format("\n--------------%s. %s, duration: %d ms ---------------\n"
				,count.toString()
				,(arg1.isCommitted() ? "committed" : "uncommitted")
				,t2);
//		
//		System.out.format("Type[%s]  Encoding[%s]  Locale[%s]\n",
//				response.getContentType(), 
//				response.getCharacterEncoding(),
//				response.getLocale().toString());
//		System.out.println("------------------------------------------------------------\n");
	}
	
	
	private static void reqHeader(HttpServletRequest request) {
System.out.println("---   browzer   ---");
		Enumeration<String> headerEnume = request.getHeaderNames();
		while (headerEnume.hasMoreElements()) {
			String name = headerEnume.nextElement();
			if (name.equals("ETag") || name.equals("Cache-Control") || name.equals("If-None-Match")
					|| name.equals("If-Modified-Since") || name.equals("Last-Modified")) {
				String var = request.getHeader(name);
				System.out.println("name[" + name + "]  var[" + var + "]");
			}
		}
System.out.println("---   browzer   ---");
	}
	/*
	 ETag
	 Cache-Control
	 Expires
	 If-None-Match
	 If-Modified-Since
	 Last-Modified
	 */
	private static void respHeader(HttpServletResponse response) {
System.out.println("---   server   ---");
		if (response.containsHeader("ETag")) {
			System.out.println("ETag");
		}
		if (response.containsHeader("Cache-Control")) {
			System.out.println("Cache-Control");
		}
		if (response.containsHeader("Expires")) {
			System.out.println("Expires");
		}
		if (response.containsHeader("If-None-Match")) {
			System.out.println("If-None-Match");
		}
		if (response.containsHeader("If-Modified-Since")) {
			System.out.println("If-Modified-Since");
		}
		if (response.containsHeader("Last-Modified")) {
			System.out.println("Last-Modified");
		}
System.out.println("---   server   ---");
	}
	@Override
	public void destroy() {
	}
}
