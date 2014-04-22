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

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caibowen.gplume.web.IErrorHandler;
import com.caibowen.gplume.web.RequestContext;


/**
 * common handlers for errors(e.g., 404, 500)
 * 
 * @author BowenCai
 *
 */
public class DefaultErrorHandler implements IErrorHandler {
	
	private static final Logger LOG = Logger.getLogger(DefaultErrorHandler.class.getName());
	
//	5. HTTP error 401 (unauthorized)
//	This error happens when a website visitor tries to access a restricted web page 
//	but isn��t authorized to do so, usually because of a failed login attempt.
//
//	4. HTTP error 400 (bad requestContext)
//	This is basically an error message from the web server telling you 
//	that the application you are using (e.g. your web browser) accessed it incorrectly or that the requestContext was somehow corrupted on the way.
//
//	3. HTTP error 403 (forbidden)
//	This error is similar to the 401 error, but note the difference between 
//	unauthorized and forbidden. In this case no login opportunity was available. This can for example happen if you try to access a (forbidden) directory on a website.
//
//	2. HTTP error 404 (not found)
//	Most people are bound to recognize this one. A 404 error happens when you 
//	try to access a resource on a web server (usually a web page) that doesn��t exist. Some reasons for this happening can for example be a broken link, a mistyped URL, or that the webmaster has moved the requested page somewhere else (or deleted it). To counter the ill effect of broken links, some websites set up custom pages for them (and some of those are really cool).

////	429 Too Many Requests (RFC 6585)
//	The user has sent too many requests in a given amount of time. 
//	Intended for use with rate limiting schemes.[18]
	
	
//	And the most common HTTP error of all is������.
//
//	1. HTTP error 500 (internal server error)
//	The description of this error pretty much says it all. 
//	It��s a general-purpose error message for when a web server encounters
//	some form of internal error. For example, the web server could be overloaded 
//	and therefore unable to handle requests properly.

	@Override
	public void http403(RequestContext requestContext) {
		PrintWriter out;
		
		if (requestContext.response.isCommitted()) {
			try {
				requestContext.response.sendError(403, "error handling 403: response has been committed");
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "error setting response to 403: response already committed\n"
										+ "error sending error response; Message[" + e.getMessage() + "]");
			}
		}
		requestContext.response.reset();
		try {
			out = requestContext.response.getWriter();
			requestContext.response.setStatus(403);
			out.format("<html><title>%s</title>", "http 403 | Gplume");
			out.format("<body>");
			out.format("<br><br><center><h1> %s </center></h1>", 
					"HTTP error 403 : Forbidden.");
			
			out.format("<br><br><center><h1>-------------------------  %s  -------------------------</center></h1>", 
					"<a href=\"http://www.caibowen.com/work/app/gplume\">Gplume</a>");
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void http404(RequestContext requestContext) {

		PrintWriter out;
		if (requestContext.response.isCommitted()) {
			try {
				requestContext.response.sendError(404, "error handling 404: response has been committed");
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "error setting response to 404: response already committed\n"
										+ "error sending error response; Message[" + e.getMessage() + "]");
			}
		}
		requestContext.response.reset();
		try {
			requestContext.response.setStatus(404);
			out = requestContext.response.getWriter();
			out.format("<html><title>%s</title>", "http 404 | Gplume");
			out.format("<body>");
			
			out.format("<br><br><br><br><center><h1> %s </center></h1>", 
					"<a href=\"http://en.wikipedia.org/wiki/HTTP_404\">"+
					"HTTP 404: Page not found.</a>");

			out.format("<br><br><center><h1>-------------------------  %s  -------------------------</center></h1>", 
					"<a href=\"http://www.caibowen.com/work/app/gplume\">Gplume</a>");
			
			out.format("</body></html>");
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void http500(RequestContext requestContext) {

		PrintWriter out;
		if (requestContext.response.isCommitted()) {
			try {
				requestContext.response.sendError(500, "error handling 500: response has been committed");
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "error setting response to 500: response already committed\n"
										+ "error sending error response; Message[" + e.getMessage() + "]");
			}
		}
		requestContext.response.reset();
		try {
			requestContext.response.setStatus(500);
			out = requestContext.response.getWriter();
			out.format("<html><title>%s</title>", "http 500 | Gplume");
			out.format("<body>");
			out.format("<br><br><br><br><center><h1> %s </center></h1>", 
					"HTTP 500 : Internal server error");
			Throwable throwable = requestContext.getAttr(java.lang.Throwable.class.getName());
			if (throwable != null) {

				out.println("\n\n"
						+"<div style=\" width: 90%;" +
										"margin-left: auto;" +
										" margin-right: auto;" +
										" font-size: 16px;" +
										"\">"
						+"<hr/><pre><code>\n");
				throwable.printStackTrace(out);
				out.println("</code></pre><hr/>\n");
			}
			out.format("\n</div>\n<br><br>\n<center><h1>-------------------------  %s  -------------------------</center></h1>", 
					"<a href=\"http://www.caibowen.com/work/app/gplume\">Gplume</a>\n");
			
			out.format("</body></html>");
			out.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
