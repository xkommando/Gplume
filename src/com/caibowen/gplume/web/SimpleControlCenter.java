/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.caibowen.gplume.web.action.Action;
import com.caibowen.gplume.web.note.Handle;

/**
 * 
 * controller with no support for interception
 * 
 * @author BowenCai
 *
 */
public class SimpleControlCenter extends ControlCenter {

	private static final long serialVersionUID = -1948249520836896853L;
	
	private static final Logger LOG = Logger.getLogger(SimpleControlCenter.class.getName());
	
	@Override
	public void handle(final String uri, RequestContext requestContext) {
		Throwable thrown = null;
		
		Action action = actionFactory.findAction(requestContext.httpmMthod, uri);
		
		if (action == null) {
			LOG.warning("no action for[" + uri + "]");
			errorHandler.http404(requestContext);
			return;
		}
		
		try {
			
			action.perform(requestContext);
			
		} catch (UndeclaredThrowableException udefe) {

			LOG.log(Level.SEVERE, "Invokation Exception " + udefe.getMessage(),
					udefe.getUndeclaredThrowable());
			thrown = udefe;

		} catch (IOException ioex) {

			LOG.log(Level.SEVERE, "I/O Exception :" + ioex.getCause(), ioex);
			thrown = ioex;

		} catch (ServletException servex) {

			LOG.log(Level.SEVERE, "Servlet Exception : " + servex.getCause(), servex);
			thrown = servex;

		} catch (Exception e) {

			LOG.log(Level.SEVERE,
					e.getClass().getName() + " Exception: " + "\n" + e.getCause(), e);
			thrown = e;

		} catch (Throwable thr) {

			LOG.log(Level.SEVERE, "Error: " + thr.getClass().getName()
					+ "\n Message"+ thr.getMessage()
					+ "\n Cause: " + thr.getCause(), thr);
			
			thrown = thr;
		}

		if (thrown != null) {
			requestContext.putAttr(java.lang.Throwable.class.getName(), thrown);
			errorHandler.http500(requestContext);
		}
	}
	
	@Override
	public void addController(Object controller, boolean doInject) throws Exception {
		
		if (doInject) {
			injector.inject(controller);
		}
		
		Class<?> clazz = controller.getClass();
		// public only
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(Handle.class)) {
				actionFactory.registerHandle(controller, method);
			}
		} // for method
		if (controllers == null) {
			controllers = new ArrayList<Object>(16);
		}
		if (!controllers.contains(controller)) {
			controllers.add(controller);
		}
	}
	
}
