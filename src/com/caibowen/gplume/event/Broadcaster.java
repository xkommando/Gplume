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
import java.lang.ref.Reference;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.caibowen.gplume.common.StrongRef;
import com.caibowen.gplume.common.WeakRef;
import com.caibowen.gplume.context.bean.IAssemlberVisitor;
import com.caibowen.gplume.core.TypeTraits;
import com.caibowen.gplume.logging.Logger;
import com.caibowen.gplume.logging.LoggerFactory;

/**
 * Register listener and broadcast event.
 * Listeners are stored in slots.
 * 
 * @author BowenCai
 *
 */
public class Broadcaster implements Serializable{
	
	private static final long serialVersionUID = -247132524218855841L;

	static final Logger LOG = LoggerFactory.getLogger(Broadcaster.class);
	
	private static final Object PRESENT = new Object();
	/**
	 * slots: the key is the event type, the value-----HashSet, 
	 * is the listener listen to this kind of event
	 * 
	 *how i wish there is a typedef here 
	 */
	private Map<Class< ? extends AppEvent>, 
					Map<Reference<? extends IAppListener<? extends AppEvent>>, Object> > slots;
	
	private Map< Reference<? extends IEventHook>, Object> hooks;
	
	private static Broadcaster handle = null;	
	private Broadcaster() {
		slots = new HashMap<>(16);
		hooks = new LinkedHashMap<>();
	}
	
	synchronized public static Broadcaster getInstance() {
		if(handle == null) {
			handle = new Broadcaster();
		}
		return handle;
	}
	

	/**
	 * strong ref to this hook
	 * @param hook
	 * @return
	 */
	public<T extends IEventHook> boolean 
	register(T hook) {
		return register(hook, true);
	}
	/**
	 * add new event hook
	 * @param hook
	 * @param strongRef, maintain a strong ref or weakRef
	 * @return true if this set did not already contain the specified element
	 */
	public<T extends IEventHook> boolean 
	register(T hook, boolean isStrongRef) {
		Reference<? extends IEventHook> ref = 
				isStrongRef ? new StrongRef<T>(hook) 
							: new WeakRef<T>(hook);
				
		return	hooks.put(ref, PRESENT) == null;
	}
	
	/**
	 * register listener to an event,
	 * listeners are stored in different slots
	 * 
	 * @param listener
	 * @param isStrongRef 
	 */
	public<T extends IAppListener<? extends AppEvent>> void 
	register(T listener, boolean isStrongRef) {

		Class<? extends AppEvent> eventClazz = getEventClazz(listener);
		Reference<T> ref = isStrongRef ? new StrongRef<T>(listener)
										: new WeakRef<T>(listener);
				
		Map<Reference<? extends IAppListener<? extends AppEvent> >, Object>
			listeners = slots.get(eventClazz);

		synchronized (slots) {
			
			if (listeners != null) {
				listeners.put(ref, PRESENT);

			} else {
				Map<Reference<? extends IAppListener<? extends AppEvent> >, Object>
					ls = new LinkedHashMap<>(16);
				ls.put(ref, PRESENT);
			}
		}
	}
	/**
	 * 
	 * @param listener
	 */
	public<T extends IAppListener<? extends AppEvent>> void 
	register(T listener) {
		register(listener, true);
	}
	
	/**
	 * get all listeners to this kind of event
	 * @param eventClazz
	 * @return
	 */
	public Set<Reference<? extends IAppListener<? extends AppEvent> >>
	getListener(Class<? extends AppEvent> eventClazz) {
		
		Map<Reference<? extends IAppListener<? extends AppEvent> >, Object>
		listeners = slots.get(eventClazz);
		return listeners != null ? listeners.keySet() : null;
	}
	
