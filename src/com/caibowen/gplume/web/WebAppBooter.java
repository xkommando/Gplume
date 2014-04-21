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

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.caibowen.gplume.core.context.AppContext;
import com.caibowen.gplume.core.context.InputStreamCallback;
import com.caibowen.gplume.core.context.InputStreamProvider;
import com.caibowen.gplume.core.context.InputStreamSupport;
import com.caibowen.gplume.core.context.ServletContextInputStreamProvider;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.i18n.WebAppBootHelper;
import com.caibowen.gplume.web.i18n.WebI18nService;

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

	private static final Logger LOG = Logger.getLogger(WebAppBooter.class
			.getName());
	/**
	 * bean name for internationalization
	 */
	private static final String I18N_SERVICE_BEAN_ID = "i18nService";
	private static final String BOOT_HELPER_BEAN_ID = "bootHelper";
	
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

		// prepare
		InputStreamProvider provider = new ServletContextInputStreamProvider(
				servletContext);
		InputStreamSupport streamSupport = new InputStreamSupport(provider);

		String manifestPath = servletContext
				.getInitParameter(AppContext.MANIFEST);

		AppContext.beanAssembler.setClassLoader(this.getClass()
				.getClassLoader());

		if (Str.Utils.notBlank(manifestPath)) {
			// build beans
			streamSupport.doInStream(manifestPath, new InputStreamCallback() {
				@Override
				public void doWithStream(InputStream stream) throws Exception {
					AppContext.beanAssembler.assemble(stream);
				}
			});
		} else {
			LOG.log(Level.WARNING, "no manifest file specified in web.xml, "
					+ "check your web.xml for context-param["
					+ AppContext.MANIFEST + "]");
			return;
		}

		// register listeners
		AppContext.beanAssembler
				.inTake(AppContext.broadcaster.listenerRetreiver);

		// load localization data

		WebI18nService service = AppContext.beanAssembler
									.getBean(I18N_SERVICE_BEAN_ID);
		
		if (service == null) {
			LOG.log(Level.INFO, "cannot find i18n servivce with bean id[i18nService]");
			
		} else {
			WebAppBootHelper loader = AppContext.beanAssembler
					.getBean(BOOT_HELPER_BEAN_ID);
			if (loader == null) {
				throw new NullPointerException(
						"no WebAppBootHelper with bean id[" + I18N_SERVICE_BEAN_ID +  "]");
			}

			loader.setStreamProvider(provider);
			
			loader.load(service);
			AppContext.beanAssembler.removeBean(BOOT_HELPER_BEAN_ID);
			AppContext.defaults.timeZone = loader.getDefaultTimeZone();
		}
		
		// emit signal
		WebAppStartedEvent signal = new WebAppStartedEvent(this);
		signal.setTime(AppContext.now().getTime());
		AppContext.broadcaster.broadcast(signal);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

}
