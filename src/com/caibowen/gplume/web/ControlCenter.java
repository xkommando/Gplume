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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.logging.Logger;
import com.caibowen.gplume.logging.LoggerFactory;
import com.caibowen.gplume.web.builder.IAction;
import com.caibowen.gplume.web.builder.IActionFactory;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.meta.Controller;
import com.caibowen.gplume.web.meta.Handle;
import com.caibowen.gplume.web.meta.Intercept;

/**
 * 
 * Instantiate all controller, set their properties.
 * 
 * scan for action and interception, put them into different mapper.
 * 
 * map HTTP servlet request to its handle and interception.
 * 
 * @author BowenCai
 * 
 */
public class ControlCenter extends AbstractControlCenter {

	private static final long serialVersionUID = -5848401100999563548L;
	
	private static final Logger LOG = LoggerFactory.getLogger(ControlCenter.class.getName());
	
	@Inject
	protected IActionFactory actionFactory;
	public void setActionFactory(IActionFactory actionFactory) {
		this.actionFactory = actionFactory;
	}

// -----------------------------------------------------------------------------

	@Override
	public void handle(final String uri, RequestContext context) {

		Throwable thrown = null;
		
		IAction action = actionFactory.findAction(context.httpmMthod, context.path);
		
		if (action == null) {
			errorHandler.http404(context);
			if (LOG.isDebugEnabled()) {
				LOG.debug("no action for URI[" + uri + "]");
			}
			return;
		}
		
		Interception interception = actionFactory.findInterception(uri);
		
		try {
			
			if (interception == null) {
				action.perform(context);
			} else {
				interception.intercept(context, action);
			}
			
		} catch (UndeclaredThrowableException udefe) {

			LOG.error("Invokation Error " + udefe.getMessage(),
					udefe.getUndeclaredThrowable());
			thrown = udefe;

		} catch (IOException ioex) {

			LOG.error("I/O Error :" + ioex.getCause(), ioex);
			thrown = ioex;

		} catch (ServletException servex) {

			LOG.error("Servlet Error : " + servex.getCause(), servex);
			thrown = servex;

		} catch (Exception e) {

			LOG.error("Exception: " + e.getClass().getName() + "\n Message: "
							+ e.getMessage() + "\n Cause: " + e.getCause(), e);
			thrown = e;

		} catch (Throwable thr) {

			LOG.error("Other Error: " + thr.getClass().getName()
					+ "\n Message"+ thr.getMessage()
					+ "\n Cause: " + thr.getCause(), thr);
			
			thrown = thr;
		}

		if (thrown != null) {
			context.putAttr(java.lang.Throwable.class.getName(), thrown);
			errorHandler.http500(context);
		}
	}

// -----------------------------------------------------------------------------

	@Override
	synchronized public void refresh() throws Exception {
		for (Object ctrl : controllers) {
			removeController(ctrl);
		}
		for (Object ctrl : controllers) {
			if (!controllers.contains(ctrl)) {
				/**
				 * the controllers are either injected or do not need injection
				 */
				addController(ctrl, false);
			}
		}
	}


// -----------------------------------------------------------------------------
// 						Add Remove Handle, Intercept, Controller
// -----------------------------------------------------------------------------

	@Inject
	protected Injector injector;
	public void setInjector(Injector injector) {this.injector = injector;}
	
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
		for (Method method : methods) {
			if (method.isAnnotationPresent(Handle.class)) {
				actionFactory.registerHandles(prefix, controller, method);
			} else if (method.isAnnotationPresent(Intercept.class)) {
				actionFactory.registerIntercept(prefix, controller, method);
			}
		} // for method
		if (controllers == null) {
			controllers = new ArrayList<Object>(16);
		}
		if (!controllers.contains(controller)) {
			controllers.add(controller);
		}
	}

//-------------------------------------------------------------------
//					change requestContext handler dynamically
//-------------------------------------------------------------------
	@Override
	public boolean removeHandle(final String uri) {
		
		return actionFactory.removeHandle(uri);
	}
	
	@Override
	public void removeHandle(Class<?> controllerClass) {

		List<String> uriList = scanHandleURI(controllerClass);
		if (uriList != null && uriList.size() > 0) {
			for (String uri : uriList) {
				actionFactory.removeHandle(uri);
			}
		}
	}
	
	public void removeHandle(Object controller) {
		removeHandle(controller.getClass());
	}
//-------------------------------------
	@Override
	public boolean removeInterception(final String uri) {
		return actionFactory.removeInterception(uri);
	}
	
	@Override
	public void removeInterception(Class<?> controllerClass) {

		List<String> uriList = scanInterceptURI(controllerClass);
		if (uriList != null && uriList.size() > 0) {
			for (String uri : uriList) {
				actionFactory.removeInterception(uri);
			}
		}
	}
	
	public void removeInterception(Object controller) {
		removeInterception(controller.getClass());
	}
//-------------------------------------
	@Override
	public boolean removeController(final String uri) {
		return actionFactory.removeInterception(uri)|| removeHandle(uri);
	}
	
	@Override
	public void removeController(Class<?> controllerClass) {
		removeHandle(controllerClass);
		removeInterception(controllerClass);
	}
	
	public void removeController(Object controller) {
		removeHandle(controller);
		removeInterception(controller);
	}

	// GC friendly
	@Override
	public void destroy() {

		actionFactory.destroy();

		errorHandler = null;
		injector = null;
		controllers.clear();
		controllers = null;

		LOG.info("control center destroyed");
	}

	protected List<String> scanHandleURI(Class<?> clazz) {

		List<String> urilist = new ArrayList<>(16);
		// public only
		Method[] methods = clazz.getMethods();

		for (Method method : methods) {
			if (method.isAnnotationPresent(Handle.class)) {
				Handle handler = method.getAnnotation(Handle.class);
				String[] uris = handler.value();
				
				if (uris != null && uris.length > 0) {
					urilist.addAll(Arrays.asList(uris));
				}
			}
		}
		return urilist;
	}
		
	protected List<String> scanInterceptURI(Class<?> clazz) {

		List<String> urilist = new ArrayList<>(16);
		// public only
		Method[] methods = clazz.getMethods();

		for (Method method : methods) {
			if (method.isAnnotationPresent(Intercept.class)) {
				Intercept intercept = method.getAnnotation(Intercept.class);				
				String[] uris = intercept.value();
				
				if (uris != null && uris.length > 0) {
					urilist.addAll(Arrays.asList(uris));
				}
			}
		}
		return urilist;
	}

}


