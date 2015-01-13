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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caibowen.gplume.context.bean.DisposableBean;
import com.caibowen.gplume.web.actions.DefaultAction;
import com.caibowen.gplume.web.actions.IActionMapper;
import com.caibowen.gplume.web.actions.SuffixActionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.web.misc.DefaultErrorHandler;
/**
 * 
 * Note that controlCenter is also an IRequestProcessor
 * 
 * when initialized, it will hook itself at the end of process chain
 * 
 * @author BowenCai
 *
 */
public abstract class BaseControlCenter
							implements IRequestProcessor,
									DisposableBean,
									Serializable {
	
	private static final long serialVersionUID = -5906639792037911875L;
	
	private static final Logger LOG = LoggerFactory.getLogger(BaseControlCenter.class);// Logger.getLogger("ControlCenter");
	
	private ServletContext servletContext;
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	public ServletContext	getServletContext() {
		return servletContext;
	}

	List<String> staticURLs;
	public List<String> getStaticURLs() {
		return staticURLs;
	}
	public void setStaticURLs(List<String> staticURLs) {
		this.staticURLs = staticURLs;
	}

	/**
	 * optional.
	 * visible to RequestContext
	 */
	IErrorHandler errorHandler = new DefaultErrorHandler();
	public IErrorHandler getErrorHandler() 
	{ return errorHandler;}
	public void setErrorHandler(IErrorHandler e) 
	{this.errorHandler = e;}
	
	protected List<Object> controllers;
	public void setControllers(List<Object> controllers) {
		this.controllers = controllers;
	}
	public List<Object> getControllers() {
		return controllers;
	}

	protected Set<String> defaultURIs;
	public Set<String> getDefaultURIs() {
		return defaultURIs;
	}
	public void setDefaultURIs(Set<String> defaultURIs) {
		this.defaultURIs = defaultURIs;
	}
	protected String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
	protected DefaultAction defaultAction;

	public String getDefaultServletName() {
		return defaultServletName;
	}
	public DefaultAction getDefaultAction() {
		return defaultAction;
	}

	/** Default Servlet name used by Tomcat, Jetty, JBoss, and GlassFish */
	private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

	/** Default Servlet name used by Google App Engine */
	private static final String GAE_DEFAULT_SERVLET_NAME = "_ah_default";

	/** Default Servlet name used by Resin */
	private static final String RESIN_DEFAULT_SERVLET_NAME = "resin-file";

	/** Default Servlet name used by WebLogic */
	private static final String WEBLOGIC_DEFAULT_SERVLET_NAME = "FileServlet";

	/** Default Servlet name used by WebSphere */
	private static final String WEBSPHERE_DEFAULT_SERVLET_NAME = "SimpleFileServlet";

	protected IActionMapper buildDefaultMapper(ServletContext context) {
		RequestDispatcher rd = getServletContext().getNamedDispatcher(COMMON_DEFAULT_SERVLET_NAME);
		if (rd != null) {
			defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
		} else if (null != (rd = getServletContext().getNamedDispatcher(GAE_DEFAULT_SERVLET_NAME))) {
			defaultServletName = GAE_DEFAULT_SERVLET_NAME;
		} else if (null != (rd = getServletContext().getNamedDispatcher(RESIN_DEFAULT_SERVLET_NAME))) {
			defaultServletName = RESIN_DEFAULT_SERVLET_NAME;
		} else if (null != (rd = getServletContext().getNamedDispatcher(WEBLOGIC_DEFAULT_SERVLET_NAME))) {
			defaultServletName = WEBLOGIC_DEFAULT_SERVLET_NAME;
		} else if (null != (rd = getServletContext().getNamedDispatcher(WEBSPHERE_DEFAULT_SERVLET_NAME))) {
			defaultServletName = WEBLOGIC_DEFAULT_SERVLET_NAME;
		} else {
			throw new IllegalStateException("Unable to locate the default servlet for serving static content. " +
				"Please set the 'defaultServletName' property explicitly.");
		}
		defaultAction = new DefaultAction(rd);
		IActionMapper am = new SuffixActionMapper();
		for (String s : defaultURIs)
			am.add(s, defaultAction);
		return am;
	}


	public abstract void init(ServletContext context) throws Throwable;
	/**
	 * entery
	 * 
	 * @param req
	 * @param resp
	 */
	public void service(HttpServletRequest req, HttpServletResponse resp) {
		getPreProcessor().process(new RequestContext(req, resp, this));
	}
	
	IRequestProcessor preProcessor = null;
	
	@Override
	public void process(RequestContext context) {
		handle(context.path, context);
	}
	/**
	 * any requseContext can enter this function at any time.
	 * but be caution about the uri, it may cause short-circuit
	 */
	abstract public void 	handle(String uri, RequestContext requestContext);

	public void handleStatic(RequestContext requestContext) {
		try {
			defaultAction.perform(requestContext);
		} catch (Throwable t) {
			throw new RuntimeException("Failed handle request[" + requestContext.path + "] to static resource.", t);
		}
	}

	/**
	 * control center is the tail of processing chain, no next
	 */
	@Override
	public IRequestProcessor getNext() {
		throw new UnsupportedOperationException("ControlCenter is the end of process chain");
	}
	@Override
	public void setNext(IRequestProcessor preProcessor) {
		throw new UnsupportedOperationException("ControlCenter is the end of process chain");
	}
	
	public IRequestProcessor getPreProcessor() {
		return preProcessor;
	}
	public void setPreProcessor(@Nullable IRequestProcessor preProcessor) {
		this.preProcessor = preProcessor;
	}

	/**
	 * add controller object dynamically
	 * 
	 * @param doInject inject properties or not
	 * @param controller the controller object
	 * 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws Exception
	 *             exception may be thrown when looking for handlers
	 */
	abstract public void 		addController(Object controller, boolean doInject) throws Exception;	
	abstract public void 		removeController(Class<?> controllerClass);
	abstract public boolean 	removeController(final String uri);

	abstract public void 		refresh() throws Exception;
	
	abstract public boolean 	removeHandle(final String uri);
	abstract public void 		removeHandle(Class<?> controllerClass);

	abstract public boolean 	removeInterception(final String uri);
	abstract public void 		removeInterception(Class<?> controllerClass);
}
