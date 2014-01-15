package com.caibowen.gplume.event;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import com.caibowen.gplume.core.IBeanFactory;
import com.caibowen.gplume.core.TypeTraits;

/**
 * Register listener and broadcast event.
 * Listeners are stored in slots.
 * @author BowenCai
 *
 */
public class Broadcaster {

	private static final Logger LOG = Logger.getLogger(Broadcaster.class.getName());
	
	/**
	 * slots: the key is the event type, the value-----HashSet, 
	 * is the listener listen to this kind of event
	 * 
	 *how i wish there is a typedef here 
	 */
	private HashMap<Class< ? extends AppEvent>, 
					HashSet<AppListener<? extends AppEvent> > > slots;
	
	private static Broadcaster handle = null;	
	private Broadcaster() {
		slots = new HashMap<>(16);
	}
	synchronized public static Broadcaster getInstance() {
		
		if(handle == null) {
			handle = new Broadcaster();
		}
		return handle;
	}

	public void register(AppListener<? extends AppEvent> listener) {

		Class<? extends AppEvent> eventClazz = getEventClazz(listener);

		HashSet<AppListener<? extends AppEvent> > listeners 
													= slots.get(eventClazz);

		synchronized (slots) {
			
			if (listeners != null) {
				listeners.add(listener);

			} else {
				HashSet<AppListener<? extends AppEvent>> ls = new HashSet<>(16);
				ls.add(listener);
				slots.put(eventClazz, ls);
			}

		}
	}
	
	public void remove(AppListener<? extends AppEvent> listener) {

		Class<? extends AppEvent> eventClazz = getEventClazz(listener);

		HashSet<AppListener<? extends AppEvent> > listeners = slots.get(eventClazz);
		
		if (listeners != null) {
			synchronized (slots) {
				listeners.remove(listener);
			}
		}
	}
	
	public void clear() {
		synchronized (slots) {
			slots.clear();
		}
	}
	
	/**
	 * 
	 * note that the order listeners receive event are undetermined,
	 * 
	 * @param event
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void broadcast(AppEvent event) {
		
		if (event == null) {
			LOG.warning("null event ");
			return;
		}
		
		Class<? extends AppEvent> eventClazz = event.getClass();

		HashSet<AppListener<? extends AppEvent>> listeners = slots.get(eventClazz);
		if (listeners != null) {
			for (AppListener listener : listeners) {
				listener.onEvent(event);
			}
		} else {
				LOG.info("no listener for event [" 
						+ event.getClass().getName() + 
						"] registerd ");
		}
	}

	Class<? extends AppEvent> getEventClazz(AppListener<? extends AppEvent> listener) {
		
		Type[] typeParams = listener.getClass().getGenericInterfaces();
		/**
		 * a listener can listen one event only,
		 * 
		 * that is, a listener class can implement AppListener once only,
		 * therefore, typeParams is of length 1.
		 * 
		 */
		return (Class<? extends AppEvent>) TypeTraits.getClass(typeParams[0], 0);
	}
	
	/**
	 * visit bean factory and get listener beans registered.
	 */
	public IBeanFactory.Visitor listenerRetreiver = new IBeanFactory.Visitor(){
		@Override
		public void visit(Object bean) {

			if (bean instanceof AppListener) {

				@SuppressWarnings("unchecked")
				AppListener<? extends AppEvent> ls = 
						(AppListener<? extends AppEvent>) bean;

				Broadcaster.this.register(ls);
			}
		}
		
	};


}


//Type[] typeParams = listener.getClass().getGenericInterfaces();
//
//for (Type type : typeParams) {
//	Class<? extends AppEvent> eventClazz
//			= (Class<? extends AppEvent>) TypeTraits.getClass(type, 0);
//
//	List<AppListener<? extends AppEvent>> listeners = slots.get(eventClazz);
//	
//	if (listeners != null) {
//		listeners.add(listener);
//	} else {
//		ArrayList<AppListener<? extends AppEvent>> ls = new ArrayList<>(8);
//		ls.add(listener);
//		slots.put(eventClazz, ls);
//	}
//}
//* it is associated with the order listeners listed in the manifest file, 
//* or the order it is manually registered.
//* But there is no guarantee that a certain listener will be notified ahead of others.
//*  
