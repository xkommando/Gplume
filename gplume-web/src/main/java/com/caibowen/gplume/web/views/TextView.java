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
package com.caibowen.gplume.web.views;

public class TextView {

	final String encoding;
	final String type;

    String content;

	public TextView() {
		this.encoding = PageAttributes.Encoding.UTF_8;
		this.type = PageAttributes.Type.TEXT;
	}
	
	public TextView(String encoding, String type) {
		this.encoding = encoding;
		this.type = type;
	}


	public TextView setContent(String content) {
		this.content = content;
		return this;
	}

}
