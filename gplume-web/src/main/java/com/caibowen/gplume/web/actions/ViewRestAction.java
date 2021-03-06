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

import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;

import java.lang.reflect.Method;

/**
 * 
 * web handle returns View
 *
 * @author BowenCai
 *
 */
public class ViewRestAction extends ViewAction {

	private static final long serialVersionUID = -2365881998002360893L;


    final IPathValResolver pathResolver;
    final boolean inMethodCall;

	protected final Method method;
	protected final Object controller;

	public ViewRestAction(String uri,  Method m, Object ctrl,
			boolean inM, boolean req, IPathValResolver pr, IViewResolver viewResolver1_) {
		super(uri, null, ctrl, req, viewResolver1_);
        pathResolver = pr;
        inMethodCall = inM;
		method = m;
		controller = ctrl;
	}

	@Override
	public void perform(RequestContext context) throws Throwable {
		Object var = pathResolver.resolveAndCast(context.path, null);
		context.putAttr(ACTION_NAME, this);
		Object v = null;
		if (inMethodCall) {
			v = method.invoke(controller, var, context);
		} else {
			context.putAttr(pathResolver.getArgName(), var);
			v = method.invoke(controller, var);
		}
		if (v != null) {
            viewResolver.resolve(context, v);
		}
	}

}
