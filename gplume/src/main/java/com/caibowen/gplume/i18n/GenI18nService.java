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

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.resource.InputStreamCallback;
import com.caibowen.gplume.resource.InputStreamSupport;
import com.caibowen.gplume.context.bean.InitializingBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 *  general I18n Service
 * 
 * @author BowenCai
 */
public class GenI18nService implements I18nService, InitializingBean, Serializable {
	
	private static final long serialVersionUID = 2823988842476726160L;

	private static final Logger LOG = LoggerFactory.getLogger(GenI18nService.class.getName());
	
	@Inject Dialect defaultLang = Dialect.SimplifiedChinese;
	@Inject TimeZone defaultTimeZone;
	@Inject Properties pkgFiles;
    @Inject IDialectResolver resolver;

    protected InputStreamSupport streamSupport;
	
	protected final EnumMap<Dialect, NativePackage> pkgTable 
			= new EnumMap<Dialect, NativePackage>(Dialect.class);

    @Override
    public void afterPropertiesSet() throws Exception {

        streamSupport = AppContext.beanAssembler.getConfigCenter().getStreamSupport();

        loadFiles();

        for (Map.Entry<Dialect, NativePackage> e : pkgTable.entrySet()) {
            if (e.getKey() != defaultLang
                    && e.getValue().cadidate == null) {
                e.getValue().cadidate = pkgTable.get(defaultLang);
                LOG.info("set candidate of package["
                        + e.getKey() + "] to [" + defaultLang  +"]");
            }
        }
    }

	/**
	 * key: ISO 639-1 id
	 * value: file path
	 * @param pkgFiles the pkgFiles to set
	 */
	@Override
    @Inject
	public void setPkgFiles(Properties pkgFiles) {
		this.pkgFiles = pkgFiles;
	}
	
	protected void loadFiles() throws Exception {

		if (pkgFiles == null || pkgFiles.size() == 0) {
			throw new NullPointerException("empty properties");
		}

		for (Map.Entry<Object, Object> e : pkgFiles.entrySet()) {
			if (e.getKey() instanceof String && e.getValue() instanceof String) {
				String k = (String) e.getKey();
				String path = (String) e.getValue();
				final Dialect dialect = resolver.resolve(k);
				final Properties pkg = new Properties();

                streamSupport.withPath(path, new InputStreamCallback() {
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
	protected void addProperties(@Nullable Dialect dialect,
								@Nonnull Properties properties) {
		
		if (dialect == null) {
            dialect = searchForDialect(properties);
		}
		if (dialect == defaultLang) {
			Object o = pkgTable.put(dialect, new NativePackage(dialect, properties, null));
            LOG.debug("pkg for default dialect[" + defaultLang.nativeName
                    + (o == null ? "] added" : "] updated") );

		} else {
            Object o = pkgTable.put(dialect, new NativePackage(dialect,
									properties, 
									pkgTable.get(defaultLang)));

			LOG.debug("pkg for [" + dialect.nativeName +
                    (o == null ? "] added" : "] updated") );
		}
	}

    @Nonnull
    protected Dialect searchForDialect(@Nonnull Properties properties) {
        Object de = properties.get(Dialect.NAME);
        if (null != de) {
            if (de instanceof String)
                return resolver.resolve((String) de);
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
	@Override
    @Nullable
	public NativePackage getPkg(String localeInfo) {
		Dialect lang = resolver.resolve(localeInfo);
		if (lang == Dialect.Unknown) {
			return getDefaultPkg();
		} else {
			return getPkg(lang);
		}
	}
	
	/**
	 * 
	 * @param lang
	 * @return
	 */
	@Override
    @Nullable
	public NativePackage getPkg(Dialect lang) {
		if (lang == Dialect.Unknown) {
			return pkgTable.get(defaultLang);
		} else {
			return pkgTable.get(lang);
		}
	}


    // copy on write
	private Set<Dialect> cachedSet;
	private int cachedHash;
	
	@Override
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
//		GenI18nService service = new GenI18nService();
//		service.pkgTable.put(Dialect.SimplifiedChinese, new NativePackage(Dialect.SimplifiedChinese, new Properties(), null));
//		service.pkgTable.put(Dialect.TraditionalChinese, new NativePackage(Dialect.TraditionalChinese, new Properties(), null));
//		service.pkgTable.put(Dialect.English, new NativePackage(Dialect.English, new Properties(), null));
//		Set<Dialect> set = service.getAll();
//		set.remove(Dialect.SimplifiedChinese);
//		Set<Dialect> set2 = service.getAll();
//		System.out.println(set2);
//	}


    @Nonnull
    @Override
    public Dialect resolve(String s) {
        return resolver.resolve(s);
    }

    /**
	 * @return the defaultLang
	 */
	@Override
    public Dialect getDefaultLang() {
		return defaultLang;
	}

	@Override
    public NativePackage getDefaultPkg() {
		return pkgTable.get(defaultLang);
	}

    public void setDefaultLang(Dialect defaultLang) {
        this.defaultLang = defaultLang;
    }

    public void setDefaultTimeZone(String timeZoneId) {
        this.defaultTimeZone = TimeZone.getTimeZone(timeZoneId);
    }

    public void setResolver(IDialectResolver resolver) {
        this.resolver = resolver;
    }
}
