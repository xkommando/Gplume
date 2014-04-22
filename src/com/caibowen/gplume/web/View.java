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
