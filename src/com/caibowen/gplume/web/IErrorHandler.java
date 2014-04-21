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
 * handling HTTP error, mainly 404 and 500
 * 
 * this interface is set for customized HTTP error handling
 * 
 * @author BowenCai
 * 
 * @see com.caibowen.gplume.web.ControlCenter
 * @see com.caibowen.gplume.web.misc.DefaultErrorHandler
 */
public interface IErrorHandler {


	public void http403(RequestContext requestContext);
	
	public void http404(RequestContext requestContext);
	
	public void http500(RequestContext requestContext);
}
