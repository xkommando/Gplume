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

import java.io.Serializable;

import com.caibowen.gplume.web.RequestContext;


/**
 * Action = Controller  + one handler function of this controller
 * 
 * each controller has one and only one instance, 
 * whereas the handler of which are many
 * 
 * @author BowenCai
 *
 */
public interface IAction extends Serializable {

	public void perform(RequestContext requestContext) throws Throwable;
}
