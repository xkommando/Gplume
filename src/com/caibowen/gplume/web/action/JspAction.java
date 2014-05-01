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

import com.caibowen.gplume.web.RequestContext;


/**
 * 
 * @author BowenCai
 *
 */
public class JspAction extends Action {

	private static final long serialVersionUID = -5228310514106204080L;
	
	public JspAction(String u, MethodHandle handle) {
		super(u, handle);
	}
	
	@Override
	public void perform(RequestContext context) throws Throwable {
		context.putAttr(ACTION_NAME, this);
		String jsp = (String)methodHandle.invoke(context);
		context.render(jsp);
	}

}
