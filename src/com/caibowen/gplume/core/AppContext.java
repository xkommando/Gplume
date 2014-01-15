package com.caibowen.gplume.core;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import com.caibowen.gplume.event.AppStartedEvent;
import com.caibowen.gplume.event.AppEvent;
import com.caibowen.gplume.event.AppListener;
import com.caibowen.gplume.event.Broadcaster;


/**
 * 
 * life circle management
 * 
 * @author BowenCai
 *
 */
public final class AppContext {

	public static final String MANIFEST = "manifest";

	public static final boolean	DEBUG_FLAG = true;

	private static final Logger logger = Logger.getLogger(Broadcaster.class.getName());
	/**
	 * need further consideration
	 */
	public static final Locale LOCATION = Locale.UK;
	
	/**
	 * this calandar is read-only,
	 * changing to calendar has thread-safty issue.
	 */
	public static final Calendar CALENDAR = Calendar.getInstance(LOCATION);

	// stadard charset is not supported by gae
	public static final Charset CHARSET = Charset.forName("UTF-8");

	private static IBeanFactory		beanFactory;
	private static Broadcaster		broadcaster;

	private static long startTime;

	public static Date now() {
		return CALENDAR.getTime();
	}
	
	/*
	 * why not take servlet context as param?
	 * because this AppContext is not specified for web application, 
	 * it can be used for other Java application as well
	 */
	/**
	 * <li> build beans according to the manifest file</li>
	 * <li> register all listeners in the bean factory</li>
	 * 
	 * @param config InputStream of the manifest file
	 */
	static public void init(InputStream config) {

		logger.info("context inicializing...");
		
		startTime = now().getTime();
		
		beanFactory = XMLBeanFactory.getInstance();
		
		// synchronized (beanFactory) {
		beanFactory.load(config);
		beanFactory.build();
		// }
		
		broadcaster = Broadcaster.getInstance();

		beanFactory.inTake(broadcaster.listenerRetreiver);

		logger.info("context inicialized.");
		
		AppStartedEvent event = new AppStartedEvent(beanFactory);
		event.setTime(startTime);
		broadcaster.broadcast(event);
	}
	
	public static Logger getContextLogger() {
		return AppContext.logger;
	}
	
//---------------------------
//			Bean Factory
//---------------------------
	static public<T> T getBean(String id) {
		return beanFactory.getBean(id);
	}
	
	static public void addBean(String id, Object bean) {	
		beanFactory.addBean(id, bean);
	}

//---------------------------
//		Event/Listeners
//---------------------------
	static public void registerListener(AppListener<? extends AppEvent> listener) {
		broadcaster.register(listener);
	}
	static public void broadcast(AppEvent e) {
		broadcaster.broadcast(e);
	}

	private AppContext(){}
}




