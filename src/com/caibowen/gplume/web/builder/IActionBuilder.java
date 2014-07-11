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

import javax.annotation.Nullable;

import com.caibowen.gplume.web.builder.actions.Interception;

/**
 * <pre>
 * Build Action Object base on handle method and uri.
 * 
 * if the method is non-static, controller object will be binded to the methodHandle
 * 
 * it is highly recommended that you use no primitive class in the method declaration
 * since the string arg extracted from actual URL will be convert to a non-primitive value first, 
 * and if the conversion failed, null is returned, 
 * which will cause a NullPointerException in the auto-boxing
 * </pre>
 * @author BowenCai
 *
 */
public interface IActionBuilder {
	
	Interception buildInterception(String u, Object object, Method method);

	IAction buildAction(final String uri, @Nullable Object object, Method method);

}
