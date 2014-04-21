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
package com.caibowen.gplume.web;


/**
 * 
 * @author BowenCai
 *
 */
public enum HttpMethod {

	GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;
	
	public static final HttpMethod[] ALL = {GET, POST, PUT, PATCH, DELETE, OPTIONS, TRACE};
	public static final HttpMethod[] GET_POST = {GET, POST};
	
    public static HttpMethod lookup(CharSequence seq) {
    	
        switch(seq.charAt(0)) {
        	case 'g' :
        	case 'G' :
        		return GET;
        	case 'P' :
        	case 'p' :
        		char ch = seq.charAt(1);
        		return ch == 'o' || ch == 'O' ? POST : (ch == 'u' || ch == 'U' ? PUT : PATCH);
        	case 'h' :
        	case 'H' :
        		return HEAD;
        	case 'o' :
        	case 'O' : 
        		return OPTIONS;
        	case 'd' :
        	case 'D' : 
        		return DELETE;
        	default : 
        		return TRACE;
        }
    }
}
