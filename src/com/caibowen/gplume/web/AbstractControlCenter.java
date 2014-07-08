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

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caibowen.gplume.context.bean.DisposableBean;
import com.caibowen.gplume.logging.Logger;
import com.caibowen.gplume.logging.LoggerFactory;
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
public abstract class AbstractControlCenter 
							implements IRequestProcessor,
									DisposableBean,
									Serializable {
	
	private static final long serialVersionUID = -5906639792037911875L;
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractControlCenter.class);// Logger.getLogger("ControlCenter");
	
	private ServletContext servletContext;
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	public ServletContext	getServletContext() {
		return servletContext;
	}

	/**
	 * optional.
	 * visible to RequestContext
	 */
	@Inject
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
						"error inject/scan controller class["
								+ ctrlObj.getClass().getName() + "]\n"
								+ e.getMessage(), e);
			}
		}
		/**
		 *  build processing chain
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

		LOG.info("\t>>>>> Ready to roll! ");
	}
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
	abstract public void 	handle(String uri, final RequestContext requestContext);
	
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
	public void setPreProcessor(IRequestProcessor preProcessor) {
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
