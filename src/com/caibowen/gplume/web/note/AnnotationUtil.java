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
