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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.context.ContextBooter;
import com.caibowen.gplume.context.InputStreamProvider;
import com.caibowen.gplume.context.ServletContextInputStreamProvider;

/**
 * 
 * 
 * 
 * <li>build beans according to the manifest file</li> <li>prepare localeService
 * </li> <li>register all listeners in the bean factory</li>
 * 
 * @author BowenCai
 * 
 */
public class WebAppBooter implements ServletContextListener {

	/**
	 * <li>set default locale, time zone</li> <li>build beans according to the
	 * manifest file</li> <li>prepare localeService</li> <li>register all
	 * listeners in the bean factory</li>
	 * 
	 * @see WebLocalPropertiesLoader
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {

		ServletContext servletContext = event.getServletContext();
		ContextBooter bootstrap = new ContextBooter();
		bootstrap.setClassLoader(this.getClass().getClassLoader());
		// prepare
		InputStreamProvider provider = new ServletContextInputStreamProvider(
				servletContext);
		
		bootstrap.setStreamProvider(provider);
		
		String manifestPath = servletContext.getInitParameter(AppContext.MANIFEST);
		bootstrap.setManifestPath(manifestPath);
		
		bootstrap.boot();
		
		// emit signal
		WebAppStartedEvent signal = new WebAppStartedEvent(this);
		signal.setTime(AppContext.now().getTime());
		AppContext.broadcaster.broadcast(signal);
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

}
