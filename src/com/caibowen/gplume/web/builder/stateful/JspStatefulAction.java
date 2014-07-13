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

import com.caibowen.gplume.web.builder.actions.ViewAction;


/**
 * String jss(State s, RequestContext ctx);
 * String jss(State s);
 *  
 * @author BowenCai
 *
 */
public class JspStatefulAction extends ViewAction {

	public JspStatefulAction(String u, Method m, Object ctrl, boolean req) {
		super(u, m, ctrl, req);
	}

	private static final long serialVersionUID = 5126077573601786405L;



}
