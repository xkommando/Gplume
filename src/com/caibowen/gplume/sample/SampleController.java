package com.caibowen.gplume.sample;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.caibowen.gplume.core.context.AppContext;
import com.caibowen.gplume.core.i18n.NativePackage;
import com.caibowen.gplume.event.AppEvent;
import com.caibowen.gplume.event.IAppListener;
import com.caibowen.gplume.event.IEventHook;
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
		return pkg.getStr(k);
	}
	
	@Handle({"/Gplume/",
			"/Gplume/index",
			"/Gplume/index.html",
			"/Gplume/index.jsp"})
	public void index(RequestContext context) {
		
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		context.render("/index.jsp");
	}
//	         /Gplume/act/post/date
	@Handle(value={"/Gplume/act/post/date"},httpMethods=HttpMethod.POST)
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
	@Handle({"/Gplume/your-birthday/{date formate like 1992-6-14::Date}"})
	public void happyBirthday(Date date, RequestContext context) {
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
		context.render("/happy.jsp");
	}
	
	@Inject
	BirthdayCalculator birthdayCalculator;

	public BirthdayCalculator getBirthdayCalculator() {
		return birthdayCalculator;
	}

	public void setBirthdayCalculator(BirthdayCalculator birthdayCalculator) {
		this.birthdayCalculator = birthdayCalculator;
	}
}
