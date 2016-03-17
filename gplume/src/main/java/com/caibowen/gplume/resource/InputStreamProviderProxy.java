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

package com.caibowen.gplume.resource;

import com.caibowen.gplume.common.Pair;
import com.caibowen.gplume.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;


/**
 * set defaultProvider
 * or classPathProvider
 *
 * for path str:
 * "classpath:aaa"  -> classPathProvider
 * "file:aaa"       -> FileInputStreamProvider
 * "url:aaa"        -> URLInputStreamProvider
 * "aaa"            -> defaultProvider
 *
 * @author bowen.cbw
 *
 */
public class InputStreamProviderProxy implements InputStreamProvider {

    private static final Logger LOG = LoggerFactory.getLogger(InputStreamProviderProxy.class);

	public static final InputStreamProvider FILE_PROVIDER = new FileInputStreamProvider();
	public static final InputStreamProvider URL_PROVIDER = new URLInputStreamProvider();
    public static final InputStreamProvider CLASSLOADER_PROVIDER = new ClassLoaderInputStreamProvider(InputStreamProviderProxy.class.getClassLoader());

    public static final InputStreamProviderProxy DEFAULT_PROXY = new InputStreamProviderProxy(FILE_PROVIDER);

	public InputStreamProvider classPathProvider = CLASSLOADER_PROVIDER;
	public InputStreamProvider defaultProvider;


    public InputStreamProviderProxy() {}

    public InputStreamProviderProxy(InputStreamProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public void setClassPathProvider(InputStreamProvider classPathProvider) {
        this.classPathProvider = classPathProvider;
    }

    public void setDefaultProvider(InputStreamProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    private Pair<InputStreamProvider, String> matchProvider(String path) {
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
            return new Pair<>(providerToUse, path);
        } else
            throw new NullPointerException("Empty path value");
    }

    @Override
	public InputStream getStream(String path) throws IOException {
        Pair<InputStreamProvider, String> p = matchProvider(path);
        LOG.trace("using [" + p.first.getClass().getSimpleName() + "] to get [" + p.second + "]");
		return p.first.getStream(p.second);
	}

	@Override
	public String getRealPath(String path) {
        Pair<InputStreamProvider, String> p = matchProvider(path);
        return p.first.getRealPath(p.second);
	}

}
