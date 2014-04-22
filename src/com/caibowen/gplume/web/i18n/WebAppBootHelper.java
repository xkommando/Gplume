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
package com.caibowen.gplume.web.i18n;

import java.io.InputStream;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import com.caibowen.gplume.core.context.InputStreamCallback;
import com.caibowen.gplume.core.context.InputStreamProvider;
import com.caibowen.gplume.core.context.InputStreamSupport;
import com.caibowen.gplume.core.i18n.Dialect;
import com.caibowen.gplume.core.i18n.I18nService;
import com.caibowen.gplume.misc.Str;

/**
  samples file path:
  
 /WEB-INF/abc/def/zh_CN.properties
 /WEB-INF/abc/def/zh_TW.properties
 /WEB-INF/abc/def/zh_CN
 /WEB-INF/abc/def/zh
 
 /WEB-INF/abc/def/zh_CN.properties
 zh_CN.properties
 zh_CN
 zh
 
 * 
 * 
 * @author BowenCai
 *
 */
public class WebAppBootHelper {

	@Inject List<String> pkgFiles;
	@Inject String defaultTimeZone;
	
	InputStreamProvider streamProvider;
	
	public void load(I18nService service) {
		if (streamProvider == null) {
			throw new IllegalArgumentException("null InputStreamProvider");
		}
		if (pkgFiles == null || pkgFiles.size() == 0) {
			throw new IllegalArgumentException("Empty language package list");
		}
		
		InputStreamSupport support = new InputStreamSupport(streamProvider);
		
		for (final String path : pkgFiles) {
			final Dialect dialect = getDialect(path);
			final I18nService i18nService = service;
			support.doInStream(path, new InputStreamCallback() {
				@Override
				public void doWithStream(InputStream stream) throws Exception {
					i18nService.loadResource(dialect, stream);
				}
			});
		}
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public Dialect getDialect(String path) {
		
		if (!Str.Utils.notBlank(path)) {
			throw new IllegalArgumentException("empty path in [" + pkgFiles + "]");
		}
		int end = path.lastIndexOf("properties");
		/**
		 * zh_CN.properties
		 * or
		 * zh_CN
		 */
		if (end == -1) {
			end = path.length();
		} else {
			end--;
		}
		int start = path.lastIndexOf('/', end);
		if (start == -1) {
			start = 0;
		} else {
			start++;
		}
		if (end - start < 2) {
			throw new IllegalArgumentException(
					"illegal i18n file name[" 
					+ path  +"], shold be like 'zh' or 'zh_CH'," +
					" check your config file");
		}
		String name = path.substring(start, end);
		Dialect dialect = I18nService.resolve(name);
		if (dialect == Dialect.Unknown) {
			throw new IllegalArgumentException("cannot resolve locale name[" 
						+ name + "] in path[" + path + "]");
		}
		return dialect;
	}

	/**
	 * @param streamProvider the streamProvider to set
	 */
	public void setStreamProvider(InputStreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}

	/**
	 * @return the pkgFiles
	 */
	public List<String> getPkgFiles() {
		return pkgFiles;
	}

	/**
	 * @param pkgFiles the pkgFiles to set
	 */
	public void setPkgFiles(List<String> pkgFiles) {
		this.pkgFiles = pkgFiles;
	}

	/**
	 * @return the defaultTimeZone
	 */
	public TimeZone getDefaultTimeZone() {
		return TimeZone.getTimeZone(defaultTimeZone);
	}

	/**
	 * @param defaultTimeZone the defaultTimeZone to set
	 */
	public void setDefaultTimeZone(String defaultTimeZone) {
		this.defaultTimeZone = defaultTimeZone;
	}
}
