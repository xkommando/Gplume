package com.caibowen.gplume.web.taglib;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import com.caibowen.gplume.misc.Str;

public class TagLink extends WriterTag {

	private static final Logger THIS_LOG = Logger.getLogger(TagLink.class.getName());

	private String displayName;
	private String nativeName;
	private String url;
	private String target;
	
	@Override
	public String write(Writer writer) throws IOException {
		
		if (displayName != null && nativeName != null) {
			return "dunplicated display name: displayName["
					+ displayName + "] native name[" + nativeName + "]";
		}
		// nullable
//		else if (displayName == null && name == null) {
//			return "null display name and  native name";
//		}
		
		boolean isNative = nativeName != null;
		String testName = isNative ? nativeName : displayName;
		
		// more validation in test mode
		if (TEST_MODE) {
			if (!Str.Utils.notBlank(testName)) {
				THIS_LOG.warning("url[" + url + "] name is empty");
			}
			if (!Str.Utils.notBlank(target)) {
				THIS_LOG.warning("target field is empty");
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
//		link.setDisplayName("name");
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
