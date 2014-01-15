package com.caibowen.gplume.common;

import java.io.Serializable;
import java.util.Date;


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
		return s * 1000L + META_OFFSET;
	}
	public static int fromJDKTime(long date) {
		return (int)((date- META_OFFSET) / 1000L);
	}
	
//-----------------------------------------------------------------------------


	/**
	 * 708480000000L
	 * 708550200000L
	 */
	public static final long META_OFFSET = 708550200000L;
	
	public static final int META_YEAR = 1992;
	public static final int META_ZODIAC = 9;

	public static final class Impl {
		
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
	 * 按 公历算的，所以在腊月到正月这段时间有问题
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
