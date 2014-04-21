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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.caibowen.gplume.web.HttpMethod;



/**
 * 
 * to identify a HTTP request handler(function)
 * 
 * @author BowenCai
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Handle {
	
	/**
	 * indicate the number of request per minutes, approximately.
	 * 
	 */
	int freq() default 0;
	
	String[] value();

	HttpMethod[] httpMethods() default {HttpMethod.GET};
	
}
