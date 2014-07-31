/******************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.context;

import java.io.InputStream;

import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.caibowen.gplume.misc.Str;


/**
 * 
 * @author bowen.cbw
 *
 */
public class InputStreamProviderProxy implements InputStreamProvider {

	public static final InputStreamProvider FILE_PROVIDER = new FileInputStreamProvider();
	public static final InputStreamProvider URL_PROVIDER = new URLInputStreamProvider();

	public InputStreamProvider servletContextProvider;
	public InputStreamProvider classPathProvider;
	public InputStreamProvider defaultProvider;
	
	@Override
	public InputStream getStream(String path) {
		InputStreamProvider providerToUse = null;
		if (Str.Utils.notBlank(path)) {
			// build beans
			if (path.startsWith("classpath:")) {
				providerToUse = classPathProvider;
				path = path.substring(10, path.length());
				
			} else if (path.startsWith("file:")) {
				providerToUse = FILE_PROVIDER;
				path = path.substring(5, path.length());
				
			} else if (path.startsWith("url:")) {
				providerToUse = URL_PROVIDER;
				path = path.substring(4, path.length());
				
			} else {
				providerToUse = defaultProvider;
			}
		}

        LOG.info("using " + providerToUse.getClass().getSimpleName());
		return providerToUse.getStream(path);
	}

    private static final Logger LOG = LoggerFactory.getLogger(InputStreamProviderProxy.class);

	@Override
	public String getContextPath() {
		throw new NotImplementedException();
	}

}
