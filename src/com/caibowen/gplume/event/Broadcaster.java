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
package com.caibowen.gplume.event;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caibowen.gplume.core.IBeanAssembler;
import com.caibowen.gplume.core.TypeTraits;

/**
 * Register listener and broadcast event.
 * Listeners are stored in slots.
 * 
 * @author BowenCai
 *
 */
public class Broadcaster implements Serializable{
	
	private static final long serialVersionUID = -247132524218855841L;

	private static final Logger LOG = Logger.getLogger(Broadcaster.class.getName());
	
	private static final Object PRESENT = new Object();
	/**
	 * slots: the key is the event type, the value-----HashSet, 
	 * is the listener listen to this kind of event
	 * 
	 *how i wish there is a typedef here 
	 */
	private Map<Class< ? extends AppEvent>, 
					Map<IAppListener<? extends AppEvent>, Object> > slots;
	
	private Map<IEventHook, Object> hooks;
	
	private static Broadcaster handle = null;	
	private Broadcaster() {
		slots = new HashMap<>(16);
		hooks = new LinkedHashMap<IEventHook, Object>();
	}
	
	synchronized public static Broadcaster getInstance() {
		if(handle == null) {
			handle = new Broadcaster();
		}
		return handle;
	}
	
	/**
	 * add new event hook
	 * @param hook
	 * @return true if this set did not already contain the specified element
	 */
	public boolean register(IEventHook hook) {
		return	hooks.put(hook, PRESENT) == null;
	}
	
	/**
	 * register listener to an event,
	 * listeners are stored in different slots
	 * 
	 * @param listener
	 */
	public void register(IAppListener<? extends AppEvent> listener) {

		Class<? extends AppEvent> eventClazz = getEventClazz(listener);

		Map<IAppListener<? extends AppEvent>, Object>
			listeners = slots.get(eventClazz);

		synchronized (slots) {
			
			if (listeners != null) {
				listeners.put(listener, PRESENT);

			} else {
				Map<IAppListener<? extends AppEvent>, Object>
					ls = new LinkedHashMap<IAppListener<? extends AppEvent>, Object>(16);
				ls.put(listener, PRESENT);
				slots.put(eventClazz, ls);
			}
		}
	}
	
	/**
	 * get all listeners to this kind of event
	 * @param eventClazz
	 * @return
	 */
	public Set<IAppListener<? extends AppEvent>>
	getListener(Class<? extends AppEvent> eventClazz) {
		return slots.get(eventClazz).keySet();
	}
	
	/**
	 * 
	 * @param listener
	 * @return
	 */
	public boolean remove(IAppListener<? extends AppEvent> listener) {

		Class<? extends AppEvent> eventClazz = getEventClazz(listener);

		Map<IAppListener<? extends AppEvent>, Object>
			listeners = slots.get(eventClazz);
		
		if (listeners != null) {
			synchronized (slots) {
				listeners.remove(listener);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public boolean removeListeners(Class<? extends AppEvent> eventClazz) {
		synchronized (slots) {
			return slots.remove(eventClazz) != null;
		}
	}
	
	public boolean clearAllListeners() {
		synchronized (slots) {
			slots.clear();
		}
		return true;
	}
	
	public boolean containsHook(IEventHook hook) {
		return hooks.containsKey(hook);
	}
	
	/**
	 * 
	 * @param hook
	 * @return true if hook removed
	 */
	public boolean remove(IEventHook hook) {
		synchronized (hooks) {
			return this.hooks.remove(hook) != null;
		}
	}
	
	public boolean clearHooks() {
		synchronized (hooks) {
			hooks.clear();
		}
		return true;
	}
	/**
	 * listeners will be informed of this event ahead of hooks.
	 * 
	 * note that the order listeners receive event are undetermined,
	 * 
	 * @param event
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void broadcast(AppEvent event) {
		
		if (event == null) {
			return;
		}
		Class<? extends AppEvent> eventClazz = event.getClass();

		Map<IAppListener<? extends AppEvent>, Object> listeners = slots.get(eventClazz);
		
		if (listeners != null) {
			for (IAppListener listener : listeners.keySet()) {
				try {
					listener.onEvent(event);
				} catch (Exception e) {
					LOG.log(Level.SEVERE, 
							"error broadcasting event[" 
							+ event + "] on listener [" + listener + "]"
							, e);
				}
			}
		} else {
				LOG.log(Level.WARNING,
						"no listener for event [" 
						+ event.getClass().getName() + 
						"] registerd ");
		}
		
		for (Map.Entry<IEventHook, Object> hk : hooks.entrySet()) {
			try {
				IEventHook hook = hk.getKey();
				if (hook != null) {
					hook.catches(event);
				}
			} catch (Exception e) {					
				LOG.log(Level.SEVERE, 
					"error catching event[" 
					+ event + "] on hook [" + hk + "]"
					, e);
			}
		}
	}

	Class<? extends AppEvent> getEventClazz(IAppListener<? extends AppEvent> listener) {
		
		Type[] typeParams = listener.getClass().getGenericInterfaces();
		for (Type type : typeParams) {
			@SuppressWarnings("unchecked")
			Class<? extends AppEvent> ec = (Class<? extends AppEvent>)
												TypeTraits.getClass(type, 0);
			if (AppEvent.class.isAssignableFrom(ec)) {
				return ec;
			}
		}
		throw new IllegalStateException(
				"cannot find legal event type from lister class ["
							+ listener.getClass().getName() + "]");
		/**
		 * a listener can listen one event only,
		 * 
		 * that is, a listener class can implement AppListener once only,
		 * therefore, typeParams is of length 1.
		 * 
		 */
//		return (Class<? extends AppEvent>) TypeTraits.getClass(typeParams[0], 0);
	}
	
	/**
	 * visit bean factory and get listener beans registered.
	 */
	public IBeanAssembler.Visitor listenerRetreiver = new IBeanAssembler.Visitor(){
		private static final long serialVersionUID = -750407111421138317L;

		@Override
		public void visit(Object bean) {

			if (bean instanceof IAppListener) {

				@SuppressWarnings("unchecked")
				IAppListener<? extends AppEvent> ls = 
						(IAppListener<? extends AppEvent>) bean;

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
