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
import java.io.Serializable;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.core.TypeTraits;
import com.caibowen.gplume.misc.Str;


/**
 * RequestContext is a wrapper class of HttpServletRequest and HttpServletResponse
 * with access to ControlCenter
 * 
 * @author BowenCai
 * 
 */
public class RequestContext implements Serializable {

	private static final long serialVersionUID = -8169661246935054100L;

	private static final Logger LOG = Logger.getLogger(RequestContext.class.getName());
	
	public final AbstractControlCenter controlCenter;
	
	public final HttpServletRequest 	request;
	public final HttpServletResponse 	response;
	public final ServletContext			context;
	public final HttpMethod 			httpmMthod;
	public final String 				uri;
	
	private long timeModified;

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
		this.context = c.getServletContext();
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
		this.uri = _u;
		
		timeModified = System.currentTimeMillis();
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
					+ this.uri + "] Error forwarding[" + jspView + "]", e);
		}
	}
	
	public void render(View view) {
		try {
			view.resolve(this);
		} catch (Exception e) {
			throw new RuntimeException("In request for [" 
					+ this.uri + "] Error rendering [" + view + "]", e);
		}
	}


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
	 * @WARN: short circuit!
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
		
		if (Str.Patterns.COOKIE_NAME.matcher(name).matches()) {
			Cookie cookie = new Cookie(name, value);
			cookie.setMaxAge(ageInSecond);
			response.addCookie(cookie);
		} else {
			throw new IllegalArgumentException(
					"illegal cookie name[" + name 
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

	public HttpSession getSession(boolean boo) {
		return request.getSession(boo);
	}
// -------------------------------------------------------------------
//				web cache utilities
//-------------------------------------------------------------------
	@Nullable
	public String getETag() {
		
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
	
	public long getLastModified() {

		String lastModified = request.getHeader("If-Modified-Since");
		if (Str.Utils.notBlank(lastModified)) {
			return Long.parseLong(lastModified.trim());
		} else {
			return -1L;
		}
	}
	
	//012345678
	//max-age=0
	//max-age=10
	public int getCacheControl() {
		
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

	public long getTimeModified() {
		return timeModified;
	}
	
	public void setTimeModified(long timeModified) {
		this.timeModified = timeModified;
		response.setHeader("Last-Modified", Long.toString(timeModified));
	}
	
	public void setCacheControl(int second) {
		String info =  "max-age=" + second + ", private";
		response.setHeader("Cache-Control",info);
		response.setHeader("Expires", Long.toString(timeModified + second * 1000L));
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
	
	public void redirect(String uri) {
		try {
			response.sendRedirect(uri);
		} catch (Exception e) {
			throw new RuntimeException("In request for [" 
					+ this.uri + "] Error sending redirect [" + uri + "]", e);
			
		}
	}
//-----------------------------------------------------------------------------
	public Object mapFromRequest(Object object, boolean reflectPrivate) {
		Enumeration<String> params = request.getParameterNames();
		try {
			while (params.hasMoreElements()) {
				String paramName = params.nextElement();
				String var = request.getParameter(paramName);
				TypeTraits.assignField(object, paramName, var, reflectPrivate);
			}
			return object;
		} catch (IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException e) {
			LOG.log(Level.SEVERE, "error setting field for object of class[" 
								+ object.getClass().getName()
								+ "]. Cause: " + e.getCause(), e);
		}
		return null;
	}
	
	public Object mapFromSession(Object object, boolean reflectPrivate) {
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		Enumeration<String> params = session.getAttributeNames();
		try {
			while (params.hasMoreElements()) {
				String paramName = params.nextElement();
				Object var = session.getAttribute(paramName);
				TypeTraits.assignField(object, paramName, var, reflectPrivate);
			}
			return object;
		} catch (IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException e) {
			LOG.log(Level.SEVERE, "error setting field for object of class[" 
								+ object.getClass().getName()
								+ "]. Cause: " + e.getCause(), e);
		}
		return null;
	}
	
// -------------------------------------------------------------------

	public void putAttr(String name, Object value) {
		request.setAttribute(name, value);
	}
	
	@Nullable
	public<T> T getAttr(String key) {
		return (T) request.getAttribute(key);
	}
	
	public void remove(String name) {
		request.removeAttribute(name);
	}

// -------------------------------------------------------------------

	@Nullable
	public Integer getIntParam(String name) {
		return Converter.toInteger(request.getParameter(name));
	}
	
	public int getIntParam(String name, int defaultVar) {
		Integer integer = getIntParam(name);
		return integer == null ? defaultVar : integer.intValue();
	}
	@Nullable
	public int[] getIntsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			int[] vars = new int[s.length];
			for (int i = 0; i < s.length; i++) {
				vars[i] = Converter.toInteger(s[i]);
			}
			return vars;
		} else {
			return null;
		}
	}
	
	public int[] getIntsParam(String name, int[] defaultVar) {

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
		return Converter.toLong(request.getParameter(name));
	}
	
	public long getLongParam(String name, long defaultVar) {
		Long var = getLongParam(name);
		return var == null ? defaultVar : var.longValue();
	}
	
	@Nullable
	public long[] getLongsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			long[] vars = new long[s.length];
			for (int i = 0; i < s.length; i++) {
				vars[i] = Converter.toLong(s[i]);
			}
			return vars;
		} else {
			return null;
		}
	}
	
	public long[] getLongsParam(String name, long[] defaultVar) {

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
		return Converter.toFloat(request.getParameter(name));
	}
	
	public float getFloatParam(String name, float defaultVar) {
		Float var = getFloatParam(name);
		return var == null ? defaultVar : var.floatValue();
	}
	@Nullable
	public float[] getFloatsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			float[] vars = new float[s.length];
			for (int i = 0; i < s.length; i++) {
				vars[i] = Converter.toFloat(s[i]);
			}
			return vars;
		} else {
			return null;
		}
	}
	public float[] getFloatsParam(String name, float[] defaultVar) {

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
		return Converter.toDouble(request.getParameter(name));
	}
	
	public double getDoubleParam(String name, double defaultVar) {
		Double var = getDoubleParam(name);
		return var == null ? defaultVar : var.doubleValue();
	}
	@Nullable
	public double[] getDoublesParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			double[] vars = new double[s.length];
			for (int i = 0; i < s.length; i++) {
				vars[i] = Converter.toDouble(s[i]);
			}
			return vars;
		} else {
			return null;
		}
	}
	public double[] getDoublesParam(String name, double[] defaultVar) {

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
		return Converter.toBool(request.getParameter(name));
	}
	
	public boolean getBoolParam(String name, boolean defaultVar) {
		Boolean var = getBoolParam(name);
		return var == null ? defaultVar : var.booleanValue();
	}
	
	@Nullable
	public boolean[] getBoolsParam(String name) {

		String[] s = request.getParameterValues(name);
		if (s != null) {
			boolean[] vars = new boolean[s.length];
			for (int i = 0; i < s.length; i++) {
				vars[i] = Converter.toBool(s[i]);
			}
			return vars;
		} else {
			return null;
		}
	}
	public boolean[] getBoolsParam(String name, boolean[] defaultVar) {

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


//
// @Inject
// public void setErrorHandler(String clazzName) {
//
// try {
// Object _handler = Class.forName(clazzName).newInstance();
// if (_handler instanceof IErrorHandler) {
// this.errorHandler = (IErrorHandler)_handler;
// } else {
// throw new IllegalArgumentException(
// "class [" + clazzName + "] is not IErrorHandler"
// +"\nrather it is [" + _handler.getClass().getName() + "]");
// }
// } catch (InstantiationException | IllegalAccessException
// | ClassNotFoundException e) {
// e.printStackTrace();
//
// throw new RuntimeException(
// "cannot instantiate error handler class[" + clazzName + "]"
// + "\nError:" + e.getMessage(),
// e);
// }
// }
