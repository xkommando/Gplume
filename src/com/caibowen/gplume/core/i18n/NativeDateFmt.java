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
package com.caibowen.gplume.core.i18n;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.caibowen.gplume.cache.mem.SimpleCache;
import com.caibowen.gplume.common.CacheBuilder;
import com.caibowen.gplume.common.Pair;

/**
 * 
 * three date format:
 * 1. date and time
 * 2. day, month and year
 * 3. month day
 * 
 * @author BowenCai
 *
 */
public class NativeDateFmt implements Serializable{

	private final SimpleDateFormat monthYearFormat;
	private final DateFormat DateTimeFormat;
	private final String[] monthName;
	private final String[] monthNameShort;

	/**
	 * 
	 * pair first en
	 * 		second zh
	 */
	private final SimpleCache<Date, String> cacheDateTime;
	private final SimpleCache<Date, Pair<String, String>> cacheMonthYear;
	private final SimpleCache<Date, String> cacheMonthDay;
	/**
	 * ad hoc for 一月（1）
	 */
	private final SimpleCache<Long, String> cacheMonthName;
	
	
	public NativeDateFmt(Dialect lang) {
		
		DateTimeFormat = DateFormat.getDateTimeInstance(
								DateFormat.LONG,
								DateFormat.SHORT);

		cacheDateTime = new SimpleCache<Date,String>(128);
		cacheMonthYear = new SimpleCache<Date, Pair<String,String>>(128);
		cacheMonthName = new SimpleCache<Long, String>(128);
		cacheMonthDay = new SimpleCache<Date, String>(128);
		
		SimpleDateFormat $;
		String[] $$;
		String[] $$$;
		switch (lang.iso639_2t) {
		case "eng":
			$ = new SimpleDateFormat("MMMMMMMMMMMM yyyy", Locale.US);
			$$ = ENG_MONTH;
			$$$ = EN_MONTH_SHORT;
			break;
		case "zho" :
			$ = new SimpleDateFormat("yyyy'年 ' MMMM", Locale.CHINESE);
			$$ = ZH_MONTH;
			$$$ = ZHO_MONTH_SHORT;
			break;
		case "spa":
			$ = new SimpleDateFormat("MMMMM-yyyy", new Locale("es"));
			$$ = SPA_MONTH;
			$$$ = SPA_MONTH_SHORT;
			break;
		case "gem":
			$ = new SimpleDateFormat("MMM yyyy", new Locale("gem"));
			$$ = GEM_MONTH;
			$$$ = GEM_MONTH_SHORT;
			break;
		case "nld":
		case "dut":
			$ = new SimpleDateFormat("MMM yyyy", new Locale("nld"));
			$$ = NLD_MONTH;
			$$$ = NLD_MONTH_SHORT;
			break;
		default:
			$ = new SimpleDateFormat("MMMMMMMMMMMM yyyy", Locale.US);
			$$ = ENG_MONTH;
			$$$ = EN_MONTH_SHORT;
			break;
		}

		monthYearFormat = $;
		monthName = $$;
		monthNameShort = $$$;
	}

	/**
	 * Pair:
	 * 
	 * first  : day(int string)
	 * second : month and year
	 * 
	 */
	public Pair<String,String> toMonthYear(final Date date) {

		return cacheMonthYear.get(date, new CacheBuilder<Pair<String,String>>() {
			@Override
			public Pair<String, String> build() {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				String t1 = Integer.toString(day);
				@SuppressWarnings("synthetic-access")
				String t2 = monthYearFormat.format(date);
				return new Pair<String, String>(t1, t2);
			}
		});
	}

//	public static void main(String...a) {
//		System.out.println("NativeDateFmt.main()");
//		System.out.println(new NativeDateFmt(CommonLang.SimplifiedChinese).toMonthYear(new Date()));
//	}
//-----------------------------------------------------------------------------
	/**
	 * full date, for comment and post sign(last sentence)
	 * @param date
	 * @return
	 */
	public String toDateTime(final Date date) {
		return cacheDateTime.get(date, new CacheBuilder<String>() {
			@SuppressWarnings("synthetic-access")
			@Override
			public String build() {
				return DateTimeFormat.format(date);
			}
		});
	}

