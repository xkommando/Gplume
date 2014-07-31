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
package com.caibowen.gplume.common;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * simple date, precision is second.
 * time starts from 1970-01-01 08:00:00
 * @author BowenCai
 *
 */
public class SimpleTimeBase implements Serializable {//, Cloneable {

	private static final long serialVersionUID = 2906984517055778500L;
	
	protected static final long MS_MINUTE 		= 1000L;
	protected static final long SECOND_MINUTE 	= 60L;
	
	protected static final long MINUTE_HOUR 		= 60L;
	protected static final long SECOND_HOUR 		= SECOND_MINUTE * MINUTE_HOUR;
	
	protected static final long HOUR_DAY			= 24;
	protected static final long MINUTE_DAY 		= MINUTE_HOUR * HOUR_DAY;
	protected static final long SECOND_DAY 		= SECOND_HOUR * HOUR_DAY;
	
	protected static final long DAY_WEEK 		= 7;
	protected static final long HOUR_WEEK 		= DAY_WEEK * HOUR_DAY;
	protected static final long MINUTE_WEEK		= DAY_WEEK * MINUTE_DAY;
	protected static final long SECOND_WEEK		= DAY_WEEK * SECOND_DAY;
	
	protected static final int[] MONTH_YEAR_SMALL = {
		31, 28, 31, 30,
		31, 30, 31, 31,
		30, 31, 30, 31
	};		
	protected static final int[] DAY_YEAR_BIG = {
		0,
		31, 60, 91, 121,
		152, 182, 213, 244,
		274, 305, 335
	};
	
	protected static final int[] MONTH_YEAR_BIG = {
		31, 29, 31, 30,
		31, 30, 31, 31,
		30, 31, 30, 31
	};
	protected static final int[] DAY_YEAR_SMALL = {
		0, 
		31, 59, 90, 120,
		151, 181, 212, 243,
		273, 304, 334
	};
//-------------------------------------------------------------------
	public static final String MONTH[] ={
		"Unknown",//MONTH[0]
		"January",	"February",		"March",	"April",
		"May",		"June",			"July",		"August",
		"September","October",		"November",	"December"
	};
	public static final String WEEK[] ={
		"Unknown",
		"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"
	};
	public static final String SIMPLE_MONTH[] ={
		"Unknown",
		"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
	};

	public static final String SIMPLE_WEEK[] ={
		"Unknown",
		"Mon","Tue","Wed","Thu","Fr","Sat","Sun"
	};

	public static final String SHENG_XIAO[] ={
		"Unknown",
		"Rat",		"Ox",		"Tiger",	"Rabbit",
		"Dragon",	"Snake",	"Horse",	"Goat",
		"Monkey",	"Rooster",	"Dog",		"Pig"
	};
	
	public static long toJDKTime(long s) {
		return s * 1000L + OFFSET;
	}
	
	public static long fromJDKTime(long date) {
		return ((date- OFFSET) / 1000L);
	}
	
//-----------------------------------------------------------------------------
	public static void main(String...args) throws ParseException {
		System.out.println(
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.parse("0001-01-01 00:00:00").getTime());
		
	}

	/**
	 * offset from
	 * 63513504000000
	 * 62135798400000L;
	 */
	public static final long OFFSET = calculateOffset();
	private static long calculateOffset() {
		long offset = 0L;
		try {
			offset = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1970-01-01 08:00:00").getTime();
		} catch (ParseException e) {
			offset = 62135798400000L;
		}
		return offset;
	}
	
	public static final int META_YEAR = 1992;
	public static final int META_ZODIAC = 9;

	public static final class Impl implements Serializable {
		
		private static final long serialVersionUID = 8650738806242341565L;

		long time;
		
		int second;
		int minute;
		int hour;
		int day;
		int week;
		int month;
		int year;

		static private boolean isLeapYear(int year) {
			return ((year & 3) == 0)
					&& ((year % 100) != 0 || (year % 400) == 0);
		}
		
		/**
		 * Gregorian Calendar
		 * 
		 * @param year
		 * @return second since this year
		 */
		static private long secondFromYear(long year) {

			long leapYears = year / 100;
			if (year < 0) {
				leapYears = ((year + 3) >> 2) - leapYears
						+ ((leapYears + 3) >> 2) - 1;
			} else {
				leapYears = (year >> 2) - leapYears + (leapYears >> 2);
				if (((year & 3) == 0)
						&& ((year % 100) != 0 || (year % 400) == 0)) {
					leapYears--;
				}
			}
			return (year * 365L + (leapYears)) * SECOND_DAY;
		}

		static public long secondFromMonthOfYear(int year, int month) {
			return 
					( isLeapYear(year) ?
							DAY_YEAR_BIG[month - 1]
						:	DAY_YEAR_SMALL[month - 1]
					);
		}
		
		/**
		 * to time::long
		 * @param y
		 * @param m
		 * @param d
		 * @param h
		 * @param minu
		 * @param s
		 */
		synchronized void up(int y, int m, int d, int h, int minu, int s) {
			
			time = secondFromYear(y) + secondFromMonthOfYear(y, m)
					+ d * SECOND_DAY
					+ h * SECOND_HOUR
					+ minu * SECOND_MINUTE
					+ s;
		}
		
		synchronized void down(long t) {
			time = t;
//			year = t / (SECOND_DAY * 365);
		}
	}
//-----------------------------------------------------------------------------

	protected Impl impl = new Impl();
	
	public SimpleTimeBase() {
		impl.down(fromJDKTime(new Date().getTime()));
	}
	public SimpleTimeBase(Date d) {
		impl.down(fromJDKTime(d.getTime()));
	}
	public SimpleTimeBase(int y, int m, int d) {
		impl.up(y, m, d, 0,0,0);
	}
	
	public SimpleTimeBase(int y, int m, int d, int h, int minu, int s) {
		impl.up(y, m, d, h, minu, s);
	}

	/**
	 * Chinese zodiac
	 * �� ������ģ����������µ��������ʱ��������
	 * @return
	 */
	@Deprecated
	public String shengxiao() {
		
		int idx = (impl.year - META_YEAR) % 12;
		idx += META_ZODIAC;
		idx = idx < 0 ? (idx + 12) : idx > 12 ? (idx - 12) : idx;
		return SHENG_XIAO[idx];
	}


}
