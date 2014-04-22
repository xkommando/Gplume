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
