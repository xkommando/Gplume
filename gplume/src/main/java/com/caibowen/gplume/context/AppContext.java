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
package com.caibowen.gplume.context;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.caibowen.gplume.context.bean.IBeanAssembler;
import com.caibowen.gplume.context.bean.XMLBeanAssembler;
import com.caibowen.gplume.event.Broadcaster;


/**
 * 
 * life circle management
 * 
 * @author BowenCai
 *
 */
public abstract class AppContext {
	
//	public static final boolean	DEBUG_FLAG = true;
	
//-----------------------------------------------------------------------------
//			global properties
	
	public static class defaults {
		
		public static TimeZone timeZone = TimeZone.getTimeZone("GMT");
		
		static Calendar calendar = Calendar.getInstance(timeZone);

		public static final Charset charSet = Charset.forName("UTF-8");
	}

	/**
	 * thread local variables
	 */
	public static final ThreadLocal<Calendar> currentCalendar = new ThreadLocal<Calendar>() {
		@Override
		protected Calendar initialValue() {
			return Calendar.getInstance(defaults.timeZone);
		}
	};
	
//-----------------------------------------------------------------------------
//				3 basic/top-level components	
	/**
	 * config file location, written in web.xml
	 */
	public static final String MANIFEST = "manifest";
	public static final String LOCALE 	= "locale";
	public static final String TIME_ZONE = "timezone";
	
	public static final IBeanAssembler		beanAssembler = XMLBeanAssembler.instance();
	public static final Broadcaster			broadcaster = Broadcaster.instance();
	
	public static Date now() {
		return defaults.calendar.getTime();
	}

}




