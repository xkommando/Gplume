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
