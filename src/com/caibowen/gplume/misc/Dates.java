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
package com.caibowen.gplume.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.caibowen.gplume.cache.mem.WeakCache;
import com.caibowen.gplume.common.CacheBuilder;

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
