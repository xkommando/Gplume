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
package com.caibowen.gplume.misc.test.jsp;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

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
public class Output implements HttpServletResponse {

	StringWriter writer;

    public Output() {
        writer = new StringWriter(1024);
    }

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

    /**
     * servlet 2.4+
     * @param arg0
     */
	public void setCharacterEncoding(String arg0) {}

	@Override
	public void setContentLength(int arg0) {}

	@Override
	public void setContentType(String arg0) {}
	
	@Override
	public String getCharacterEncoding() {return null; }

    /**
     * servlet 2.4+
     * @return
     */
	public String getContentType() {return null;}
	
	@Override
	public void flushBuffer() throws IOException {}
	
	@Override
	public int getBufferSize() { return 0x7fffffff; }


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

	//--
	@Override
	public String encodeURL(String url) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		return null;
	}

	@Override
	public String encodeUrl(String url) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {

	}

	@Override
	public void sendError(int sc) throws IOException {

	}

	@Override
	public void sendRedirect(String location) throws IOException {

	}

	@Override
	public void setDateHeader(String name, long date) {

	}

	@Override
	public void setHeader(String name, String value) {

	}

	@Override
	public void setIntHeader(String name, int value) {

	}

	@Override
	public void setStatus(int sc) {

	}

	@Override
	public void setStatus(int sc, String sm) {

	}

	@Override
	public void setLocale(Locale loc) {

	}

	@Override
	public Locale getLocale() {
		return null;
	}
}
