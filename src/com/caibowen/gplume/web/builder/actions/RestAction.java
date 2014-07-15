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
import com.caibowen.gplume.web.builder.PathValResolver;

/**
 * 
 * 
 * public void action(Date date, RequestContext context) {
 * 		context.render("xxx.jsp");
 * }
 * returns void, must has requestContext
 * @author BowenCai
 *
 */
public class RestAction extends SimpleAction {
	
	private static final long serialVersionUID = 7479824844662522176L;

	final PathValResolver resolver;
	final boolean inMethodCall;
	
	public RestAction(String uri, MethodHandle handle, boolean call, PathValResolver pr) {
		
		super(uri, handle);
		resolver = pr;
		inMethodCall = call;
	}
	
	@Override
	public void perform(RequestContext requestContext) throws Throwable {
		
		Object var = resolver.resolveAndCast(requestContext.path, null);
		
		requestContext.putAttr(ACTION_NAME, this);

		if (inMethodCall) {
			methodHandle.invoke(var, requestContext);
		} else {
			requestContext.putAttr(resolver.getArgName(), var);
			methodHandle.invoke(requestContext);
		}
	}
	

}
