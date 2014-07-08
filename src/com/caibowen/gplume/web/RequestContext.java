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

import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.view.IView;


/**
 * RequestContext is a wrapper of HttpServletRequest and HttpServletResponse
 * with a ref to ControlCenter
 * 
 * @author BowenCai
 * 
 */
public class RequestContext implements Serializable {

	private static final long serialVersionUID = -8169661246935054100L;
	
	public final AbstractControlCenter controlCenter;
	
	public final HttpServletRequest 	request;
	public final HttpServletResponse 	response;
	public final HttpMethod 			httpmMthod;
	public final String 				path;
	
	private long _timeModified;

	/**
	 *  created by ControlCenter controlCenter only
	 * 
	 * @param in
	 * @param out
	 */
	public RequestContext(HttpServletRequest in, 
							HttpServletResponse out, 
							AbstractControlCenter c) {
		
		this.request = in;
		this.response = out;
		this.controlCenter = c;
		this.httpmMthod = HttpMethod.lookup(in.getMethod());
		
		//						  javax.servlet.forward.request_uri
		String _u = (String) in.getAttribute("javax.servlet.include.request_uri");
		if (_u == null) {
			_u = in.getRequestURI();
		}
		int idx = _u.toLowerCase().lastIndexOf(";jsessionid=");
		if (idx != -1) {
			String prefix = _u.substring(0, idx);
			int end = _u.lastIndexOf(';', idx + 12);
			_u = end != -1 ? prefix + _u.substring(end) : prefix;
		}
		
		this.path = _u.substring(request.getContextPath().length());
		
		_timeModified = System.currentTimeMillis();
	}
	
	/**
	 * 	JSP view
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
	
	/**
	 * 
	 * @param view
	 */
	public void render(IView view) {
		try {
			view.resolve(this);
		} catch (Exception e) {
			throw new RuntimeException("In request for [" 
					+ this.path + "] Error rendering [" + view + "]", e);
		}
	}
	
	/**
	 * reject this request
	 * return 503, e.g., service unavailable
	 */
	public void reject() {
		try {
			response.sendError(503);
		} catch (Exception e) {
			throw new RuntimeException("In request for [" 
					+ this.path + "] Error sending 503 service unavailable error", e);
		}
	}

	/**
	 * sendRedirect
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

	/**
	 * pass this request to other handle
	 * @WARN short circuit!
	 * @param actionName
	 */
	public void passOff(String actionName) {
		controlCenter.handle(actionName, this);
	}

// -----------------------------------------------
	public boolean containsCookie(final String name) {
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
//        name.equalsIgnoreCase("Comment") // rfc2019
//        name.equalsIgnoreCase("Discard") // 2019++
//        name.equalsIgnoreCase("Domain") ||
//        name.equalsIgnoreCase("Expires") // (old cookies)
//        name.equalsIgnoreCase("Max-Age") // rfc2019
//        || name.equalsIgnoreCase("Path") ||
//        name.equalsIgnoreCase("Secure") ||
//        name.equalsIgnoreCase("Version")
	
	public void addCookie(final String value) {
		addCookie(value, -1);
	}
	public void addCookie(final String value, final int ageInSecond) {
		addCookie(Thread.currentThread().getName(), value, ageInSecond);
	}
	
	public void addCookie(final String name, final String value, final int ageInSecond) {
		
		if (Str.Patterns.COOKIE_NAME.matcher(name).matches()
			&& !name.equalsIgnoreCase("Comment") // rfc2019
			&& !name.equalsIgnoreCase("Discard") // 2019++
			&& !name.equalsIgnoreCase("Domain")
			&& !name.equalsIgnoreCase("Expires") // (old cookies)
			&& !name.equalsIgnoreCase("Max-Age") // rfc2019
			&& !name.equalsIgnoreCase("Path")
			&& !name.equalsIgnoreCase("Secure") 
			&& !name.equalsIgnoreCase("Version")
			&& !name.startsWith("$")) {
			Cookie cookie = new Cookie(name, value);
			cookie.setMaxAge(ageInSecond);
			response.addCookie(cookie);
			
		} else {
			throw new IllegalArgumentException(
					"illegal cookie name or name is token[" + name 
					+"]\nname pattern [" + Str.Patterns.COOKIE_NAME.pattern() + "]");
		}
	}
	
	public void addCookie(Cookie ck) {
		
		if (Str.Patterns.COOKIE_NAME.matcher(ck.getName()).matches()) {
			response.addCookie(ck);
		} else {
			throw new IllegalArgumentException(
					"illegal cookie name[" + ck.getName() 
					+"]\nname pattern [" + Str.Patterns.COOKIE_NAME.pattern() + "]");
		}
	}

	@Nullable
	public Cookie getCookie(final String name) {
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

	public HttpSession session(boolean boo) {
		return request.getSession(boo);
	}
// -------------------------------------------------------------------
//				web cache utilities
//-------------------------------------------------------------------
	@Nullable
	public String eTag() {
		
		String tag = request.getHeader("If-None-Match");
		
		return Str.Utils.notBlank(tag) ? tag : null;
	}
	/**
	 * 
	 * @param etag not null!
	 * @return false if do not have e-tag in request or does not match
	 */
	public boolean matchETag(final String etag) {
		
		String tag = request.getHeader("If-None-Match");
		if (Str.Utils.notBlank(tag)) {
			return tag.equals(etag);
		} else {
			return false;
		}
	}
	
	public long lastModified() {

		String _lastModified = request.getHeader("If-Modified-Since");
		if (Str.Utils.notBlank(_lastModified)) {
			return Long.parseLong(_lastModified.trim());
		} else {
			return -1L;
		}
	}
	
	//012345678
	//max-age=0
	//max-age=10
	public int cacheControl() {
		
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
		return 0;
	}
	
	public void setETag(final String tag) {
		response.setHeader("ETag", tag);
	}

	public long timeModified() {
		return _timeModified;
	}
	
	public void setTimeModified(long timeModified) {
		this._timeModified = timeModified;
		response.setHeader("Last-Modified", Long.toString(timeModified));
	}
	
	public void setCacheControl(int second) {
		String info =  "max-age=" + second;
		response.setHeader("Cache-Control",info);
		response.setHeader("Expires", Long.toString(_timeModified + second * 1000L));
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
	
	public void remove(String name) {
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

// -----------------------------------------------------------------------------

}
//private static final String METHOD_GET		= "GET";
//private static final String METHOD_POST		= "POST";
//private static final String METHOD_PUT		= "PUT";
//private static final String METHOD_DELETE	= "DELETE";
//private static final String METHOD_HEAD		= "HEAD";
//private static final String METHOD_OPTIONS	= "OPTIONS";
//private static final String METHOD_TRACE	= "TRACE";
