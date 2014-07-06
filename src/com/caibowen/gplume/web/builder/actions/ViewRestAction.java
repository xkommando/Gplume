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

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.View;

/**
 * 
 * web handle returns View
 * @author BowenCai
 *
 */
public class ViewRestAction extends RestAction {

	private static final long serialVersionUID = -2365881998002360893L;
	private final Method method;
	private final Object controller;
	public ViewRestAction(String uri,  Method m, Object ctrl, int start,
			String name, Class<?> type, String s, boolean inM) {
		super(uri, null, start, name, type, s, inM);
		method = m;
		controller = ctrl;
	}

	@Override
	public void perform(RequestContext context) throws Throwable {
		Object var = Converter.slient.translateStr(parseArg(context.path), argType);
		context.putAttr(ACTION_NAME, this);
		Object v = null;
		if (inMethodCall) {
			v = method.invoke(controller, var, context);
		} else {
			context.putAttr(argName, var);
			v = method.invoke(controller, var);
		}
		if (v != null) {
			((View)v).resolve(context);
		}
	}

}
