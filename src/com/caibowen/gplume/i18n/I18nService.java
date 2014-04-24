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
package com.caibowen.gplume.i18n;

import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.caibowen.gplume.context.ClassLoaderInputStreamProvider;
import com.caibowen.gplume.context.FileInputStreamProvider;
import com.caibowen.gplume.context.InputStreamCallback;
import com.caibowen.gplume.context.InputStreamProvider;
import com.caibowen.gplume.context.InputStreamSupport;
import com.caibowen.gplume.context.bean.DisposableBean;
import com.caibowen.gplume.context.bean.InitializingBean;

/**
 * 
 * 
 * @author BowenCai
 */
public class I18nService implements Serializable, InitializingBean, DisposableBean {
	
	private static final long serialVersionUID = 2823988842476726160L;

	private static final Logger LOG = Logger.getLogger(I18nService.class.getName());
	
	@Inject Dialect defaultLang = Dialect.SimplifiedChinese;
	@Inject TimeZone defaultTimeZone;
	@Inject Properties pkgFiles;
	
	protected final EnumMap<Dialect, NativePackage> pkgTable 
			= new EnumMap<Dialect, NativePackage>(Dialect.class);
	
	/**
	 * key: ISO 639-1 name
	 * value: file path
	 * @param pkgFiles the pkgFiles to set
	 */
	@Inject
	public void setPkgFiles(Properties pkgFiles) {
		this.pkgFiles = pkgFiles;
	}
	
	/**
	 * 
	 * @param properties
	 */
	public void loadFiles(final InputStreamProvider provider) throws Exception {
		if (pkgFiles == null || pkgFiles.size() == 0) {
			throw new NullPointerException("empty properties");
		}
		
		InputStreamSupport support = new InputStreamSupport(provider);

		for (Map.Entry<Object, Object> e : pkgFiles.entrySet()) {
			if (e.getKey() instanceof String && e.getValue() instanceof String) {
				String k = (String) e.getKey();
				String path = (String) e.getValue();
				final Dialect dialect = resolve(k);
				final Properties pkg = new Properties();
				if (path.startsWith("classpath:")) {
					support.setStreamProvider(
							new ClassLoaderInputStreamProvider(
								I18nService.class.getClassLoader()));
					path = path.substring(0, path.length());
					
				} else if (path.startsWith("file:")) {
					support.setStreamProvider(new FileInputStreamProvider());
					path = path.substring(5, path.length());
					
				} else {
					support.setStreamProvider(provider);
				}
				support.withPath(path, new InputStreamCallback() {
					@Override
					public void doInStream(InputStream stream) throws Exception {
						pkg.load(stream);
					}
				});
				addProperties(dialect, pkg);
			}
		}
	}
	
//	public void loadResource(Dialect dialect, InputStream stream) {
//		Properties properties = new Properties();
//		try {
//			properties.load(stream);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		loadResource(dialect, properties);
//	}

	/**
	 * 
	 * @param dialect if is null, look for dialect in Properties
	 * @param properties
	 */
	public void addProperties(@Nullable Dialect dialect, 
								@Nonnull Properties properties) {
		
		if (dialect == null) {
			if (null != properties.get(Dialect.NAME)) {
				dialect = (Dialect)properties.get(Dialect.NAME);
			} else {
				throw new NullPointerException("no dialect specified");
			}
		}
		if (dialect == defaultLang) {
			LOG.info("pkg for default dialect[" + defaultLang.nativeName + "] added");
			pkgTable.put(dialect, new NativePackage(dialect, properties, null));
		} else {
			pkgTable.put(dialect, new NativePackage(dialect, 
									properties, 
									pkgTable.get(defaultLang)));
			LOG.info("pkg for [" + dialect.nativeName + "] added");
		}
	}
	
	@Nullable
	public NativePackage getPkg(String localeInfo) {
		Dialect lang = resolve(localeInfo);
		if (lang == Dialect.Unknown) {
			return pkgTable.get(defaultLang);
		} else {
			return pkgTable.get(lang);
		}
	}

	
	private Set<Dialect> cachedSet;
	private int cachedHash;
	
	public Set<Dialect> getSupportedDialects() {
		if (cachedSet == null) {
			cachedSet = new HashSet<Dialect>(pkgTable.keySet());
			cachedHash = cachedSet.hashCode();
			return cachedSet;
			
		} else {
			final int newHash = cachedSet.hashCode();
			if (cachedHash == newHash) {
				return cachedSet;
			} else {
				cachedHash = newHash;
				cachedSet = new HashSet<Dialect>(pkgTable.keySet());
				return cachedSet;
			}
		}
	}

//	public static void main(String...a) {
//		I18nService service = new I18nService();
//		service.pkgTable.put(Dialect.SimplifiedChinese, new NativePackage(Dialect.SimplifiedChinese, new Properties(), null));
//		service.pkgTable.put(Dialect.TraditionalChinese, new NativePackage(Dialect.TraditionalChinese, new Properties(), null));
//		service.pkgTable.put(Dialect.English, new NativePackage(Dialect.English, new Properties(), null));
//		Set<Dialect> set = service.getAll();
//		set.remove(Dialect.SimplifiedChinese);
//		Set<Dialect> set2 = service.getAll();
//		System.out.println(set2);
//	}
	
	/**
	 * resolve ISO 639-1 name
	 * zh_CN
	 * en
	 * @param localeInfo
	 * @return
	 */
	public static Dialect resolve(String localeInfo) {
		
		if (null != localeInfo) {
			localeInfo = localeInfo.trim();
			if (localeInfo.startsWith("zh")
				|| localeInfo.startsWith("ZH")) {
				
				if (localeInfo.length() > 4) {
					char c3 = localeInfo.charAt(3);
					if ((c3 == 'T' || c3 == 't') 
						|| (c3 == 'H' || c3 == 'h')) {
						return Dialect.TraditionalChinese;
					}
				}
				return Dialect.SimplifiedChinese;
				
			} else if (localeInfo.startsWith("en")
						|| localeInfo.startsWith("EN")) {
				return Dialect.English;
			} else if (localeInfo.length() > 2) {
				return Dialect.parseISO639_1(localeInfo.substring(0, 2));
			}
		}
		LOG.warning("failed to resolve dialect [" + localeInfo + "]. set as Unknown");
		return Dialect.Unknown;
	}

	/**
	 * @return the defaultLang
	 */
	public Dialect getDefaultLang() {
		return defaultLang;
	}

	/**
	 * @param defaultLang the defaultLang to set
	 */
	public void setDefaultLang(Dialect defaultLang) {
		this.defaultLang = defaultLang;
	}

	/**
	 * @param defaultTimeZone TimeZone.getTimeZone(defaultTimeZone);
	 */
	public void setDefaultTimeZone(String defaultTimeZone) {
		this.defaultTimeZone = TimeZone.getTimeZone(defaultTimeZone);
	}

	public NativePackage getDefaultPkg() {
		return pkgTable.get(defaultLang);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		for (Map.Entry<Dialect, NativePackage> e : pkgTable.entrySet()) {
			if (e.getKey() != defaultLang
					&& e.getValue().cadidate == null) {
				e.getValue().cadidate = pkgTable.get(defaultLang);
				LOG.config("set candidate of package[" 
						+ e.getKey() + "] to [" + defaultLang  +"]");
			}
		}
	}
	
	@Override
	public void destroy() throws Exception {
		pkgTable.clear();
		this.cachedSet.clear();
		LOG.config("i18nService destroyed");
	}

}
