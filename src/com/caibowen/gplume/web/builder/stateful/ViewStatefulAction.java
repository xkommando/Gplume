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
package com.caibowen.gplume.web.builder.stateful;

import java.lang.reflect.Method;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.ViewAction;
import com.caibowen.gplume.web.view.IView;


/**
 * 
 * IView p(State s, RequestContext ctn);
 * IView p(State s);
 * 
 * @author BowenCai
 *
 */
public class ViewStatefulAction extends ViewAction {

	private static final long serialVersionUID = -1318446478047269626L;

	protected final StateGen gen;
	
	public ViewStatefulAction(String u, Method m, Object ctrl, boolean hasReq, StateGen g) {
		super(u, m, ctrl, hasReq);
		this.gen = g;
	}
	
	@Override
	public void perform(RequestContext context) throws Throwable {
		context.putAttr(ACTION_NAME, this);
		Object state = gen.gen(context);
		Object v = null;
		if (hasRequest)
			v = method.invoke(controller, state, context);
		else
			v = method.invoke(controller, state);
		
		if (v != null) {
			((IView)v).resolve(context);
		}
	}

	
}
