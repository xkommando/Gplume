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
package com.caibowen.gplume.core.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

/**
 * 
 * 
 * @author BowenCai
 */
public class I18nService implements Serializable {
	
	private static final long serialVersionUID = 2823988842476726160L;
	
	@Inject Dialect defaultLang = Dialect.SimplifiedChinese;

	public final EnumMap<Dialect, NativePackage> pkgTable 
			= new EnumMap<Dialect, NativePackage>(Dialect.class);
	
	public void loadResource(Dialect dialect, InputStream stream) {
		Properties properties = new Properties();
		try {
			properties.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		loadResource(dialect, properties);
	}

	public void loadResource(Dialect dialect, Properties properties) {
		if (dialect == defaultLang) {
			pkgTable.put(dialect, new NativePackage(dialect, properties, null));
		} else {
			pkgTable.put(dialect, new NativePackage(dialect, 
									properties, 
									pkgTable.get(defaultLang)));
		}
//System.out.println("added [" + dialect + "]");
	}
	
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
	
	public Set<Dialect> getAll() {
//		return new HashSet<Dialect>(pkgTable.keySet());
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
//		System.out.println("I18nService.setDefaultLang()" + defaultLang);
		this.defaultLang = defaultLang;
	}
	
	public NativePackage getDefaultPkg() {
		return pkgTable.get(defaultLang);
	}
}
