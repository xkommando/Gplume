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
package com.caibowen.gplume.core.context;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.caibowen.gplume.core.IBeanAssembler;
import com.caibowen.gplume.core.XMLBeanAssembler;
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
	
	public static final IBeanAssembler		beanAssembler = XMLBeanAssembler.getInstance();
	public static final Broadcaster			broadcaster = Broadcaster.getInstance();
	
	public static Date now() {
		return defaults.calendar.getTime();
	}

}




