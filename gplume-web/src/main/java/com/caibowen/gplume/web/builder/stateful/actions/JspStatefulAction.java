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
package com.caibowen.gplume.web.builder.stateful.actions;

import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.stateful.StateGen;


/**
 * String jss(State s, RequestContext ctx);
 * String jss(State s);
 *  
 * @author BowenCai
 *
 */
public class JspStatefulAction extends SimpleStatefulAction  {

	private static final long serialVersionUID = 5126077573601786405L;

	protected final boolean hasRequest;
	public JspStatefulAction(String u, MethodHandle handle, StateGen g, boolean hasRequestContext) {
		super(u, handle, g);
		this.hasRequest = hasRequestContext;
	}
	
	@Override
	public void perform(RequestContext context) throws Throwable {
		context.putAttr(ACTION_NAME, this);
		Object state = gen.gen(context);
		
		Object o = null;
		if (hasRequest)
			o = methodHandle.invoke(state, context);
		else
			o = methodHandle.invoke(state);
		
		if(o != null)
			context.render((String)o);
		/**
		 * o must be String, this has been check in the construction of this action
		 */
	}



}