	/**
	 * ad hoc function for 一月（1） January(1)
	 * @param info
	 * @return
	 */
	public String toMonthPost(final Long info) {
		
		return cacheMonthName.get(info, new CacheBuilder<String>() {
			@SuppressWarnings("synthetic-access")
			@Override
			public String build() {
				int month = (int)(info >> 32);
				int number = info.intValue();
				StringBuilder builder = new StringBuilder(48);
				builder.append(monthName[month]);
				builder.append('(').append(Integer.toString(number)).append(')');
				return builder.toString();
			}
		});
	}
	
	/**
	 * post archive
	 * 
	 * @param date
	 * @return
	 */
	public String toMonthDay(final Date date) {
		return cacheMonthDay.get(date, new CacheBuilder<String>() {
			@SuppressWarnings("synthetic-access")
			@Override
			public String build() {
				@SuppressWarnings("deprecation")
				int m = date.getMonth() + 1;
				@SuppressWarnings("deprecation")
				int day = date.getDate();
				return monthNameShort[m] + day;
			}
		});
	}

	private static final long serialVersionUID = -400133790070906634L;
	
	public static final String NAME = NativeDateFmt.class.getName();
//-----------------------------------------------------------------------------
	
	/**
	 * month index start from 1
	 */
	private static final String[] ZH_MONTH = {
		"",
		"\u4e00\u6708",
		"\u4e8c\u6708",
		"\u4e09\u6708",
		"\u56db\u6708",
		"\u4e94\u6708",
		"\u516d\u6708",
		"\u4e03\u6708",
		"\u516b\u6708",
		"\u4e5d\u6708",
		"\u5341\u6708",
		"\u5341\u4e00\u6708",
		"\u5341\u4e8c\u6708",
	};

	private static final String[] ZHO_MONTH_SHORT = {
		"",
		"1.", "2.","3.",
		"4.", "5.", "6.", "7.", "8.",
		"9.", "10.", "11.", "12."	
	};
	private static final String[] ENG_MONTH = {
		"",
		"January", "February","March",
		"April", "May", "June", "July", "August",
		"September", "October", "November", "December"
	};
	
	private static final String[] EN_MONTH_SHORT = {
		"",
		"Jan.", "Feb.","Mar.",
		"Apr.", "May.", "Jun.", "Jul.", "Aug.",
		"Sep.", "Oct.", "Nov.", "Dec."
	};
	
	
	private static final String[] SPA_MONTH = {
		"",
		"enero", "febrero","marzo",
		"abril", "mayo", "junio", "julio", "agosto",
		"septiembre", "octubre", "noviembre", "diciembre"
	};
	
	private static final String[] SPA_MONTH_SHORT = {
		"",
		"enero", "feb","marzo",
		"abr", "mayo", "jun", "Jul", "agosto",
		"sept", "oct", "oct", "dic"
	};
	
	private static final String[] NLD_MONTH = {
		"",
		"januari", "februari","maart",
		"april", "mei", "juni", "juli", "augustus",
		"september", "october", "november", "december"
	};
	// for [Jan 31]
	private static final String[] NLD_MONTH_SHORT = {
		"",
		"jan.", "feb.","maart.",
		"apr.", "mei.", "juni.", "juli.", "aug.",
		"sept.", "okt.", "nov.", "dec."
	};

	private static final String[] GEM_MONTH = {
		"",
		"Januar", "Februar","März",
		"April", "Mai", "Juni", "Juli", "August",
		"September", "Oktober", "November", "Dezember"
	};
	
	private static final String[] GEM_MONTH_SHORT = {
		"",
		"Jän.", "Feb.","März.",
		"Apr.", "Mai.", "Juni.", "Juli.", "Aug.",
		"Sept.", "Okt.", "Nov.", "Dez."
	};

}
