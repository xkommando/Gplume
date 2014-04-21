/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.misc;

public class HtmlUtils {


    public static String escape(CharSequence text,
                                    int start, int end) {
    	StringBuilder out = new StringBuilder(text.length() * 3 / 2);
    	escapeTo(out, text, start, end);
    	return out.toString();
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
    public static void escapeTo(StringBuilder out, CharSequence text,
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
