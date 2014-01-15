package com.caibowen.gplume.web;

import com.caibowen.gplume.web.ControlCenter.Request;

/**
 * handling HTTP error, mainly 404 and 500
 * 
 * this interface is set for customized HTTP error handling
 * 
 * @author BowenCai
 * @see com.caibowen.gplume.web.ControlCenter
 */
public interface IErrorHandler {


	public void http403(Request request);
	public void http404(Request request);
	public void http500(Request request);
}
