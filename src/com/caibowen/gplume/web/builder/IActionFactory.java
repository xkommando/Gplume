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
package com.caibowen.gplume.web.builder;

import java.lang.reflect.Method;

import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.actions.SimpleAction;

/**
 * manage all actions
 * @author BowenCai
 *
 */
public interface IActionFactory {

	/**
	 * @param controller
	 * @param method
	 */
	public void 			registerHandle(Object controller, Method method);
	public void 			registerIntercept(Object controller, Method method);
	
	public SimpleAction 			findAction(HttpMethod httpmMthod, String uri);

	public Interception 	findInterception(String uri);

	public boolean 			removeHandle(String uri);

	public boolean 			removeInterception(final String uri);

	public void 			destroy();
}
