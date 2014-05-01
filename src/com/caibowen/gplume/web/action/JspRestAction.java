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

import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.web.RequestContext;


/**
 * RestAction with method returning JSP view
 * 
 * why another Action class?
 * to speed up!
 * 
 * @author BowenCai
 *
 */
public class JspRestAction extends RestAction {

	private static final long serialVersionUID = -7671427896241639360L;

	public JspRestAction(String uri, MethodHandle handle, int start,
			String name, Class<?> type, String s, boolean call) {
		super(uri, handle, start, name, type, s, call);
	}

	@Override
	public void perform(RequestContext context) throws Throwable {
		Object var = Converter.slient.translateStr(parseArg(context.uri), argType);
		context.putAttr(ACTION_NAME, this);

		if (inMethodCall) {
			String jsp = (String)methodHandle.invoke(var, context);
			context.render(jsp);
			
		} else {
			context.putAttr(argName, var);
			String jsp = (String)methodHandle.invoke(context);
			context.render(jsp);
		}

	}
}
