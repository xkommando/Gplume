package com.caibowen.gplume.util;

import java.io.IOException;
import java.util.regex.Pattern;



public class Str {

	public static class Patterns{

		public static final Pattern POST_URI_TITLE = Pattern.compile(
				"^[A-Za-z][A-Za-z0-9_.-]{3,200}$");
		
		public static final Pattern ENTITY_FIELD_NAME = Pattern.compile(
				"^[A-Za-z][A-Za-z0-9_]{3,500}$");
	
		public static final Pattern  URL = Pattern.compile(
				"^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
				Pattern.CASE_INSENSITIVE);

		public static final Pattern ASCII = Pattern.compile("\\A\\p{ASCII}*\\z");
		

		public static final Pattern PART_URI = Pattern.compile(
			"^/(([\\w\\-]+|\\{([a-zA-Z][\\w]*)\\})(;*)/?)+(\\.\\w+$)?|^/$");
	}
	
	public static class Util {
		
	}
	
	public static void main(String[] args) throws IOException {

//		String uri = "/123sd-reg_sdg-sdg8_456789-sdjifg.html*****";
//		System.out.println(valideURI(uri));
////		System.out.println(uri.substring(0, len));
//		if (uri.charAt(len - 1) != '*') {
//			
//			if (!StrUtil.valideURI(uri)) {
//				throw new IllegalArgumentException("illegal url[" + uri + "]");
//			}
//			
//		} else {
//			System.out.println(uri.substring(0, len - 1));
//			if (!StrUtil.valideURI(uri.substring(0, len - 1))) {
//				System.out.println("error");
//				throw new IllegalArgumentException("illegal url[" + uri + "]");
//			}
//		}
	}
	
	private Str(){}
//	public static String make(final String str) {
//		
//		return str;
//	}
//	public static String join(List<String> strls) {
//		
//		StringBuilder builder = new StringBuilder(8 * strls.size());
//		for (String string : strls) {
//			builder.append(string).append(';');
//		}
//		builder.deleteCharAt(builder.length() - 1);
//		return builder.toString();
//	}
//	public static List<String> splite(final String string) {
//		return splite(string, ";");
//	}
//	public static List<String> splite(final String string, final String delim) {
//		
//		return Arrays.asList(string.split(delim));
//	}
}
