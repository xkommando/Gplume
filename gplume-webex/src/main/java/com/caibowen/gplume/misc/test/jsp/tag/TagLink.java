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
package com.caibowen.gplume.misc.test.jsp.tag;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.misc.Str;

public class TagLink extends WriterTag {

	private static final Logger THIS_LOG = LoggerFactory.getLogger(TagLink.class.getName());

	private String displayName;
	private String nativeName;
	private String url;
	private String target;
	
	@Override
	public String write(JspWriter writer) throws IOException {
		
		if (displayName != null && nativeName != null) {
			return "dunplicated display id: displayName["
					+ displayName + "] native id[" + nativeName + "]";
		}
		// required
//		else if (displayName == null && id == null) {
//			return "null display id and  native id";
//		}
		
		boolean isNative = nativeName != null;
		String testName = isNative ? nativeName : displayName;
		
		// more validation in test mode
		if (TEST_MODE) {
			if (!Str.Utils.notBlank(testName)) {
				THIS_LOG.warn("url[" + url + "] id is empty");
			}
			if (!Str.Utils.notBlank(target)) {
				THIS_LOG.warn("target field is empty");
			}
			if (Str.Utils.notBlank(url)) {
				if (!Str.Patterns.URL.matcher(url).matches()) {
					return " url[" + url + "] does not match["
							+ Str.Patterns.URL.pattern() + "]";
				}
			}
		}
		
		if (Str.Utils.notBlank(url)) {
			if (testName != null) {
				if (isNative) {
					writer.write(getNatives().getStr(testName));
				} else {
					writer.write(testName);
				}
			}
			writer.write("<a href=\"");
			writer.write(url);
			writer.write("\"");
			if (target != null) {
				writer.write(" target=\"");
				writer.write(target);
				writer.write("\"");
			}
			writer.write(">");
			writer.write(url);
			writer.write("</a>");
			return SUCCESS;
		} else {
			return "url is empty";
		}
	}
	
//	public static void main(String...a) {
//		TagLink link = new TagLink();
//		link.setDisplayName("id");
////		link.setTarget("_blank");
//		link.setUrl("http://www.caibowen.com/");
//		link.doTag();
//	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getNativeName() {
		return nativeName;
	}

	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}

	
}
