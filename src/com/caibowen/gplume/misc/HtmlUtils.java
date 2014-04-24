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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

//	String str = "";
//	Pattern patt = Pattern.compile(str);
//	Matcher matcher = patt.matcher(plain);
//	plain = matcher.replaceAll("<a href=\"$1\">$1</a>");
	private static final Pattern PTN_LINK = Pattern.compile(
			"(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?«»“”‘’]))");

	public static String txtToHtml(CharSequence text,
                                    int start, int end) {
    	StringBuilder out = new StringBuilder(text.length() * 3 / 2);
    	txtToHtml(out, text, start, end);
    	return out.toString();
    }
	
	/**
	 * add <a href></a> to links
	 * 
	 * e.g.
	 * 
	 * look here http://www.caibowen.com/about.html
	 * output
	 * look here <a href="http://www.caibowen.com/about.html" target="_black" rel="nofollow" >
	 * 	http://www.caibowen.com/about.html </a>
	 * @param text
	 * @param start
	 * @param end
	 * @return
	 */
	public static String addHref(CharSequence text, int start, int end) {
    	Matcher linkMatcher = PTN_LINK.matcher(text);
    	return linkMatcher.replaceAll(
    			"<a href=\"$1\" rel=\"nofollow\" target=\"_blank\">$1</a>");
	}
    /**
     * escapt html
     * <pre>
     * \n 		->	<br/>
     * \r\n 	->  <br/>
     * space 	-> &nbsp;
     * <		-> &lt;
     * >		-> &gt;
     * &		-> amp;
     * </pre>
     * @param out
     * @param text
     * @param start
     * @param end
     */
    public static void txtToHtml(StringBuilder out, CharSequence text,
                                    int start, int end) {
    	
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c == '\r') {
				if (i + i < end && text.charAt(i + 1) == '\n') {
					out.append("<br/>\r\n");
					i++;
				}
            } else if (c == '\n') {
				out.append("<br/>\r\n");
            } else if (c > 0x7E || c < ' ') {
                out.append("&#" + ((int) c) + ";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }
                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }
    
	public static long findParagraphs(String content, final int lowerBound, final long def) {

		long out = def;
		
		int left = content.indexOf("<p>", 0);
		int right = -1;

		if (left != -1) {
			int niddle = left + 3;
			right = content.indexOf("</p>", niddle);
			if (right < 0) {
				return out;
			}
			while (right < lowerBound) {
				niddle = content.indexOf("<p>", right + 4);
				if (niddle < 0) {
					return out;
				} else {
					right = content.indexOf("</p>", niddle + 3);
				}
				
				if (right < 0) {
					return out;
				} else {
					continue;
				}
			}
			right += 4; // include the </p>
			out = ((long)left) << 32 | ((long)right) & 0xffffffffL;
			return out;
		}
		return out;
	}

}
