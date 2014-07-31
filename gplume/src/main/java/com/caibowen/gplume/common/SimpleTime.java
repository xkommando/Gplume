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

import java.util.Date;

/**
 * 
 * 
 * count time by second using 64-bit signed long integer
 * 
 * time starts from 1970-01-01 08:00:00
 * 
 * time range from 24855 B.C. to 24855 A.C.
 * 
 * @author BowenCai
 *
 */
public class SimpleTime extends SimpleTimeBase {

	private static final long serialVersionUID = 3724493616332293280L;

	public SimpleTime() {
		super();
	}
	
	public SimpleTime(Date d) {
		super(d);
	}
	public SimpleTime(int time) {
		impl.down(time);
	}	
	public SimpleTime(int y, int m, int d) {
		super(y, m, d);
	}
	
	public SimpleTime(int y, int m, int d, int h, int minu, int s) {
		super(y, m, d, h, minu, s);
	}
	
	public SimpleTime now() {
		return new SimpleTime(new Date());
	}
	public Date toJDKDate() {
		return new Date(toJDKTime(impl.time));
	}


	public boolean before(final SimpleTime st) {
		return impl.time < st.impl.time;
	}
	
	public boolean after(final SimpleTime st) {
		return impl.time < st.impl.time;
	}
	
	public boolean before(final Date d) {
		return impl.time < fromJDKTime(d.getTime());
	}
	
	public boolean after(final Date d) {
		return impl.time > fromJDKTime(d.getTime());
	}
	
//	public void parse(String s) {
//		
//	}
	
	public long getTime() {
		return impl.time;
	}
	public int year() {
		return impl.year;
	}
	public int month() {
		return impl.month;
	}
	public int week() {
		return impl.week;
	}
	public int day() {
		return impl.day;
	}
	public int hour() {
		return impl.hour;
	}
	public int minute() {
		return impl.minute;
	}
	
	public int second() {
		return impl.second;
	}

//	//1992-06-14T19:30:00.000Z
//	public String timeFormatted() {
//		
//	}
//	public String dateFormatted() {
//		return year() + 
//	}
//	
//	public String simpleTimeFormatted() {
//	}
//	public String simpleDateFormatted() {	
//	}

//	@Override
//	public String toString() {
//		return "SimpleTime Year: " + year() + " Month: " + month() + " Day: " + day() 
//				+ " Hour: " + hour() + " Monute: " + minute() + " Second: " + second();
//	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (impl.time ^ (impl.time >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof SimpleTime) {
			SimpleTime other = (SimpleTime) obj;
			
			return other.impl.time == impl.time;
		} else {
			return false;
		}
	}
	
}


