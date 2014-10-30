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
package com.caibowen.gplume.misc;

import javax.annotation.Nullable;
import java.util.Locale;


/**
 * 
 * @author BowenCai
 *
 */
public class Locales {

    public static Locale parseLocale(String str) {
        if (str == null) {
            return null;
        }
        if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
        	return null;
        }
        final int len = str.length();
        if (len < 2) {
        	return null;
        }
        final char ch0 = str.charAt(0);
        if (ch0 == '_') {
            if (len < 3) {
            	return null;
            }
            final char ch1 = str.charAt(1);
            final char ch2 = str.charAt(2);
            if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
            	return null;
            }
            if (len == 3) {
            	return null;
            }
            if (len < 5) {
            	return null;
            }
            if (str.charAt(3) != '_') {
            	return null;
            }
            return new Locale("", str.substring(1, 3), str.substring(4));
        }
        final char ch1 = str.charAt(1);
        if (!Character.isLowerCase(ch0) || !Character.isLowerCase(ch1)) {
        	return null;
        }
        if (len == 2) {
            return new Locale(str);
        }
        if (len < 5) {
        	return null;
        }
        if (str.charAt(2) != '_') {
        	return null;
        }
        final char ch3 = str.charAt(3);
        if (ch3 == '_') {
            return new Locale(str.substring(0, 2), "", str.substring(4));
        }
        final char ch4 = str.charAt(4);
        if (!Character.isUpperCase(ch3) || !Character.isUpperCase(ch4)) {
        	return null;
        }
        if (len == 5) {
            return new Locale(str.substring(0, 2), str.substring(3, 5));
        }
        if (len < 7) {
        	return null;
        }
        if (str.charAt(5) != '_') {
        	return null;
        }
        return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
    }
    

    /**
     * 
     * @param acceptedLang 'zh_CN' 'en'
     * @return
     */
    @Nullable
	public static Locale fastParseLocale(String acceptedLang) {

		acceptedLang = acceptedLang.trim();
		
		if (acceptedLang.startsWith("zh")
				|| acceptedLang.startsWith("ZH")) {

			if (acceptedLang.length() > 4) {
				char c2 = acceptedLang.charAt(2);
				char c3 = acceptedLang.charAt(3);
				if ((c2 == '-') // zh-TW or zh-HK
					&& ( (c3 == 'T' || c3 == 't') 
						|| (c3 == 'H' || c3 == 'h')) ) {

					return Locale.TRADITIONAL_CHINESE;
				}
			}
			return Locale.SIMPLIFIED_CHINESE;
			
		} else if (acceptedLang.startsWith("en")
					|| acceptedLang.startsWith("EN")) {
			return Locale.ENGLISH;
			
		}  else {
			return null;
		}
	}
    

    //-----------------------------------------------------------------------
    /**
     * <p>Converts a String to a Locale.</p>
     *
     * <p>This method takes the string format of a locale and creates the
     * locale object from it.</p>
     *
     * <pre>
     *   LocaleUtils.toLocale("")           = new Locale("", "")
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>(#) The behavior of the JDK variant constructor changed between JDK1.3 and JDK1.4.
     * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
     * Thus, the result from getVariant() may vary depending on your JDK.</p>
     *
     * <p>This method validates the input strictly.
     * The language code must be lowercase.
     * The country code must be uppercase.
     * The separator must be an underscore.
     * The length must be correct.
     * </p>
     *
     * @param str  the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws IllegalArgumentException if the string is an invalid format
     * @see Locale#forLanguageTag(String)
     */
    public static Locale toLocale(final String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) { // LANG-941 - JDK 8 introduced an empty locale where all fields are blank
            return new Locale("", "");
        }
        if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final int len = str.length();
        if (len < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final char ch0 = str.charAt(0);
        if (ch0 == '_') {
            if (len < 3) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            final char ch1 = str.charAt(1);
            final char ch2 = str.charAt(2);
            if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (len == 3) {
                return new Locale("", str.substring(1, 3));
            }
            if (len < 5) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (str.charAt(3) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale("", str.substring(1, 3), str.substring(4));
        }
        
        String[] split = str.split("_", -1);
        int occurrences = split.length -1;
        switch (occurrences) {
            case 0:
                if (Str.Utils.isAllLowerCase(str) && (len == 2 || len == 3)) {
                    return new Locale(str);
                } else {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                
            case 1:
                if (Str.Utils.isAllLowerCase(split[0]) &&
                    (split[0].length() == 2 || split[0].length() == 3) &&
                     split[1].length() == 2 && Str.Utils.isAllUpperCase(split[1])) {
                    return new Locale(split[0], split[1]);
                } else {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }

            case 2:
                if (Str.Utils.isAllLowerCase(split[0]) && 
                    (split[0].length() == 2 || split[0].length() == 3) &&
                    (split[1].length() == 0 || (split[1].length() == 2 && Str.Utils.isAllUpperCase(split[1]))) &&
                     split[2].length() > 0) {
                    return new Locale(split[0], split[1], split[2]);
                }

                //$FALL-THROUGH$
            default:
                throw new IllegalArgumentException("Invalid locale format: " + str);
        }
    }
}
