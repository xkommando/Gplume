package com.caibowen.gplume.event;

import java.util.EventObject;

/**
 * In applications, you must use specific event class,
 *  distinguished event classes makes it possible to separate 
 * listeners to different slot for the specific event.
 * 
 * Thereby, this class is made abstract.
 *  in this way, client won't be able to use java.util.EventObject 
 * which is unclear and not specified. 
 * 
 * 
 * @author BowenCai
 * @since 2013-12-29
 */
@SuppressWarnings("serial")
public abstract class AppEvent extends EventObject {

	public AppEvent(Object arg0) {
		super(arg0);
	}

}
