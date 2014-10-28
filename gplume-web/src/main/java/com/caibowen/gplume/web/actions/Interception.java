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
package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.web.IAction;
import com.caibowen.gplume.web.RequestContext;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * 
 * @author BowenCai
 *
 */
public class Interception extends SimpleAction {
	
	private static final long serialVersionUID = 254538927443500914L;

	public Interception(String u, MethodHandle handle, Method _m) {
		super(u, handle, _m);
	}
	
	public void intercept(RequestContext requestContext, IAction action) throws Throwable {
		methodHandle.invoke(requestContext, action);
	}
}
