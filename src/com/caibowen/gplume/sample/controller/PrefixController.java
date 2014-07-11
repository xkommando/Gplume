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
package com.caibowen.gplume.sample.controller;

import java.util.Date;

import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.annotation.Controller;
import com.caibowen.gplume.web.annotation.Handle;


@Controller("/prefix/")
public class PrefixController{

	@Handle({"case1"})// real path: /prefix/case1
	public static String case1() {
//		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		return "/index.jsp";
	}
	
	@Handle({"index",
			"index.html",
			"index.jsp"})
	public static void staticMethod(RequestContext context) {
//		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		context.render("/index.jsp");
	}
	
	@Handle(value = { "case2/{date formate like 1992-6-14::Date}" },
			httpMethods = {HttpMethod.GET, HttpMethod.POST})
	public static String case2(Date f) {
		System.out.println("PrefixController.happyBirthday()");
		System.out.println(f);
		return "/index.jsp";
	}
}
