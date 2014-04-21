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
package com.caibowen.gplume.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.caibowen.gplume.common.CacheBuilder;
import com.caibowen.gplume.common.WeakCache;

public class Dates {

	static final WeakCache<Date, String> CACHE_ISO8601 = new WeakCache<Date, String>();
	static final SimpleDateFormat DATE_FORMAT_ISO8601;
	static {
		DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		DATE_FORMAT_ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	public static String iso8601(final Date date) {
		return CACHE_ISO8601.get(date, new CacheBuilder<String>() {
			@Override
			public String build() {
				return DATE_FORMAT_ISO8601.format(date);
			}
		});
	}
	
	private static final WeakCache<Date, String> CACHE_RFC822 = new WeakCache<Date, String>();
	static final SimpleDateFormat RFC822_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	
	public static String rfc822(final Date date) {
		return CACHE_RFC822.get(date, new CacheBuilder<String>() {
			@Override
			public String build() {
				return RFC822_FORMAT.format(date);
			}
		});
	}

}
