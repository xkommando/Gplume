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

import com.caibowen.gplume.web.IAction;
import com.caibowen.gplume.web.IView;
import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;


/**
 * 
 * IView p(RequestContext ctn);
 * IView p();
 * 
 * 
 * web handle returns View
 * 
 * ViewAction, including ViewRestAction
 * use java.lang.reflect.Method instead of MethodHandle,
 * for invoking, this is because function that View Action
 * may serve functions that return objects that is derived from the interface View,
 * thus its return types various and cannot be resolved by MethodHandle
 * 
 * @author BowenCai
 *
 */
public class ViewAction implements IAction {

	private static final long serialVersionUID = 2075886979686649253L;
	
	protected final Method method;
	protected final Object controller;
    protected final IViewResolver viewResolver;

	/**
	 * uri for mapping
	 */
	protected final String	effectiveURI;
	
	protected final boolean hasRequest;
	
	public ViewAction(String u, Method m, Object ctrl, boolean hasReq, IViewResolver viewResolver_) {
		effectiveURI = u;
		hasRequest = hasReq;
		method = m;
		controller = ctrl;
        this.viewResolver = viewResolver_;
	}
	
	@Override
	public void perform(RequestContext context) throws Throwable {
		context.putAttr(ACTION_NAME, this);
		Object v = null;
		if (hasRequest)
			v = method.invoke(controller, context);
		else
			v = method.invoke(controller);
		
		if (v != null) {
            viewResolver.resolve(context, (IView)v);
		}
	}

	MethodHandle handle;
	@Override
	public MethodHandle getMethodHandle() {
		if (handle == null) {
			try {
				handle = MethodHandles.lookup().unreflect(method);
				handle = controller == null ? handle : handle.bindTo(controller);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return handle;
	}

	@Override
	public String getEffectiveURI() {
		return effectiveURI;
	}
}
