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
package com.caibowen.gplume.sample.controller;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Named;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.event.AppEvent;
import com.caibowen.gplume.event.IAppListener;
import com.caibowen.gplume.event.IEventHook;
import com.caibowen.gplume.i18n.NativePackage;
import com.caibowen.gplume.sample.feature.BirthdayCalculator;
import com.caibowen.gplume.sample.feature.TimeChangedEvent;
import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.note.Handle;

/**
 * 
 * @author BowenCai
 *
 */
public class SampleController {
	
	static final Logger LOG = Logger.getLogger(SampleController.class.getName());
	
	public static String nativeStr(String k, RequestContext context) {
		NativePackage pkg = 
				(NativePackage) context.request.getSession(true)
							.getAttribute(NativePackage.NAME);
		if (pkg == null) {
			pkg = context.getAttr(NativePackage.NAME);
		}
		return pkg.getStr(k);
	}
	
	@Handle({"/",
			"/index",
			"/index.html",
			"/index.jsp"})
	public String index(RequestContext context) {
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		
		System.out.println(context.request.getContextPath());
		
		return "/index.jsp";
	}
	
//	         /act/post/date
	@Handle(value={"/act/post/date"},httpMethods=HttpMethod.POST)
	public void getTime(RequestContext context) {
		int y = context.getIntParam("year", 0);
		int m = context.getIntParam("month", 0);
		int d = context.getIntParam("day", 0);
		Calendar calendar = Calendar.getInstance();
		calendar.set(y, m - 1 + Calendar.JANUARY, d);
		happyBirthday(calendar.getTime(), context);
	}
	
	/**
	 * 
	 * @param date
	 * @param context
	 */
	@Handle(value={"/your-birthday/{date formate like 1992-6-14::Date}"}
			, httpMethods={HttpMethod.GET, HttpMethod.POST})
	public String happyBirthday(Date date, RequestContext context) {
		if (date != null) {
			int dist = birthdayCalculator.dateDistance(date);
			if (dist == 0) {
				context.putAttr("msg", nativeStr("happyBirthDay", context));
			} else {
				context.putAttr("msg", 
						MessageFormat.format(nativeStr("daysToBirthDay", context), dist));
			}
		} else {
			AppContext.broadcaster.register(new IEventHook() {
				@Override
				public void catches(AppEvent event) {
					LOG.info("cought event[" + event.getClass().getName() + "]"
							+ "from source[" + event.getSource() + "]");
				}
			});
			AppContext.broadcaster.register(new IAppListener<TimeChangedEvent>() {
				@Override
				public void onEvent(TimeChangedEvent event) {
					LOG.info("time changed event");
				}
			});
			TimeChangedEvent event = new TimeChangedEvent(this);
			event.setTime(new Date());
			AppContext.broadcaster.broadcast(event);
			
			context.putAttr("msg", nativeStr("dateIsWrong", context));
		}
		context.putAttr("date", date);
		return "/happy.jsp";
	}

	@Named("birthdayCalculator")
	BirthdayCalculator birthdayCalculator;

	public BirthdayCalculator getBirthdayCalculator() {
		return birthdayCalculator;
	}

	public void setBirthdayCalculator(BirthdayCalculator birthdayCalculator) {
		this.birthdayCalculator = birthdayCalculator;
	}
}

