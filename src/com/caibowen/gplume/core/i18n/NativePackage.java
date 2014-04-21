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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.caibowen.gplume.misc.Str;


/**
 * this class is unmodifiable, thus is thread safe
 * 
 * Strings and Objects are stored in different hash map
 * 
 * map for object is lazy-initialized
 * 
 * @author BowenCai
 *
 */
public class NativePackage implements Serializable {

	private static final long serialVersionUID = -1809489470636183308L;

	public static final String NAME = NativePackage.class.getName();

	public final NativeDateFmt dateFmt;
	public final Dialect dialect;
	private HashMap<String, String> strMap;
	private HashMap<Object, Object> objMap;
	private NativePackage cadidate;
	
	/**
	 * 
	 * @param lang
	 * @param prop
	 * @param backup
	 */
	public NativePackage(@Nonnull Dialect lang, 
							@Nonnull Properties prop, 
							@Nullable NativePackage backup) {
		
		dialect = lang;
		dateFmt = new NativeDateFmt(lang);
		cadidate = backup;
		
		for (Object k : prop.keySet()) {
			Object v = prop.get(k);
			if (k instanceof String
				&& v instanceof String) {
				String sk = (String)k;
				String sv = (String)v;
				if (Str.Utils.notBlank(sk)) {
					if (strMap == null) {
						strMap = new HashMap<String, String>(128);
					}
					strMap.put(sk, sv);
				} else {
					throw new NullPointerException(
						"empty properties key[" 
						+ k + "] or value[" + v + "]");
				}
			} else {
				if (null != k && null != v) {
					if (objMap == null) {
						objMap = new HashMap<Object, Object>(64);
					}
					objMap.put(k, v);
				} else {
					throw new NullPointerException(
							"empty properties key[" 
							+ k + "] or value[" + v + "]");
				}
			}
		}
	}
	
	@Nullable
	public String getStr(String k) {
		String lab = strMap.get(k);
		if (lab == null && cadidate != null) {
			lab = cadidate.getStr(k);
		}
		return lab;
	}
	
	@Nullable
	public Object getObj(Object obj) {
		if (objMap != null) {
			Object v = objMap.get(obj);
			if (v == null && cadidate != null) {
				v = cadidate.getObj(obj);
			}
			return v;
		}
		return null;
	}
}
