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
package com.caibowen.gplume.i18n;

import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.caibowen.gplume.misc.Str;


/**
 * Frequently used human dialect on Internet.
 * 
 * @author BowenCai
 *
 */
public enum Dialect {
	
	Unknown(1,"xxx", "xx", "Unknown"),
	
	SimplifiedChinese(2, "zho", "zh", "\u7b80\u4f53\u4e2d\u6587"),
	TraditionalChinese(3, "zho", "zh", "\u7e41\u9ad4\u4e2d\u6587"),
	
	Japanese(5, "jpn", "ja", "\u65e5\u672c\u8a9e"),
	Korean(7, "kor", "ko", "\ud55c\uad6d\uc5b4"),
	Thai(11, "tha", "th", "\u0e1b\u0e23\u0e30\u0e40\u0e17\u0e28\u0e44\u0e17\u0e22"),
	Vietnamese(13, "vie", "vi", "ti\u1ebfng vi\u1ec7t"),

	English(17, "eng", "en", "English"),
	Spanish(19, "spa", "es", "espa\u00f1ol"),
	Portuguese(23, "por", "pt", "Portuguese"),
	German(29,"deu", "de", "Deutsch"),
	French(31, "fra", "fr", "fran\u00e7aise"),
	Dutch(37, "nld", "nl", "Nederlands"),
	Danish(41, "dan", "da", "dansk"),
	Italian(43, "ita", "it", "Italiano"),
	Polish(47, "pol", "pl", "polski"),
	Turkish(51, "tur", "tr", "T\u00fcrk\u00e7e"),
	Russia(53, "rus", "ru", "\u0440\u0443\u0441\u0441\u043a\u0438\u0439"),
	Arabic(57,"ara", "ar", "\u0627\u0644\u0639\u0631\u0628\u064a\u0629");
	
	private Dialect(int code, String i639_2, String i639_1, String nativeNameInASCI) {
		this.code = code;
		this.iso639_1 = i639_1;
		this.iso639_2t = i639_2;
		this.nativeName = nativeNameInASCI;
	}
	
	public static final String NAME = Dialect.class.getName();
	
	public final int code;
	public final String iso639_1;
	public final String iso639_2t;
	public final String nativeName;
	
	private static final HashMap<String, Dialect> ISO639_1_TABLE;
	private static final HashMap<String, Dialect> ISO639_2T_MA_TABLE;

	private static final HashMap<String, Dialect> STR_CODE_TABLE;
	private static final HashMap<Integer, Dialect> INT_CODE_TABLE;
	
	static {
		ISO639_1_TABLE = new HashMap<String, Dialect>(48);
		for (Dialect dia : Dialect.class.getEnumConstants()) {
			ISO639_1_TABLE.put(dia.iso639_1, dia);
		}
		
		ISO639_2T_MA_TABLE = new HashMap<String, Dialect>(48);
		for (Dialect dia : Dialect.class.getEnumConstants()) {
			ISO639_1_TABLE.put(dia.iso639_2t, dia);
		}
		
		STR_CODE_TABLE = new HashMap<>(48);
		for (Dialect d : Dialect.class.getEnumConstants()) {
			STR_CODE_TABLE.put(Integer.toString(d.code), d);
		}
		
		INT_CODE_TABLE = new HashMap<>(48);
		for (Dialect d : Dialect.class.getEnumConstants()) {
			INT_CODE_TABLE.put(d.code, d);
		}
	}
	
	/**
	 * 
	 * @param loc
	 * @return unknown if not found
	 */
	@Nonnull
	public static Dialect fromJdkLocale(Locale loc) {
		Dialect cl = ISO639_2T_MA_TABLE.get(loc.getISO3Language());
		return cl != null ? cl : Dialect.Unknown;
	}
	
	/**
	 * 
	 * @param loc
	 * @return unknown if not found
	 */
	public static Dialect parseISO639_1(String str) {
		if (Str.Utils.notBlank(str)) {
			Dialect cl = ISO639_1_TABLE.get(str);
			return cl != null ? cl : Dialect.Unknown;
		} else {
			throw new NullPointerException(" empty string to parse");
		}
	}
	public static void main(String...a) {
		String head = "nl,es-es;q=0.8,zh-cn;q=0.6,zh-tw;q=0.4,en-us;q=0.2";
		System.out.println(I18nService.resolve(head));
	}
	/**
	 * 
	 * @param code
	 * @return
	 */
	public static Dialect parseCode(String code) {
		if (Str.Utils.notBlank(code)) {
			Dialect cl = STR_CODE_TABLE.get(code);
			return cl != null ? cl : Dialect.Unknown;
		} else {
			throw new NullPointerException(" empty string to parse");
		}
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	public static Dialect parseCode(Integer code) {
		if (code != null) {
			Dialect cl = INT_CODE_TABLE.get(code);
			return cl != null ? cl : Dialect.Unknown;
		} else {
			throw new NullPointerException(" empty string to parse");
		}
	}
	
	/**
	 * 
	 * @param str
	 * @return unknown if not found
	 */
	public static Dialect parseISO639_2t(String str) {
		if (Str.Utils.notBlank(str)) {
			Dialect cl = ISO639_2T_MA_TABLE.get(str);
			return cl != null ? cl : Dialect.Unknown;
		} else {
			throw new NullPointerException(" empty string to parse");
		}
	}
	
}
