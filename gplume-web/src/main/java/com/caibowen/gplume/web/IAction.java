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
package com.caibowen.gplume.web;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;


/**
 * Action = Controller  + one handler function of this controller
 * 
 * each controller has one and only one instance, 
 * whereas the handler of which are many
 * 
 * Note that for ViewActions, the method invking is doen by Method instead of MethodHandle
 * 
 * @author BowenCai
 *
 */
public interface IAction extends Serializable {

	public static final String ACTION_NAME = IAction.class.getName();
	
	/**
	 * 
	 * @param requestContext
	 * @throws Throwable
	 */
	public void perform(RequestContext requestContext) throws Throwable;

	/**
	 * if this method is not static, the returned method handle is binded to the object
	 * and can be invoked without the object parameter
	 * 
	 * @return method handle
	 */
    @Deprecated
	public MethodHandle getMethodHandle();
	
	/**
	 * 
	 * @return the uri for routing
	 */
	public String getEffectiveURI();
}
