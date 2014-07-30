package com.caibowen.gplume.web.builder;

import java.lang.reflect.Method;
import java.util.Date;

import javax.annotation.Nonnull;

import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.stateful.StatefulActionBuilder;


/**
 * 
 * get builders for different actions
 * 
 * @author BowenCai
 *
 */
public class BuilderProxy {

	public static IAction
	buildAction(String uri, Method method, @Nonnull Object object) {
		
		BuilderAux.ctrlMap.put(object.getClass(), object);
		
		if (uri.indexOf('{') != -1)
			return RestActionBuilder.buildAction(uri, object, method);
		else {
			for (Class<?> c: method.getParameterTypes()) {
				// 3 kind of classes supported by Converter
				if (!Klass.isPrimitiveOrWrapper(c)
					&& !c.equals(Date.class)
					&& !c.equals(RequestContext.class))
					// customer
					return StatefulActionBuilder.buildAction(uri, object, method);
			}
		}
		return SimpleActionBuilder.buildAction(uri, object, method);
	}
	
	public static Interception 
	buildInterception(final String u, Object object, Method method) {
		return BuilderAux.buildInterception(u, object, method);
	}
	
	
}
