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

import com.caibowen.gplume.core.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.web.actions.Interception;
import com.caibowen.gplume.web.annotation.Controller;
import com.caibowen.gplume.web.annotation.Handle;
import com.caibowen.gplume.web.annotation.Intercept;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class ControlCenter extends BaseControlCenter {

	private static final long serialVersionUID = -5848401100999563548L;
	
	private static final Logger LOG = LoggerFactory.getLogger(ControlCenter.class.getName());
	
	@Inject
	protected IActionFactory actionFactory;
	public void setActionFactory(IActionFactory actionFactory) {
		this.actionFactory = actionFactory;
	}

// -----------------------------------------------------------------------------

	/**
	 * called before any request processing
	 *
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Throwable
	 */
	public void init(ServletContext context) throws Throwable {
		/**
		 * set servlet context
		 */
		setServletContext(context);

		/**
		 * build Actions
		 */
		for (Object ctrlObj : controllers) {
			try {
				addController(ctrlObj, true);
			} catch (Exception e) {
				throw new RuntimeException(
						"error adding controller["
								+ ctrlObj.getClass().getName() + "]\r\n"
								+ e.getMessage(), e);
			}
		}

		/**
		 *  build processing chain: put this control center to the tail
		 */
		IRequestProcessor p = getPreProcessor();
		if (p == null) {
			setPreProcessor(this);
		} else {
			// controlCenter is the tail of process chain
			while (p.getNext() != null) {
				p = p.getNext();
			}
			p.setNext(this);
		}
		actionFactory.setDefaultMapper(buildDefaultMapper(context));
		LOG.info("\t>>>>> Ready to roll! ");
	}

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

			LOG.error(udefe.getMessage(),
					udefe.getUndeclaredThrowable());
			thrown = udefe;

		} catch (IOException ioex) {

			LOG.error("I/O :" + ioex.getCause(), ioex);
			thrown = ioex;

		} catch (ServletException servex) {

			LOG.error("Servlet : " + servex.getCause(), servex);
			thrown = servex;

		} catch (Throwable thr) {

			LOG.error(thr.getMessage()
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
			injector.injectMediate(controller);
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
			controllers = new ArrayList<>(32);
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

	public static List<String> scanHandleURI(Class<?> clazz) {

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
		
	public static List<String> scanInterceptURI(Class<?> clazz) {

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


