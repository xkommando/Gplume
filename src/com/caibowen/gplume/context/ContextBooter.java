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
package com.caibowen.gplume.context;

import java.io.InputStream;

import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.i18n.WebI18nService;

/**
 * 
 * For manifest.xml and i18n properties file:
 * 
 * 1. if read manifest.xml as file on file system
 * write as "file:src/manifest.xml"
 * 
 * 2. if read manifest.xml is in class path(e.g. in src folder)
 * write as "classpath:manifest.xml"
 * 
 * 3. if read as file in web root, a servlet context input stream provider is needed
 * write as "/in_web_root" or "/WEB-INF/in_web_root"
 * 
 * 
 *  
 * @author BowenCai
 *
 */
public class ContextBooter {

	private static final Logger LOG 
					= LoggerFactory.getLogger(ContextBooter.class);

	/**
	 * bean id for internationalization
	 */
	private static final String I18N_SERVICE_BEAN_ID = "i18nService";
	

	private InputStreamSupport streamSupport = new InputStreamSupport();

	// optional
	private ClassLoader classLoader = ContextBooter.class.getClassLoader();
	
	// required if in web root, set by caller
	private InputStreamProvider streamProvider;
	// required
	private String manifestPath;
	
	// require streamProvider
	
	public void boot() {
		
		// set classloader for beanAssembler
		AppContext.beanAssembler.setClassLoader(this.classLoader);
		String path;
		if (Str.Utils.notBlank(manifestPath)) {
			// build beans
			if (manifestPath.startsWith("classpath:")) {
				streamSupport.setStreamProvider(
						new ClassLoaderInputStreamProvider(getClassLoader()));
				path = manifestPath.substring(10, manifestPath.length());
				
			} else if (manifestPath.startsWith("file:")) {
				streamSupport.setStreamProvider(new FileInputStreamProvider());
				path = manifestPath.substring(5, manifestPath.length());

			} else {
				// set by caller, can be servlet context inputstream provider
				streamSupport.setStreamProvider(streamProvider);
				path = manifestPath;
			}
			
			streamSupport.withPath(path, new InputStreamCallback() {
				@Override
				public void doInStream(InputStream stream) throws Exception {
					AppContext.beanAssembler.assemble(stream);
				}
			});
			
		} else {
			LOG.warn("no manifest file specified "
					+ "For web application, check your web.xml for context-param[{0}]"
					+ AppContext.MANIFEST);
			return;
		}

		// register listeners
		AppContext.beanAssembler
				.inTake(AppContext.broadcaster.listenerRetreiver);
		
		// load language packages
		WebI18nService service = AppContext.beanAssembler.getBean(I18N_SERVICE_BEAN_ID);
		if (service != null) {
			try {
				service.loadFiles(streamProvider);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			LOG.info(I18N_SERVICE_BEAN_ID + " ready to roll!");
		} else {
			LOG.warn("cannot find " + I18N_SERVICE_BEAN_ID);
		}
	}
	
	/**
	 * @return the manifestPath
	 */
	public String getManifestPath() {
		return manifestPath;
	}

	/**
	 * @param manifestPath the manifestPath to set
	 */
	public void setManifestPath(String manifestPath) {
		this.manifestPath = manifestPath;
	}

	/**
	 * @param streamProvider the streamProvider to set
	 */
	public void setStreamProvider(InputStreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}

	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
