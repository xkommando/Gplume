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
package com.caibowen.gplume.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import com.caibowen.gplume.logging.Logger;
import com.caibowen.gplume.logging.LoggerFactory;
import com.caibowen.gplume.web.builder.IAction;
import com.caibowen.gplume.web.meta.Controller;
import com.caibowen.gplume.web.meta.Handle;

/**
 * 
 * controller with no support for interception
 * 
 * @author BowenCai
 *
 */
public class SimpleControlCenter extends ControlCenter {
	
	private static final long serialVersionUID = -1948249520836896853L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleControlCenter.class);
	
	@Override
	public void handle(final String uri, RequestContext requestContext) {
		Throwable thrown = null;
		
		IAction action = actionFactory.findAction(requestContext.httpmMthod, uri);
		
		if (action == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("no action for URI[" + uri + "]");
			}
			errorHandler.http404(requestContext);
			return;
		}
		
		try {
			
			action.perform(requestContext);
			
		} catch (UndeclaredThrowableException udefe) {

			LOG.error("Invokation Exception " + udefe.getMessage(),
					udefe.getUndeclaredThrowable());
			thrown = udefe;

		} catch (IOException ioex) {

			LOG.error("I/O Exception :" + ioex.getMessage(), ioex);
			thrown = ioex;

		} catch (ServletException servex) {

			LOG.error("Servlet Exception : " + servex.getMessage(), servex);
			thrown = servex;

		} catch (Exception e) {

			LOG.error(e.getClass().getName() + " Exception: " + "\n" + e.getMessage(), e);
			thrown = e;

		} catch (Throwable thr) {

			LOG.error("Error: " + thr.getClass().getName()
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
		String prefix = null;
		Controller anno = clazz.getAnnotation(Controller.class);
		if (anno != null) {
			prefix = anno.value();
		}
		// public only
		Method[] methods = clazz.getMethods();
//		System.out.println("SimpleControlCenter.addController()");
		for (Method method : methods) {
//			System.out.println(method.isAnnotationPresent(Handle.class) + "   " + method.toString());
			if (method.isAnnotationPresent(Handle.class)) {
				actionFactory.registerHandles(prefix, controller, method);
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
