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
package com.caibowen.gplume.web;


/**
 * 
 * @author BowenCai
 *
 */
public enum HttpMethod {

	GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;
	
//	public static final HttpMethod[] ALL = {GET, POST, PUT, PATCH, DELETE, OPTIONS, TRACE};
//	public static final HttpMethod[] GET_POST = {GET, POST};
	
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
