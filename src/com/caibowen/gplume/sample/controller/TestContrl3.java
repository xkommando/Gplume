package com.caibowen.gplume.sample.controller;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.annotation.ContextAttr;
import com.caibowen.gplume.web.annotation.Controller;
import com.caibowen.gplume.web.annotation.Handle;
import com.caibowen.gplume.web.annotation.ReqAttr;
import com.caibowen.gplume.web.annotation.SessionAttr;



@Controller
public class TestContrl3 {

	class X {
		@SessionAttr("aaa")
		int test_inttt;
		@SessionAttr
		String msg;
		@ReqAttr(defaultVal="159")
		int test_int; 
//		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
//		context.putAttr("test_int", 123);
	}
	
	@Handle({"/state/1"})
	public void name(X x, RequestContext req) {
		System.out.println("TestContrl3.name()");
		System.out.println(req);
		System.out.println(x.test_inttt);
		System.out.println(x.msg);
		System.out.println(x.test_int);
		req.render("/index.jsp");
	}
}
