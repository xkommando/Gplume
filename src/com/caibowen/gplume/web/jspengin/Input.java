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
package com.caibowen.gplume.web.jspengin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.caibowen.gplume.web.RequestContext;


/**
 * Wrapper for the HttpServletRequest.
 * 
 * delegate all attributes to the hashmap,
 * so that all operation during the compilation will not affect the original request
 * 
 * @author BowenCai
 *
 */
public class Input extends WeakHashMap<String, Object> implements HttpServletRequest {
	
	public HttpServletRequest original;

	public static Input buildFrom(RequestContext c) {
		return new Input(c.request);
	}
	public static Input buildFrom(HttpServletRequest c) {
		return new Input(c);
	}
	
	private Input(HttpServletRequest request) {
		super(64);
		this.original = request;
		Enumeration<String> names = original.getAttributeNames();
		while (names.hasMoreElements()) {
			String string = (String) names.nextElement();
			this.put(string, original.getAttribute(string));
		}
	}
	
	@Override
	public Object getAttribute(String arg0) {
		return get(arg0);
	}

	@Override
	public Enumeration getAttributeNames() {
		Set<String> keys = keySet();
		Vector<String> vector = new Vector<String>(keys.size());
		for (String k : keys) {
			vector.add(k);
		}
		return vector.elements();
	}

	@Override
	public void removeAttribute(String arg0) {
		original.removeAttribute(arg0);
		this.remove(arg0);
	}


	@Override
	public void setAttribute(String arg0, Object arg1) {
		this.put(arg0, arg1);
	}

//-----------------------------------------------------------------------------
	/**
	 * The default behavior of this method is to return getAuthType() on the
	 * wrapped original object.
	 */
	@Override
	public String getAuthType() {
		return this.original.getAuthType();
	}

	/**
	 * The default behavior of this method is to return getCookies() on the
	 * wrapped original object.
	 */
	@Override
	public Cookie[] getCookies() {
		return this.original.getCookies();
	}

	/**
	 * The default behavior of this method is to return getDateHeader(String
	 * name) on the wrapped original object.
	 */
	@Override
	public long getDateHeader(String name) {
		return this.original.getDateHeader(name);
	}

	/**
	 * The default behavior of this method is to return getHeader(String name)
	 * on the wrapped original object.
	 */
	@Override
	public String getHeader(String name) {
		return this.original.getHeader(name);
	}

	/**
	 * The default behavior of this method is to return getHeaders(String name)
	 * on the wrapped original object.
	 */
	@Override
	public Enumeration getHeaders(String name) {
		return this.original.getHeaders(name);
	}

	/**
	 * The default behavior of this method is to return getHeaderNames() on the
	 * wrapped original object.
	 */

	@Override
	public Enumeration getHeaderNames() {
		return this.original.getHeaderNames();
	}

	/**
	 * The default behavior of this method is to return getIntHeader(String
	 * name) on the wrapped original object.
	 */

	@Override
	public int getIntHeader(String name) {
		return this.original.getIntHeader(name);
	}

	/**
	 * The default behavior of this method is to return getMethod() on the
	 * wrapped original object.
	 */
	@Override
	public String getMethod() {
		return this.original.getMethod();
	}

	/**
	 * The default behavior of this method is to return getPathInfo() on the
	 * wrapped original object.
	 */
	@Override
	public String getPathInfo() {
		return this.original.getPathInfo();
	}

	/**
	 * The default behavior of this method is to return getPathTranslated() on
	 * the wrapped original object.
	 */

	@Override
	public String getPathTranslated() {
		return this.original.getPathTranslated();
	}

	/**
	 * The default behavior of this method is to return getContextPath() on the
	 * wrapped original object.
	 */
	@Override
	public String getContextPath() {
		return this.original.getContextPath();
	}

	/**
	 * The default behavior of this method is to return getQueryString() on the
	 * wrapped original object.
	 */
	@Override
	public String getQueryString() {
		return this.original.getQueryString();
	}

	/**
	 * The default behavior of this method is to return getRemoteUser() on the
	 * wrapped original object.
	 */
	@Override
	public String getRemoteUser() {
		return this.original.getRemoteUser();
	}

	/**
	 * The default behavior of this method is to return isUserInRole(String
	 * role) on the wrapped original object.
	 */
	@Override
	public boolean isUserInRole(String role) {
		return this.original.isUserInRole(role);
	}

	/**
	 * The default behavior of this method is to return getUserPrincipal() on
	 * the wrapped original object.
	 */
	@Override
	public java.security.Principal getUserPrincipal() {
		return this.original.getUserPrincipal();
	}

	/**
	 * The default behavior of this method is to return getRequestedSessionId()
	 * on the wrapped original object.
	 */
	@Override
	public String getRequestedSessionId() {
		return this.original.getRequestedSessionId();
	}

	/**
	 * The default behavior of this method is to return getRequestURI() on the
	 * wrapped original object.
	 */
	@Override
	public String getRequestURI() {
		return this.original.getRequestURI();
	}

	/**
	 * The default behavior of this method is to return getRequestURL() on the
	 * wrapped original object.
	 */
	@Override
	public StringBuffer getRequestURL() {
		return this.original.getRequestURL();
	}

	/**
	 * The default behavior of this method is to return getServletPath() on the
	 * wrapped original object.
	 */
	@Override
	public String getServletPath() {
		return this.original.getServletPath();
	}

	/**
	 * The default behavior of this method is to return getSession(boolean
	 * create) on the wrapped original object.
	 */
	@Override
	public HttpSession getSession(boolean create) {
		return this.original.getSession(create);
	}

	/**
	 * The default behavior of this method is to return getSession() on the
	 * wrapped original object.
	 */
	@Override
	public HttpSession getSession() {
		return this.original.getSession();
	}

