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
import java.util.Map;

import javax.inject.Named;









import javax.servlet.http.Cookie;

import com.caibowen.gplume.annotation.Semaphored;
import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.event.AppEvent;
import com.caibowen.gplume.event.IAppListener;
import com.caibowen.gplume.event.IEventHook;
import com.caibowen.gplume.i18n.NativePackage;
import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import com.caibowen.gplume.sample.feature.BirthdayCalculator;
import com.caibowen.gplume.sample.feature.TimeChangedEvent;
import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.IRequestProcessor;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.annotation.Handle;
import com.caibowen.gplume.web.annotation.Intercept;
import com.caibowen.gplume.web.builder.IAction;

/**
 * 
 * @author BowenCai
 *
 */
public class SampleController {

//	@Intercept(value = { "/*" })
//	public void demo(RequestContext context, IAction action) throws Throwable {
//		if (hasLogedIn(context)) {
//			action.perform(context);
//			after(context);
//		} else {
//			context.jumpTo("/login");
//		}
//	}

	public boolean hasLogedIn(RequestContext context) {
		return false;
	}
	
	public void after(RequestContext context) {
		
	}
	
	@Semaphored(permit=100, fair=false)
	@Handle({"/test"})
	public void name(RequestContext context) {
		System.out.println("SampleController.name()");
		Map<String, Object> map = context.request.getParameterMap();
		for (Map.Entry<String, Object> e : map.entrySet()) {
			System.out.print("[" + e.getKey() + "]  ");
			String[] strs = (String[]) e.getValue();
			for (String string : strs) {
				System.out.print("[" + string + "]");
			}
			System.out.println();
		}
		context.render("index");
	}
	
	
	static final Logger LOG = LoggerFactory.getLogger(SampleController.class.getName());
	
	public static String nativeStr(String k, RequestContext context) {
		NativePackage pkg = 
				(NativePackage) context.session(true)
							.getAttribute(NativePackage.NAME);
		if (pkg == null) {
			pkg = context.attr(NativePackage.NAME);
		}
		return pkg.getStr(k);
	}
	
	@Handle({"/",
			"/index",
			"/index.html",
			"index"})
	public String index(SampleController self, RequestContext context) {
		System.out.println("SampleController.index()");
		System.out.println(AppContext.beanAssembler.getBean("birthdayCalculator"));
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		context.putAttr("test_int", 123);
		
		context.session(true).setAttribute("msg", "ctx msg");
		context.session(true).setAttribute("aaa", 15999);
		
		Integer[] ints = new Integer[3];
		ints[0] = new Integer(1);
		ints[1] = new Integer(2);
		ints[2] = new Integer(3);
		
		Cookie ck = new Cookie("ck_name1", "ck_value1");
		ck.setComment("ck comment 1");
		ck.setDomain("ck_domain1");
		ck.setMaxAge(500);
		ck.setPath("/ck_path");
		ck.setSecure(true);
		ck.setVersion(2);
		context.addCookie(ck);
		
		context.session(true).setAttribute("ints", ints);
		
		System.out.println(context.toString());
		
		return "index";
	}
	
	
	@Handle(value={"/act/post/date"},httpMethods=HttpMethod.POST)
	public void getTime(RequestContext context) {
		int y = context.getIntParam("year", 0);
		int m = context.getIntParam("month", 0);
		int d = context.getIntParam("day", 0);
		Calendar calendar = Calendar.getInstance();
		calendar.set(y, m - 1 + Calendar.JANUARY, d);
		happyBirthday(calendar.getTime(), context);
	}
	

	@Handle(value={"/s/your-birthday/{date formate like 1992-6-14::Date}"}
			, httpMethods={HttpMethod.GET, HttpMethod.POST})
	public static String happyBirthday(RequestContext context) {
		Date date = context.attr("date formate like 1992-6-14");
		return new SampleController().happyBirthday(date, context);
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
					LOG.info("cought event{0} from source {1}"
							, event.getClass().getSimpleName()
							, event.getSource());
				}
			});
			AppContext.broadcaster.register(new IAppListener<TimeChangedEvent>() {
				@Override
				public void onEvent(TimeChangedEvent event) {
					LOG.info("time changed {0}", event.getTime());
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
	
//	@Handle(value={"/your-birthday/{date formate like 1992-6-14::Date}"},
//			httpMethods={HttpMethod.GET, HttpMethod.POST})
//	public FreemarkerView id(Date date, RequestContext context) {
//		return new FreemarkerView("/index.", date);
//	}
	
	public static class FreemarkerView {
		FreemarkerView(String a, Date b){
			
		}
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

