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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Str;


/**
 * RequestContext is a wrapper of HttpServletRequest and HttpServletResponse
 * with a ref to ControlCenter
 * 
 * @author BowenCai
 * 
 */
public class RequestContext implements Serializable {

	private static final long serialVersionUID = -8169661246935054100L;
	
	public final BaseControlCenter controlCenter;
	
	public final HttpServletRequest 	request;
	public final HttpServletResponse 	response;
	public final HttpMethod 			httpmMthod;
	public final String 				path;
	
	private long timeModified;

	public static String uri(HttpServletRequest request) {
		//						  javax.servlet.forward.request_uri
		String _u = (String) request.getAttribute("javax.servlet.include.request_uri");
		if (_u == null) {
			_u = request.getRequestURI();
		}
		int idx = _u.toLowerCase().lastIndexOf(";jsessionid=");
		if (idx != -1) {
			String prefix = _u.substring(0, idx);
			int end = _u.lastIndexOf(';', idx + 12);
			_u = end != -1 ? prefix + _u.substring(end) : prefix;
		}
		int prefixLen = request.getContextPath().length();
		return prefixLen == 0 ? _u : _u.substring(prefixLen);
	}
	/**
	 *  created by ControlCenter controlCenter only
	 * 
	 * @param in
	 * @param out
	 */
	//public for test only
	public RequestContext(HttpServletRequest in, 
							HttpServletResponse out, 
							BaseControlCenter c) {
		
		this.request = in;
		this.response = out;
		this.controlCenter = c;
		this.httpmMthod = HttpMethod.lookup(in.getMethod());
		this.path = uri(in);
		timeModified = System.currentTimeMillis();
	}
	
	/**
	 * 	JSP views
	 * @param jspView
	 */
	public void render(String jspView) {
		RequestDispatcher dispatcher = 
				controlCenter.getServletContext().getRequestDispatcher(jspView);
		try {
			dispatcher.forward(request, response);
		} catch (ServletException | IOException e) {
			throw new RuntimeException("In request for [" 
					+ this.path + "] Error forwarding[" + jspView + "]", e);
		}
	}

	public void renderAsStatic() {
		controlCenter.handleStatic(this);
	}

	/**
	 * reject this request
	 * return 403, e.g., authentication failed
	 */
	public void reject() {
		try {
			response.sendError(403);
		} catch (Exception e) {
			throw new RuntimeException("In request for [" 
					+ this.path + "] Error sending 403 error", e);
		}
	}

    /**
     * set 503 service unavailable
     */
    public void setUnAvailiable() {
        try {
            response.sendError(503);
        } catch (Exception e) {
            throw new RuntimeException("In request for ["
                    + this.path + "] Error sending 503 error", e);
        }
    }

    public void error(int code) {
        controlCenter.errorHandler.error(this, code);
    }

    public void error(int code, String message) {
        controlCenter.errorHandler.error(this, code, message);
    }

	/**
	 * AKA, sendRedirect
	 * 
	 * @param url
	 */
	public void jumpTo(final String url) {
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			throw new RuntimeException(
					"I/O Error sendRedirect[" + url + "]"
					+ "\n Cause:" + e.getCause(), e);
		}
	}

//    private static final String NAME = RequestContext.class.getName();
	/**
	 * pass this request to other handle
	 * @WARN short circuit!
     *
	 * @param actionName
	 */
	public void passOff(String actionName) {
		controlCenter.handle(actionName, this);
	}

