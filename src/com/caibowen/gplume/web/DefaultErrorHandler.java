package com.caibowen.gplume.web;

import java.io.IOException;
import java.io.PrintWriter;

import com.caibowen.gplume.web.ControlCenter.Request;


/**
 * common handlers for errors(e.g., 404, 500)
 * 
 * @author BowenCai
 *
 */
public class DefaultErrorHandler implements IErrorHandler{
	
//	5. HTTP error 401 (unauthorized)
//	This error happens when a website visitor tries to access a restricted web page 
//	but isn¡¯t authorized to do so, usually because of a failed login attempt.
//
//	4. HTTP error 400 (bad request)
//	This is basically an error message from the web server telling you 
//	that the application you are using (e.g. your web browser) accessed it incorrectly or that the request was somehow corrupted on the way.
//
//	3. HTTP error 403 (forbidden)
//	This error is similar to the 401 error, but note the difference between 
//	unauthorized and forbidden. In this case no login opportunity was available. This can for example happen if you try to access a (forbidden) directory on a website.
//
//	2. HTTP error 404 (not found)
//	Most people are bound to recognize this one. A 404 error happens when you 
//	try to access a resource on a web server (usually a web page) that doesn¡¯t exist. Some reasons for this happening can for example be a broken link, a mistyped URL, or that the webmaster has moved the requested page somewhere else (or deleted it). To counter the ill effect of broken links, some websites set up custom pages for them (and some of those are really cool).

////	429 Too Many Requests (RFC 6585)
//	The user has sent too many requests in a given amount of time. 
//	Intended for use with rate limiting schemes.[18]
	
	
//	And the most common HTTP error of all is¡­¡­¡­.
//
//	1. HTTP error 500 (internal server error)
//	The description of this error pretty much says it all. 
//	It¡¯s a general-purpose error message for when a web server encounters
//	some form of internal error. For example, the web server could be overloaded 
//	and therefore unable to handle requests properly.
	

	@Override
	public void http403(Request request) {
		PrintWriter out;
		try {
			out = request.getOut();
			out.format("<html><body>");
			out.format("<br><br><center><h1> %s </center></h1>", 
					"HTTP error 403 : Forbidden.");
			
			out.format("<br><br><center><h1>-------------------------  %s  -------------------------</center></h1>", 
					"<a href=\"http://www.caibowen.com/work/app/gplume\">Gplume</a>");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void http404(Request request) {

		request.out.reset();
		PrintWriter out;
		
		try {
			
			out = request.getOut();
			out.format("<html><body>");
			
			out.format("<br><br><br><br><center><h1> %s </center></h1>", 
					"<a href=\"http://en.wikipedia.org/wiki/HTTP_404\">"+
					"HTTP 404: Page not found.</a>");

			out.format("<br><br><center><h1>-------------------------  %s  -------------------------</center></h1>", 
					"<a href=\"http://www.caibowen.com/work/app/gplume\">Gplume</a>");
			
			out.format("</body></html>");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void http500(Request request) {

		request.out.reset();
		PrintWriter out = null;
		
		try {
			
			out = request.getOut();
			out.format("<html><body>");
			out.format("<br><br><br><br><center><h1> %s </center></h1>", 
					"HTTP 500 : Internal server error");
			
			out.format("<br><br><center><h1>-------------------------  %s  -------------------------</center></h1>", 
					"<a href=\"http://www.caibowen.com/work/app/gplume\">Gplume</a>");
			
			out.format("</body></html>");
			out.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
