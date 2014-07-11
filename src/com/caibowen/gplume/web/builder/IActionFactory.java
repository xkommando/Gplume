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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.builder.actions.Interception;

/**
 * manage all actions
 * @author BowenCai
 *
 */
public interface IActionFactory {

	void setActionBuilder(IActionBuilder actionBuilder);
	
	/**
	 * @param controller
	 * @param method
	 */
	 void 			registerHandles(@Nullable String prefix, 
											@Nullable Object ctrl,
											@Nonnull Method method);
	
	 void 			registerIntercept(@Nullable String prefix, 
											@Nullable Object ctrl,
											@Nonnull Method method);
	
	 IAction 			findAction(HttpMethod httpmMthod, String uri);

	 Interception 	findInterception(String uri);

	 boolean 			removeHandle(String uri);

	 boolean 			removeInterception(final String uri);

	 void 			destroy();
}
