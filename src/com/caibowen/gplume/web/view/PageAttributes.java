package com.caibowen.gplume.web.view;



public interface PageAttributes {

	static class Type {
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
