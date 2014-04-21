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

import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.web.action.Action;
/**
 * 
 * @author BowenCai
 *
 */
public class Interception extends Action {
	
	private static final long serialVersionUID = 254538927443500914L;

	public Interception(String u, MethodHandle handle) {
		super(u, handle);
	}
	
	public void intercept(RequestContext requestContext, Action action) throws Throwable {
		methodHandle.invoke(requestContext, action);
	}
}
