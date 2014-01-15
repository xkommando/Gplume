package com.caibowen.gplume.web;

import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.web.ControlCenter.Request;

/**
 * Actor = Controller  + one handler function of this controller
 * 
 * each handler has one and only one instance, 
 * whereas the handle function of which are many
 * 
 * @author BowenCai
 *
 */
public class Actor {
	
	public final Object			object;
	public final MethodHandle	handle;
	
	public Actor(Object object, MethodHandle handle) {
		super();
		this.object = object;
		this.handle = handle;
	}

	public Object perform(Request request) throws Throwable {
		
		return handle.invoke(object, request);
	}

}
