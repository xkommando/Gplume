package com.caibowen.gplume.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caibowen.gplume.core.AppContext;


/**
 * the only one servlet, pass request and response to ControlCenter
 * 
 * DO NOT set it to be loaded on start up!(the context may have not been initialized)
 * 
 * @author BowenCai
 *
 */
public class DispacherServlet extends HttpServlet {

	private static final long serialVersionUID = 8478522490124064747L;

//	private static final Logger LOG = Logger.getLogger(DispacherServlet.class.getName());
	
	ControlCenter controlCenter;
	
	@Override
	public void init() throws ServletException {

		controlCenter = AppContext.getBean("controlCenter");
		
		controlCenter.setServletContext(getServletContext());

		try {	
			
			controlCenter.prepare();
			
		} catch (Throwable e) {
			// no exception will be thrown at production, so remove this line after test!

			// if debug, print it
			e.printStackTrace();
		}
		
		super.init();
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {
		
		controlCenter.process(request, response);

	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}