	/**
	 * remove one listener
	 * 
	 * @param listener
	 * @return
	 */
	public<T extends IAppListener<? extends AppEvent>> boolean 
	remove(T listener) {

		Class<? extends AppEvent> eventClazz = getEventClazz(listener);

		Map<Reference<? extends IAppListener<? extends AppEvent> >, Object>
			listeners = slots.get(eventClazz);
		
		if (listeners != null) {
			WeakRef<IAppListener<? extends AppEvent>> 
			wref = new WeakRef<IAppListener<? extends AppEvent>>(listener);
			StrongRef<IAppListener<? extends AppEvent>> 
			sref = new StrongRef<IAppListener<? extends AppEvent>>(listener);
			synchronized (slots) {
				listeners.remove(sref);
				listeners.remove(wref);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * remove all listeners to this kind of event
	 * @param eventClazz
	 * @return
	 */
	public boolean removeListeners(Class<? extends AppEvent> eventClazz) {
		synchronized (slots) {
			return slots.remove(eventClazz) != null;
		}
	}
	
	/**
	 * clear all listeners
	 * @return
	 */
	public boolean clearAllListeners() {
		synchronized (slots) {
			slots.clear();
		}
		return true;
	}
	
	public<T extends IEventHook> boolean containsHook(T hook) {
		WeakRef<? extends IEventHook> 
		wref = new WeakRef<>(hook);
		StrongRef<? extends IEventHook> 
		sref = new StrongRef<>(hook);
		return hooks.containsKey(wref)
				|| hooks.containsKey(sref);
	}
	
	/**
	 * 
	 * @param hook
	 * @return true if hook removed
	 */
	public<T extends IEventHook> boolean remove(T hook) {
		WeakRef<? extends IEventHook> 
		wref = new WeakRef<>(hook);
		StrongRef<? extends IEventHook> 
		sref = new StrongRef<>(hook);
		synchronized (hooks) {
			return this.hooks.remove(wref) != null
					|| this.hooks.remove(sref) != null;
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
	public<T extends AppEvent> void broadcast(T event) {
		
		if (event == null) {
			return;
		}
		Class<? extends AppEvent> eventClazz = event.getClass();

		Map<Reference<? extends IAppListener<? extends AppEvent> >, Object> 
		listeners = slots.get(eventClazz);
		
		if (listeners != null) {
			for (Reference<? extends IAppListener<? extends AppEvent>> 
					listener : listeners.keySet()) {
				
				@SuppressWarnings("unchecked")
				IAppListener<T> lsr = (IAppListener<T>) listener.get();
				if (lsr != null) {
					try {
						lsr.onEvent(event);
					} catch (Exception e) {
						LOG.error("broadcasting event["
								+ event + "] on listener [" + listener + "]", e);
					}
				}
			}
		} else {
				LOG.warn( "no listener for event [" 
						+ event.getClass().getName() + 
						"] registerd ");
		}
		
		for (Map.Entry<? extends Reference<? extends IEventHook>, Object> 
				hk : hooks .entrySet()) {
			
			if (hk.getKey().get() != null) {
				try {
					IEventHook hook = hk.getKey().get();
					if (hook != null) {
						hook.catches(event);
					}
				} catch (Exception e) {
					LOG.error("error catching event[" + event
							+ "] on hook [" + hk + "]", e);
				}
			}
		}
	}
	
	/**
	 * a listener can listen one event only,
	 * 
	 * that is, a listener class can implement AppListener once only,
	 * therefore, typeParams is of length 1.
	 * 
	 */
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

	}
	
	/**
	 * visit bean factory and get listener beans registered.
	 */
	public IAssemlberVisitor listenerRetreiver = new IAssemlberVisitor() {
		
		@Override
		public void visit(Object bean) {

			if (bean instanceof IAppListener) {

				@SuppressWarnings("unchecked")
				IAppListener<? extends AppEvent> ls = 
						(IAppListener<? extends AppEvent>) bean;

				Broadcaster.this.register(ls);
				LOG.info("Add listener[" 
					+ ls.getClass().getName() + "]");
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
