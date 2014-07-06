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
package com.caibowen.gplume.web.misc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.caibowen.gplume.context.bean.InitializingBean;
import com.caibowen.gplume.misc.ClassFinder;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.AbstractControlCenter;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.SimpleAction;
import com.caibowen.gplume.web.note.Controller;
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
public class ControllerScanner implements InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(ControllerScanner.class.getName());
	
	List<Object> controllers;
	AbstractControlCenter controlCenter;
	
	@Inject public void setPackages(List<String> packages) {
		// avoid redundant obj
		Set<Object> ctrls = new HashSet<Object>();
		for (String pkg : packages) {
			ctrls.addAll(findControllers(pkg));
		}
		this.controllers = new ArrayList<Object>(ctrls);
	}

	/**
	 * callback control center
	 * @param controlCenter
	 * @throws Exception
	 */
	@Inject
	public void setControlCenterCallBack(AbstractControlCenter controlCenter) throws Exception {
		this.controlCenter = controlCenter;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		controlCenter.setControllers(controllers);
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
					LOG.log(Level.CONFIG,
							"\t>>> find controller[" + class1.getName() + "]");
					ctrls.add(class1.newInstance());
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("error init class", e);
		}
		return ctrls;
	}
	
	
	private static boolean mayBeController(Class<?> clazz) {
		/**
		 * example:
		 * 
		 *  @Controller("base_path")
		 * 	class WebHandler {}
		 * 
		 * @Handle(value={"/test"})
		 * class WebHandler {}
		 */
		if (clazz.isAnnotationPresent(Controller.class)
				|| clazz.isAnnotationPresent(Handle.class)) {
			return true;
		}
		
		for (Method method : clazz.getMethods()) {
			
			if (method.isAnnotationPresent(Handle.class)) {
				return true;
//				Class<?> params[] = method.getParameterTypes();
//				for (Class<?> class1 : params) {
//					if (class1.equals(RequestContext.class)) {
//						return true;
//					}
//				}
//				return false;
				
			} else if (method.isAnnotationPresent(Intercept.class)) {
				Class<?> params[] = method.getParameterTypes();
				return params.length == 2 
						&& params[0].equals(RequestContext.class)
						&& params[1].equals(SimpleAction.class);
			}
		}
		return false;
	}

}
