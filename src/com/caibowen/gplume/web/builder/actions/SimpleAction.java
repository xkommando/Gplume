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
package com.caibowen.gplume.web.builder.actions;

import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.IAction;


/**
 * 
 * fixed path non-binding action
 * @author BowenCai
 *
 */
public class SimpleAction implements IAction {

	private static final long serialVersionUID = 5581602597667250526L;
	
	/**
	 * uri for mapping
	 */
	protected final String				effectiveURI;
	
	/**
	 * controller object shall be binded to this handle if it is not a static function
	 */
	protected final MethodHandle		methodHandle;
	
	public SimpleAction(String u, MethodHandle handle) {
		effectiveURI = u;
		this.methodHandle = handle;
	}
	
	@Override
	public void perform(RequestContext requestContext) throws Throwable {
		
		requestContext.putAttr(ACTION_NAME, this);
		methodHandle.invoke(requestContext);
	}

	@Override
	public MethodHandle getMethodHandle() {
		return methodHandle;
	}
	
	@Override
	public String getEffectiveURI() {
		return effectiveURI;
	}
}