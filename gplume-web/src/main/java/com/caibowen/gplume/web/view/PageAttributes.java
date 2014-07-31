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
package com.caibowen.gplume.web.view;


/**
 * constants for HTTP attributes
 * 
 * @author BowenCai
 *
 */
public interface PageAttributes {

	static class Type {
		static final String TEXT = "application/text";
		static final String XML = "application/xml";
		static final String RSS = "application/rss+xml";
		static final String ATOM = "application/atom+xml";
		static final String JSON = "application/json";
	}

	static class Encoding {
		static final String UTF_8 = "utf-8";
		static final String UTF_16 = "utf-16";
		static final String UTF_32 = "utf-32";
		static final String ISO_8859_1 = "ISO-8859-1";
	}
}
