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

import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.IStrViewResolver;


/**
 * web handle returning String
 * 
 * return "index.jsp";
 * 
 * @author BowenCai
 *
 */
public class StrAction extends SimpleAction {

	private static final long serialVersionUID = -5228310514106204080L;
	
	protected final boolean hasRequest;
    protected final IStrViewResolver viewResolver;

	public StrAction(String u
                       , MethodHandle handle
                        , boolean hasRequestContext
                        , IStrViewResolver resolver) {

		super(u, handle);
		this.hasRequest = hasRequestContext;
        this.viewResolver = resolver;
	}
	
	@Override
	public void perform(RequestContext context) throws Throwable {
		context.putAttr(ACTION_NAME, this);
		
		Object o = null;
		if (hasRequest)
			o = methodHandle.invoke(context);
		else
			o = methodHandle.invoke();

        /**
         * o must be String, this has been checked in the construction of this action
         */
		if(o != null)
			viewResolver.resolve(context, (String)o);
	}

}