// -----------------------------------------------
	public boolean hasCookie(final String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * cookie name is the name of this thread
	 * @param value cookie value
	 */
	public void addCookie(final String value) {
		addCookie(value, -1);
	}
	
	/**
	 * cookie name is the name of this thread
	 * @param value cookie value
	 */
	public void addCookie(final String value, final int ageInSecond) {
		addCookie(Thread.currentThread().getName(), value, ageInSecond);
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param ageInSecond  age in second
	 */
	public void addCookie(final String name, final String value, final int ageInSecond) {
		
		if (cookieNameValid(name)) {
			Cookie cookie = new Cookie(name, value);
			cookie.setMaxAge(ageInSecond);
			response.addCookie(cookie);
			
		} else {
			throw new IllegalArgumentException(
					"illegal cookie id or id is token[" + name 
					+"]\nname pattern [" + Str.Patterns.COOKIE_NAME.pattern() + "]");
		}
	}
	
	public void addCookie(Cookie ck) {
		if (cookieNameValid(ck.getName())) {
			response.addCookie(ck);
		} else {
			throw new IllegalArgumentException(
					"illegal cookie name[" + ck.getName() 
					+"]\nname pattern [" + Str.Patterns.COOKIE_NAME.pattern() + "]");
		}
	}
	
	private static boolean cookieNameValid(@Nonnull String name) {
		return Str.Patterns.COOKIE_NAME.matcher(name).matches()
		&& !name.equalsIgnoreCase("Comment") // rfc2019
		&& !name.equalsIgnoreCase("Discard") // 2019++
		&& !name.equalsIgnoreCase("Domain")
		&& !name.equalsIgnoreCase("Expires") // (old cookies)
		&& !name.equalsIgnoreCase("Max-Age") // rfc2019
		&& !name.equalsIgnoreCase("Path")
		&& !name.equalsIgnoreCase("Secure") 
		&& !name.equalsIgnoreCase("Version")
		&& !name.startsWith("$");
	}

	@Nullable
	public Cookie cookie(final String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	@Nullable
	public String cookieVal(String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	
	public HttpSession session(boolean boo) {
		return request.getSession(boo);
	}
	
	@Nullable
	public<T> T sessionAttr(String name) {
		HttpSession session = request.getSession(true);
		return (T)session.getAttribute(name);
	}
	

	public ServletContext context() {
		return controlCenter.getServletContext();
	}
	
	@Nullable
	public<T> T contextAttr(String name) {
		return (T)controlCenter.getServletContext().getAttribute(name);
	}
	
// -------------------------------------------------------------------
//				web cache utilities
//-------------------------------------------------------------------
	public void setETag(final String tag) {
		response.setHeader("ETag", tag);
	}
	
	@Nullable
	public String eTag() {
		String tag = request.getHeader("If-None-Match");
		return Str.Utils.notBlank(tag) ? tag : null;
	}

	/**
	 * @param etag 
	 * @return false if do not have e-tag in request or does not match
	 */
	public boolean matchETag(@Nonnull final String etag) {
		
		String tag = request.getHeader("If-None-Match");
		if (Str.Utils.notBlank(tag)) {
			return tag.equals(etag);
		} else {
			return false;
		}
	}
	
	public void setTimeModified(long timeModified) {
		this.timeModified = timeModified;
		response.setHeader("Last-Modified", Long.toString(timeModified));
	}
	
	public long timeModified() {
		return timeModified;
	}
	
	/**
	 * 
	 * @return -1 if not specified
	 */
	public long lastModified() {

		String _lastModified = request.getHeader("If-Modified-Since");
		if (Str.Utils.notBlank(_lastModified)) {
			return Long.parseLong(_lastModified.trim());
		} else {
			return -1L;
		}
	}
	
	/**
	 * set max age and expire time
	 * @param second
	 */
	public void setCacheControl(int second) {
		String info =  "max-age=" + second;
		response.setHeader("Cache-Control", info);
		response.setHeader("Expires", Long.toString(timeModified + second * 1000L));
	}
	
	//012345678
	//max-age=0
	//max-age=10
	/**
	 * get max age header
	 * @return -1 if not specified
	 */
	public int maxAge() {
		
		String cacheCtrl = request.getHeader("Cache-Control");
		if (Str.Utils.notBlank(cacheCtrl)) {
			cacheCtrl = cacheCtrl.trim();
			if (cacheCtrl.startsWith("max-age=", 0)) {
				int idx = cacheCtrl.lastIndexOf((int)',');
				if (idx > -1) {//max-age=10,private
					return Integer.parseInt(cacheCtrl.substring(8, idx));
				}
				return Integer.parseInt(cacheCtrl.substring(8));
			}
		}
		return -1;
	}
	
	public boolean canBeGZipped() {
		
		final Enumeration<?> accepted = request.getHeaders("Accept-Encoding");
		while (accepted.hasMoreElements()) {
			final String headerValue = (String) accepted.nextElement();
			if (headerValue.indexOf("gzip") != -1) {
				return true;
			}
		}
		return false;
	}

	
// -------------------------------------------------------------------

	public void putAttr(String name, Object value) {
		request.setAttribute(name, value);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public<T> T attr(String key) {
		return (T) request.getAttribute(key);
	}
	
	public void removeAttr(String name) {
		request.removeAttribute(name);
	}

// -------------------------------------------------------------------

	@Nullable
	public Integer getIntParam(String name) {
		return Converter.slient.toInteger(request.getParameter(name));
	}
	
	public int getIntParam(String name, int defaultVar) {
		Integer integer = getIntParam(name);
		return integer == null ? defaultVar : integer.intValue();
	}
	
	@Nullable
	public List<Integer> getIntsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			 List<Integer> vars = new ArrayList<Integer>(s.length);
			for (int i = 0; i < s.length; i++) {
				vars.add(Converter.toInteger(s[i]));
			}
			return vars;
		} else {
			return null;
		}
	}
	
	public List<Integer> getIntsParam(String name, List<Integer> defaultVar) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			return getIntsParam(name);
		} else {
			return defaultVar;
		}
	}
// ------------------------------------
	@Nullable
	public Long getLongParam(String name) {
		return Converter.slient.toLong(request.getParameter(name));
	}
	
	public long getLongParam(String name, long defaultVar) {
		Long var = getLongParam(name);
		return var == null ? defaultVar : var.longValue();
	}
	
	@Nullable
	public List<Long> getLongsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			List<Long> vars = new ArrayList<>(s.length);
			for (int i = 0; i < s.length; i++) {
				vars.add(Converter.slient.toLong(s[i]));
			}
			return vars;
		} else {
			return null;
		}
	}
	
	public List<Long> getLongsParam(String name, List<Long> defaultVar) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			return getLongsParam(name);
		} else {
			return defaultVar;
		}
	}
	
