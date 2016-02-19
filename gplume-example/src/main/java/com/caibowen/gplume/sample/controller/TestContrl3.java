package com.caibowen.gplume.sample.controller;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.annotation.Controller;
import com.caibowen.gplume.web.annotation.Handle;
import com.caibowen.gplume.web.annotation.ReqAttr;
import com.caibowen.gplume.web.annotation.SessionAttr;

import javax.annotation.Nullable;
import java.util.Date;



@Controller
public class TestContrl3 {

	static class X {
		@SessionAttr("aaa")
		int test_inttt;
		@SessionAttr
		String msg;
		@ReqAttr(defaultVal="159")
		int test_int;
		@SessionAttr(required=false, defaultVal="1992-6-14")
		Date date;
		
		@Nullable
		@SessionAttr
		Integer[] ints;
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
		req.render("index");
	}
	
	@Handle({"/state/2"})
	public String name2(X x, RequestContext req) {
		System.out.println("TestContrl3.name()");
		System.out.println(req);
		System.out.println(x.test_inttt);
		System.out.println(x.msg);
		System.out.println(x.test_int);
		return "index";
	}
	
	@Handle({"/state/3"})
	public String name3(X x) {
		System.out.println("TestContrl3.name()");
		System.out.println(x.test_inttt);
		System.out.println(x.msg);
		System.out.println(x.test_int);
		return "index";
	}
	
//	@Handle({"/state/4"})
//	public IView name4(X x, RequestContext req) {
//		System.out.println("TestContrl3.name()");
//		System.out.println(x.test_inttt);
//		System.out.println(x.msg);
//		System.out.println(x.test_int);
//		System.out.println(x.ints);
//		return IView.get.textView("4 hahaha" + x.msg);
//	}
//
//	@Handle({"/state/5"})
//	public IView name5(X x) {
//		System.out.println("TestContrl3.name()");
//		System.out.println(x.test_inttt);
//		System.out.println(x.msg);
//		System.out.println(x.test_int);
//		return IView.get.textView("5 hahaha" + x.msg);
//	}
	

}
