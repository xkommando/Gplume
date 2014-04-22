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
package com.caibowen.gplume.web.note;

import com.caibowen.gplume.web.HttpMethod;


/**
 * toString functions of each Annotation 
 *  used to generate exception message
 *  
 * @author BowenCai
 *
 */
public class AnnotationUtil {
	
	public static String handleInfo(Handle anno) {
		
		StringBuilder builder = new StringBuilder(64);
		builder.append("URIs{ ");
		for (String uri : anno.value()) {
			builder.append('[').append(uri).append(']').append(' ');
		}
		builder.append('}').append('\n').append("HTTP Method{ ");
		for (HttpMethod m : anno.httpMethods()) {
			builder.append(m).append(' ');
		}
		builder.append('\n');
		
		return builder.toString();
	}
	
	public static String InterceptInfo(Intercept anno) {
		
		StringBuilder builder = new StringBuilder(48);
		builder.append("URIs{ ");
		for (String uri : anno.uri()) {
			builder.append('[').append(uri).append(']').append(' ');
		}
		builder.append('}').append('\n');
		
		return builder.toString();
	}
}
