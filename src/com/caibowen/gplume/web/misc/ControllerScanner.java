/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.web.misc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.caibowen.gplume.misc.ClassFinder;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.AbstractControlCenter;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.action.Action;
import com.caibowen.gplume.web.note.Handle;
import com.caibowen.gplume.web.note.Intercept;


/**
 * Usage:
 * 
 *     
    <bean class="com.caibowen.gplume.web.misc.ControllerScanner">
        <property name="pkg" value="com.caibowen.web.conroller"/>
        <property name="doInject" value="true"/>
        <property name="controlCenterCallBack" ref="controlCenter"/>
    </bean>
 * 
 * 
 * @author BowenCai
 *
 */
public class ControllerScanner {

//	Logger LOG = Logger.getLogger(ControllerScanner.class.getName());
	
	List<Object> controllers;
	public List<String> pkgs;
	private boolean callbacked = false;
	AbstractControlCenter controlCenter;
	
	@Inject public void setPackages(List<String> packages) {
		Set<Object> ctrls = new HashSet<Object>();
		for (String pkg : packages) {
			ctrls.addAll(findControllers(pkg));
		}
		List<Object> tmp = new ArrayList<Object>(ctrls);
		this.controllers = tmp;
		
		if (!callbacked) {
			if (controlCenter != null) {
				controlCenter.setControllers(this.controllers);
				this.callbacked = true;
				this.controllers = null;
				this.controlCenter = null;
			}
		}
	}

	/**
	 * callback control center
	 * @param controlCenter
	 * @throws Exception
	 */
	@Inject
	public void setControlCenterCallBack(AbstractControlCenter controlCenter) throws Exception {
		if (controllers != null) {
			controlCenter.setControllers(controllers);
			callbacked = true;				
			this.controllers = null;
			this.controlCenter = null;
		} else {
			this.controlCenter = controlCenter;
			callbacked = false;
		}
	}

	
	private static List<Object> findControllers(String pkg) {
		
		if (!Str.Utils.notBlank(pkg)) {
			throw new IllegalArgumentException(
					"no pkg specified["+pkg+']');
		}
		
		ArrayList<Object> ctrls = new ArrayList<Object>(16);

		List<Class<?>> allClazz =  ClassFinder.find(pkg, 
									Thread.currentThread().getContextClassLoader());
		try {
			
			for (Class<?> class1 : allClazz) {
				if (mayBeController(class1)) {
//					LOG.log(Level.INFO,
//							"\t>>>>>find controller[" + class1.getName() + "]");
					ctrls.add(class1.newInstance());
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("error init class", e);
		}
		return ctrls;
	}
	
	
	private static boolean mayBeController(Class<?> clazz) {
		
		for (Method method : clazz.getMethods()) {
			
			if (method.isAnnotationPresent(Handle.class)
				&& method.getReturnType().equals(void.class)) {
				
				Class<?> params[] = method.getParameterTypes();
				for (Class<?> class1 : params) {
					if (class1.equals(RequestContext.class)) {
						return true;
					}
				}
				return false;
				
			} else if (method.isAnnotationPresent(Intercept.class)) {
				Class<?> params[] = method.getParameterTypes();
				return params.length == 2 
						&& params[0].equals(RequestContext.class)
						&& params[1].equals(Action.class);
			}
		}
		return false;
	}
}
