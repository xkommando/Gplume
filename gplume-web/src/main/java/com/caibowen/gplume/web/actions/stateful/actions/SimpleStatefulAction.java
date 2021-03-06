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
package com.caibowen.gplume.web.actions.stateful.actions;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.SimpleAction;
import com.caibowen.gplume.web.actions.stateful.StateGen;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;


/**
 * void ss(MyState s, RequestContext ctx)
 * return void, must has RequestContext
 * @author BowenCai
 *
 */
public class SimpleStatefulAction extends SimpleAction {

	private static final long serialVersionUID = -8968416219169871432L;
	protected final StateGen gen;
	
	public SimpleStatefulAction(String u, MethodHandle handle, Method _m, StateGen g) {
		super(u, handle, _m);
		this.gen = g;
	}
	
	@Override
	public void perform(RequestContext req) throws Throwable {
		req.putAttr(ACTION_NAME, this);
		Object state = gen.gen(req);
		methodHandle.invoke(state, req);
	}

}
