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

import com.caibowen.gplume.web.IStrViewResolver;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.stateful.StateGen;

import java.lang.invoke.MethodHandle;


/**
 * String jss(State s, RequestContext ctx);
 * String jss(State s);
 *  
 * @author BowenCai
 *
 */
public class StrStatefulAction extends SimpleStatefulAction  {

	private static final long serialVersionUID = 5126077573601786405L;

	protected final boolean hasRequest;
    protected final IStrViewResolver viewResolver;

	public StrStatefulAction(String u
                            , MethodHandle handle
                            , StateGen g
                            , boolean hasRequestContext
                            , IStrViewResolver resolver) {

		super(u, handle, g);
		this.hasRequest = hasRequestContext;
        this.viewResolver = resolver;
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
            viewResolver.resolve(context, (String)o);
	}



}
