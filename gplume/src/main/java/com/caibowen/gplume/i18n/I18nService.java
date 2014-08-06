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
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.caibowen.gplume.context.*;
import com.caibowen.gplume.context.bean.DisposableBean;
import com.caibowen.gplume.context.bean.InitializingBean;
import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;

/**
 * 
 * 
 * @author BowenCai
 */
public class I18nService implements Serializable, InitializingBean, DisposableBean {
	
	private static final long serialVersionUID = 2823988842476726160L;

	private static final Logger LOG = LoggerFactory.getLogger(I18nService.class.getName());
	
	@Inject Dialect defaultLang = Dialect.SimplifiedChinese;
	@Inject TimeZone defaultTimeZone;
	@Inject Properties pkgFiles;
	
	protected final EnumMap<Dialect, NativePackage> pkgTable 
			= new EnumMap<Dialect, NativePackage>(Dialect.class);
	
	/**
	 * key: ISO 639-1 id
	 * value: file path
	 * @param pkgFiles the pkgFiles to set
	 */
	@Inject
	public void setPkgFiles(Properties pkgFiles) {
		this.pkgFiles = pkgFiles;
	}
	
	/**
	 * 
	 * @param provider
	 */
	public void loadFiles(final InputStreamProvider provider) throws Exception {
		if (pkgFiles == null || pkgFiles.size() == 0) {
			throw new NullPointerException("empty properties");
		}
		
		InputStreamSupport support = new InputStreamSupport(provider);
        InputStreamProviderProxy sp = new InputStreamProviderProxy();
        sp.defaultProvider = provider;
        support.setStreamProvider(sp);

		for (Map.Entry<Object, Object> e : pkgFiles.entrySet()) {
			if (e.getKey() instanceof String && e.getValue() instanceof String) {
				String k = (String) e.getKey();
				String path = (String) e.getValue();
				final Dialect dialect = resolve(k);
				final Properties pkg = new Properties();

				support.withPath(path, new InputStreamCallback() {
					@Override
					public void doInStream(InputStream stream) throws Exception {
						pkg.load(stream);
					}
				});
				addProperties(dialect, pkg);
			}// if is string
		}// for each pkg
	}
	
	/**
	 * 
	 * @param dialect if is null, look for dialect in Properties
	 * @param properties
	 */
	public void addProperties(@Nullable Dialect dialect, 
								@Nonnull Properties properties) {
		
		if (dialect == null) {
            dialect = searchForDialect(properties);
		}
		if (dialect == defaultLang) {
			LOG.debug("pkg for default dialect[" + defaultLang.nativeName + "] added");
			pkgTable.put(dialect, new NativePackage(dialect, properties, null));
		} else {
			pkgTable.put(dialect, new NativePackage(dialect, 
									properties, 
									pkgTable.get(defaultLang)));
			LOG.debug("pkg for [" + dialect.nativeName + "] added");
		}
	}

    @Nonnull
    protected static Dialect searchForDialect(@Nonnull Properties properties) {
        Object de = properties.get(Dialect.NAME);
        if (null != de) {
            if (de instanceof String)
                return resolve((String) de);
            else if (de instanceof Dialect)
                return (Dialect) de;
        }
        throw new NullPointerException("no dialect specified");
    }
	/**
	 * 
	 * @param localeInfo
	 * @return
	 */
	@Nullable
	public NativePackage getPkg(String localeInfo) {
		Dialect lang = resolve(localeInfo);
		if (lang == Dialect.Unknown) {
			return pkgTable.get(defaultLang);
		} else {
			return pkgTable.get(lang);
		}
	}
	
	/**
	 * 
	 * @param lang
	 * @return
	 */
	@Nullable
	public NativePackage getPkg(Dialect lang) {
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
			if (cachedHash != newHash) {
				cachedSet = new HashSet<Dialect>(pkgTable.keySet());
				cachedHash = cachedSet.hashCode();
			}
		}
		return cachedSet;
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
	 * resolve ISO 639-1 id
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
		LOG.warn("failed to resolve dialect [" + localeInfo + "]. set as Unknown");
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
				LOG.info("set candidate of package[" 
						+ e.getKey() + "] to [" + defaultLang  +"]");
			}
		}
	}
	
	@Override
	public void destroy() throws Exception {
		pkgTable.clear();
		this.cachedSet.clear();
		LOG.info("i18nService destroyed");
	}

}