// -------------------------------------
	@Nullable
	public Float getFloatParam(String name) {
		return Converter.slient.toFloat(request.getParameter(name));
	}
	
	public float getFloatParam(String name, float defaultVar) {
		Float var = getFloatParam(name);
		return var == null ? defaultVar : var.floatValue();
	}
	
	@Nullable
	public List<Float> getFloatsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			List<Float> vars = new ArrayList<>(s.length);
			for (int i = 0; i < s.length; i++) {
				vars.add(Converter.slient.toFloat(s[i]));
			}
			return vars;
		} else {
			return null;
		}
	}
	public List<Float> getFloatsParam(String name, List<Float> defaultVar) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			return getFloatsParam(name);
		} else {
			return defaultVar;
		}
	}
// -------------------------------------
	@Nullable
	public Double getDoubleParam(String name) {
		return Converter.slient.toDouble(request.getParameter(name));
	}
	
	public double getDoubleParam(String name, double defaultVar) {
		Double var = getDoubleParam(name);
		return var == null ? defaultVar : var.doubleValue();
	}
	@Nullable
	public List<Double> getDoublesParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			List<Double> vars = new ArrayList<>(s.length);
			for (int i = 0; i < s.length; i++) {
				vars.add(Converter.slient.toDouble(s[i]));
			}
			return vars;
		} else {
			return null;
		}
	}
	public List<Double> getDoublesParam(String name, List<Double> defaultVar) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			return getDoublesParam(name);
		} else {
			return defaultVar;
		}
	}
