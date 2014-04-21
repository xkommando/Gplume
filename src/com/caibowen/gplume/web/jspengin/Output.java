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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * wrapper for HttpServletResponse.
 * 
 * redirect all out put to the ByteArrayOutputStream
 *  so that we can retrieve the content without affecting the real Request and Response
 * 
 * 
 * @author BowenCai
 *
 */
public class Output implements HttpServletResponse{

	StringWriter writer = new StringWriter(1024);
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				writer.write(b);
			}
		};
	}
	
	@Override
	public String toString() {
		return writer.toString();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(writer);
	}

	@Override
	public void reset() {}

	@Override
	public void resetBuffer() {}

//-----------------------------------------------------------------------------
	@Override
	public boolean isCommitted() {
		return false;
	}
	@Override
	public void setBufferSize(int arg0) {}

	@Override
	public void setCharacterEncoding(String arg0) {}

	@Override
	public void setContentLength(int arg0) {}

	@Override
	public void setContentType(String arg0) {}
	
	@Override
	public String getCharacterEncoding() {return null; }

	@Override
	public String getContentType() {return null;}
	
	@Override
	public void flushBuffer() throws IOException {}
	
	@Override
	public int getBufferSize() { return 0x7fffffff; }
	
	@Override
	public void setLocale(Locale arg0) {}
	@Override
	public Locale getLocale() { return null;}
	
	@Override
	public void addCookie(Cookie arg0) {}

	@Override
	public void addDateHeader(String arg0, long arg1) {}

	@Override
	public void addHeader(String arg0, String arg1) {}

	@Override
	public void addIntHeader(String arg0, int arg1) {}

	@Override
	public boolean containsHeader(String arg0) {return false;}

	@Override
	public String encodeRedirectURL(String arg0) {return null;}

	@Override
	public String encodeRedirectUrl(String arg0) {return null;}

	@Override
	public String encodeURL(String arg0) {return null;}

	@Override
	public String encodeUrl(String arg0) {return null;}

	@Override
	public void sendError(int arg0) throws IOException {}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {}

	@Override
	public void sendRedirect(String arg0) throws IOException {}

	@Override
	public void setDateHeader(String arg0, long arg1) {}

	@Override
	public void setHeader(String arg0, String arg1) {}

	@Override
	public void setIntHeader(String arg0, int arg1) {}

	@Override
	public void setStatus(int arg0) {}
	@Override
	public void setStatus(int arg0, String arg1) {}

}
