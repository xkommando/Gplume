package com.caibowen.gplume.event;

import java.util.EventListener;

/**
 * a listener will receive only events that it support
 * a lister class can implement one and one only AppListener in its type hierarchy
 * 
 * @author BowenCai
 *
 * @param <T> the event type that this listener supports
 */
public interface AppListener<T extends AppEvent> extends EventListener {
	
	public void onEvent(T event);

}