	/**
	 * The default behavior of this method is to return
	 * isRequestedSessionIdValid() on the wrapped original object.
	 */

	@Override
	public boolean isRequestedSessionIdValid() {
		return this.original.isRequestedSessionIdValid();
	}

	/**
	 * The default behavior of this method is to return
	 * isRequestedSessionIdFromCookie() on the wrapped original object.
	 */
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return this.original.isRequestedSessionIdFromCookie();
	}

	/**
	 * The default behavior of this method is to return
	 * isRequestedSessionIdFromURL() on the wrapped original object.
	 */
	@Override
	public boolean isRequestedSessionIdFromURL() {
		return this.original.isRequestedSessionIdFromURL();
	}

	/**
	 * The default behavior of this method is to return
	 * isRequestedSessionIdFromUrl() on the wrapped original object.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return this.original.isRequestedSessionIdFromUrl();
	}

	@Override
	public String getCharacterEncoding() {
		return this.original.getCharacterEncoding();
	}

	/**
	 * The default behavior of this method is to set the character encoding on
	 * the wrapped original object.
	 */

	@Override
	public void setCharacterEncoding(String enc)
			throws java.io.UnsupportedEncodingException {
		this.original.setCharacterEncoding(enc);
	}

	/**
	 * The default behavior of this method is to return getContentLength() on
	 * the wrapped original object.
	 */

	@Override
	public int getContentLength() {
		return this.original.getContentLength();
	}

	/**
	 * The default behavior of this method is to return getContentType() on the
	 * wrapped original object.
	 */
	@Override
	public String getContentType() {
		return this.original.getContentType();
	}

	/**
	 * The default behavior of this method is to return getInputStream() on the
	 * wrapped original object.
	 */

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return this.original.getInputStream();
	}

	/**
	 * The default behavior of this method is to return getParameter(String
	 * name) on the wrapped original object.
	 */

	@Override
	public String getParameter(String name) {
		return this.original.getParameter(name);
	}

	/**
	 * The default behavior of this method is to return getParameterMap() on the
	 * wrapped original object.
	 */
	@Override
	public Map getParameterMap() {
		return this.original.getParameterMap();
	}

	/**
	 * The default behavior of this method is to return getParameterNames() on
	 * the wrapped original object.
	 */

	@Override
	public Enumeration getParameterNames() {
		return this.original.getParameterNames();
	}

	/**
	 * The default behavior of this method is to return
	 * getParameterValues(String name) on the wrapped original object.
	 */
	@Override
	public String[] getParameterValues(String name) {
		return this.original.getParameterValues(name);
	}

	/**
	 * The default behavior of this method is to return getProtocol() on the
	 * wrapped original object.
	 */

	@Override
	public String getProtocol() {
		return this.original.getProtocol();
	}

	/**
	 * The default behavior of this method is to return getScheme() on the
	 * wrapped original object.
	 */

	@Override
	public String getScheme() {
		return this.original.getScheme();
	}

	/**
	 * The default behavior of this method is to return getServerName() on the
	 * wrapped original object.
	 */
	@Override
	public String getServerName() {
		return this.original.getServerName();
	}

	/**
	 * The default behavior of this method is to return getServerPort() on the
	 * wrapped original object.
	 */

	@Override
	public int getServerPort() {
		return this.original.getServerPort();
	}

	/**
	 * The default behavior of this method is to return getReader() on the
	 * wrapped original object.
	 */

	@Override
	public BufferedReader getReader() throws IOException {
		return this.original.getReader();
	}

	/**
	 * The default behavior of this method is to return getRemoteAddr() on the
	 * wrapped original object.
	 */

	@Override
	public String getRemoteAddr() {
		return this.original.getRemoteAddr();
	}

	/**
	 * The default behavior of this method is to return getRemoteHost() on the
	 * wrapped original object.
	 */

	@Override
	public String getRemoteHost() {
		return this.original.getRemoteHost();
	}

	/**
	 * The default behavior of this method is to return getLocales() on the
	 * wrapped original object.
	 */

	@Override
	public Enumeration getLocales() {
		return this.original.getLocales();
	}

	/**
	 * The default behavior of this method is to return isSecure() on the
	 * wrapped original object.
	 */

	@Override
	public boolean isSecure() {
		return this.original.isSecure();
	}

	/**
	 * The default behavior of this method is to return
	 * getRequestDispatcher(String path) on the wrapped original object.
	 */

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return this.original.getRequestDispatcher(path);
	}

	/**
	 * The default behavior of this method is to return getRealPath(String path)
	 * on the wrapped original object.
	 */

	@SuppressWarnings("deprecation")
	@Override
	public String getRealPath(String path) {
		return this.original.getRealPath(path);
	}

	/**
	 * The default behavior of this method is to return getRemotePort() on the
	 * wrapped original object.
	 * 
	 * @since 2.4
	 */
	@Override
	public int getRemotePort() {
		return this.original.getRemotePort();
	}

	/**
	 * The default behavior of this method is to return getLocalName() on the
	 * wrapped original object.
	 * 
	 * @since 2.4
	 */
	@Override
	public String getLocalName() {
		return this.original.getLocalName();
	}

	/**
	 * The default behavior of this method is to return getLocalAddr() on the
	 * wrapped original object.
	 * 
	 * @since 2.4
	 */
	@Override
	public String getLocalAddr() {
		return this.original.getLocalAddr();
	}

	/**
	 * The default behavior of this method is to return getLocalPort() on the
	 * wrapped original object.
	 * 
	 * @since 2.4
	 */
	@Override
	public int getLocalPort() {
		return this.original.getLocalPort();
	}

	@Override
	public Locale getLocale() {
		return original.getLocale();
	}

}
