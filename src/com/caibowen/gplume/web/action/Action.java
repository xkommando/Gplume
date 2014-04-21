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
package com.caibowen.gplume.web.action;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.web.RequestContext;


/**
 * Actor = Controller  + one handler function of this controller
 * 
 * each controller has one and only one instance, 
 * whereas the handler of which are many
 * 
 * @author BowenCai
 *
 */
public class Action implements Serializable {

	private static final long serialVersionUID = 5581602597667250526L;

	public static final String ACTION_NAME = Action.class.getName();
	
	/**
	 * uri for mapping
	 */
	protected final String				effectiveURI;
	
	/**
	 * controller object shall be binded to this handle if it is not a static function
	 */
	protected final MethodHandle		methodHandle;
	
	public Action(String u, MethodHandle handle) {
		effectiveURI = u;
		this.methodHandle = handle;
	}
	
	public void perform(RequestContext requestContext) throws Throwable {
		
		requestContext.putAttr(ACTION_NAME, this);
		methodHandle.invoke(requestContext);
	}

	public MethodHandle getMethodHandle() {
		return methodHandle;
	}
	public String getEffectiveURI() {
		return effectiveURI;
	}
}
