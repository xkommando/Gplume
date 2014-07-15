package com.caibowen.gplume.web.builder;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.stateful.StatefulActionBuilder;


/**
 * 
 * get builders for different actions
 * 
 * @author BowenCai
 *
 */
public class BuilderFactory {

//	if (object != null)
//		ctrlMap.put(object.getClass(), object);
//	
//	if (uri.indexOf('{') != -1) {// rest uri
//		if (uri.lastIndexOf('}') != -1) {
//			return buildRest(uri, object, method);
//			
//		} else {
//			throw new IllegalArgumentException("illegal uri ["
//					+ uri + "] miss right '{'" + "in class["
//					+ object.getClass().getName() + "]  method ["
//					+ method + "]");
//		}
//		
//	} else {// average action
	static SimpleActionBuilder simpleBuilder = new SimpleActionBuilder();
	static RestActionBuilder restBuilder = new RestActionBuilder();
	static StatefulActionBuilder statefulBuilder = new StatefulActionBuilder();
	
	public static IActionBuilder getBuilder(String uri, Method method, @Nonnull Object ctrl) {
		BuilderHelper.ctrlMap.put(ctrl.getClass(), ctrl);
		
		if (uri.indexOf('{') != -1)
			return restBuilder;
		else {
			for (Class<?> c: method.getParameterTypes()) {
				if (!Klass.isPrimitiveOrWrapper(c)
					&& !c.equals(RequestContext.class))
					return statefulBuilder;
			}
		}
		return simpleBuilder;
	}
}
