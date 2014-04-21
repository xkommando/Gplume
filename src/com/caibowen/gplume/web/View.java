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

import java.io.PrintWriter;


/**
 * 
 * @author BowenCai
 *
 */
public interface View {
	
	
	public void resolve(RequestContext context);


	public static final String XML = "application/xml";
	public static final String RSS = "application/rss+xml";
	public static final String ATOM = "application/atom+xml";
	public static final String JSON = "application/json";
	
	
	public static class build{
		public static TextView textView(String encoding, String type) {
			return new TextView(encoding, type);
		}
	}
	
	public static class TextView implements View {
		
		private final String encoding;
		private final String type;
		public TextView(String encoding, String type) {
			this.encoding = encoding;
			this.type = type;
		}
		private String content;
		public void setContent(String content) {
			this.content = content;
		}
		
		@Override
		public void resolve(RequestContext context) {
			try {
				context.response.setContentType(type);
				context.response.setCharacterEncoding(encoding);
				PrintWriter writer = context.response.getWriter();
				writer.write(content);
			} catch (Exception e) {
				throw new RuntimeException("Error writing JSP", e);
			}
		}

	}
}
