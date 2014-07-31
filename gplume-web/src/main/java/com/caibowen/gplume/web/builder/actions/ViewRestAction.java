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

import java.lang.reflect.Method;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.PathValResolver;
import com.caibowen.gplume.web.view.IView;

/**
 * 
 * web handle returns View
 * @author BowenCai
 *
 */
public class ViewRestAction extends RestAction {

	private static final long serialVersionUID = -2365881998002360893L;
	
	protected final Method method;
	protected final Object controller;

	protected final boolean hasRequest;
	
	public ViewRestAction(String uri,  Method m, Object ctrl,
			boolean inM, boolean req, PathValResolver pr) {
		super(uri, null, inM, pr);
		hasRequest = req;
		method = m;
		controller = ctrl;
	}

	@Override
	public void perform(RequestContext context) throws Throwable {
		Object var = resolver.resolveAndCast(context.path, null);
		context.putAttr(ACTION_NAME, this);
		Object v = null;
		if (inMethodCall) {
			v = method.invoke(controller, var, context);
		} else {
			context.putAttr(resolver.getArgName(), var);
			v = method.invoke(controller, var);
		}
		if (v != null) {
			((IView)v).resolve(context);
		}
	}

}
