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

import com.caibowen.gplume.web.IStrViewResolver;
import com.caibowen.gplume.web.RequestContext;

import java.lang.invoke.MethodHandle;


/**
 * RestAction with method returning JSP views
 * @Handle(...)
 * public action(Date date) {
 * 
 * 		return "index.jsp";
 * }
 * 
 * why another Action class?
 * to speed up!
 * 
 * @author BowenCai
 *
 */
public class StrRestAction extends RestAction {

	private static final long serialVersionUID = -7671427896241639360L;


	protected final boolean hasRequest;
    protected final IStrViewResolver viewResolver;
	
	public StrRestAction(String uri
                        , MethodHandle handle
                        , boolean call
                        , boolean req
                        , PathValResolver pr
                        , IStrViewResolver resolver) {

		super(uri, handle, call, pr);
		this.hasRequest = req;
        this.viewResolver = resolver;
	}

	@Override
	public void perform(RequestContext context) throws Throwable {
		Object var = pathResolver.resolveAndCast(context.path, null);
		context.putAttr(ACTION_NAME, this);
		Object jsp = null;
		if (inMethodCall) {
			if (hasRequest)
				jsp = methodHandle.invoke(var, context);
			else
				jsp = methodHandle.invoke(var);
		} else {
			context.putAttr(pathResolver.getArgName(), var);

			if (hasRequest)
				jsp = methodHandle.invoke(context);
			else
				jsp = methodHandle.invoke();
		}
		
		if (jsp != null) {
			context.render((String)jsp);
		}
	}
}