// ---------------------------------------
	@Nullable
	public Boolean getBoolParam(String name) {
		return Converter.slient.toBool(request.getParameter(name));
	}
	
	public boolean getBoolParam(String name, boolean defaultVar) {
		Boolean var = getBoolParam(name);
		return var == null ? defaultVar : var.booleanValue();
	}
	
	@Nullable
	public List<Boolean> getBoolsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			List<Boolean> vars = new ArrayList<>(s.length);
			for (int i = 0; i < s.length; i++) {
				vars.add(Converter.slient.toBool(s[i]));
			}
			return vars;
		} else {
			return null;
		}
	}
	public List<Boolean> getBoolsParam(String name, List<Boolean> defaultVar) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			return getBoolsParam(name);
		} else {
			return defaultVar;
		}
	}
	
	@Nullable
	public String getStrParam(String name) {
		return request.getParameter(name);
	}
	
	public String getStrParam(String name, String def) {
		String ret = request.getParameter(name);
		return ret == null ? def : ret;
	}
	
	@Nullable
	public String[] getStrArrayParam(String name) {
		return request.getParameterValues(name);
	}
	public String[] getStrArrayParam(String name, String[] def) {
		String[] ret = request.getParameterValues(name);
		return ret == null ? ret : def;
	}
	
	
	/**
	 * to JSON string
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(1024);
		b.append("{\r\n\tRequestContext : {"
				+ "\r\n \t\t \"httpmMthod\" : \"" + httpmMthod 
				+ "\",\r\n \t\t \"path\" : \"" + path 
				+ "\",\r\n \t\t \"query string\" : \"" + request.getQueryString() 
				+ "\",\r\n \t\t \"remote ddress\" : \"" + request.getRemoteAddr() 
				+ "\",\r\n \t\t \"timeModified\" : \"" + timeModified 
				+ "\"\r\n\t}\r\n");
		
		int len = 0;
		boolean added = false;
		b.append("\tRequest Parameters : {\r\n");
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			String var = request.getParameter(paramName);
			b.append("\t\t\"").append(paramName).append("\" : \"").append(var).append("\", \r\n");
			added = true;
		}
		if (added) {
			len = b.length();
			b.delete(len - 4, len - 3);
		}
		added = false;
		
		b.append("\t}\r\n\tCookies: [\r\n");
		Cookie[] cks = request.getCookies();
		if (cks != null)
		for (Cookie ck : request.getCookies()) {
			b.append("\t\t{\r\n\t\t\t\"name\" : ").append("\"").append(ck.getName()).append("\", \r\n");
			b.append("\t\t\t\"value\" : ").append("\"").append(ck.getValue()).append("\", \r\n");
			String var = ck.getComment();
			if (Str.Utils.notBlank(var)) {
				b.append("\t\t\t\"comment\" : ").append("\"").append(var).append("\", \r\n");
			}
			var = ck.getDomain();
			if (Str.Utils.notBlank(var)) {
				b.append("\t\t\t\"domain\" : ").append("\"").append(var).append("\", \r\n");
			}
			var = ck.getPath();
			if (Str.Utils.notBlank(var)) {
				b.append("\t\t\t\"path\" : ").append("\"").append(var).append("\", \r\n");
			}
			b.append("\t\t\t\"secure\" : ").append("\"").append(ck.getSecure()).append("\", \r\n");
			b.append("\t\t\t\"max age\" : ").append("\"").append(ck.getMaxAge()).append("\", \r\n");
			b.append("\t\t\t\"version\" : ").append("\"").append(ck.getVersion()).append("\"\r\n\t\t}, \r\n");
			added = true;
		}
		if (added) {
			len = b.length();
			b.delete(len - 4, len - 3);
		}
		added = false;
		
		b.append("\t]\r\n\tRequest Attributes : {\r\n");
		params = request.getAttributeNames();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			Object var = request.getAttribute(paramName);
			b.append("\t\t\"").append(paramName).append("\" : \"").append(var).append("\", \r\n");
			added = true;
		}
		if (added) {
			len = b.length();
			b.delete(len - 4, len - 3);
		}
		added = false;
		
		HttpSession session = request.getSession(false);
		b.append("\t}\r\n\tSession Attributes : {\r\n");
		if (session != null) {
			b
			.append(" \t\t \"session id\" : \"").append(session.getId())
			.append("\",\r\n \t\t \"creation time\" : \"").append(session.getCreationTime())
			.append("\",\r\n \t\t \"lastAccessed time\" : \"").append(session.getLastAccessedTime())
			.append("\",\r\n \t\t \"maxInactive interval\" : \"").append(session.getMaxInactiveInterval()).append("\"");
			
			params = session.getAttributeNames();
			while (params.hasMoreElements()) {
				String paramName = params.nextElement();
				Object var = request.getAttribute(paramName);
				b.append(", \r\n \t\t \"")
				.append(paramName).append("\" : \"").append(var).append('\"');
			}
		}
		
		b.append("\r\n\t}\r\n\tServlet Context Attributes : {\r\n");
		params = context().getAttributeNames();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			Object var = request.getAttribute(paramName);
			b.append("\t\t\"").append(paramName).append("\" : \"").append(var).append("\", \r\n");
			added = true;
		}
		if (added) {
			len = b.length();
			b.delete(len - 4, len - 3);
		}
		added = false;
		b.append("\r\n\t}\r\n}");
		return b.toString();
	}

}

