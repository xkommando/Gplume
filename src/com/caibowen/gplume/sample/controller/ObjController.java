package com.caibowen.gplume.sample.controller;

import com.caibowen.gplume.web.meta.ReqParam;
import com.caibowen.gplume.web.meta.SessionAttr;

public class ObjController {
	
	@ReqParam
	String reqVal;
	
	@SessionAttr
	String sessionVal;

//	@Handle({"case1"})
	public String name() {
		
		System.out.println("ObjController.name()");
		System.out.println(reqVal);
		System.out.println(sessionVal);
		
		return "/index.jsp";
	}
}
