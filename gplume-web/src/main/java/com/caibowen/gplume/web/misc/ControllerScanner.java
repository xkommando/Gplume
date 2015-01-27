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

import com.caibowen.gplume.context.bean.InitializingBean;
import com.caibowen.gplume.misc.ClassFinder;
import com.caibowen.gplume.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.web.BaseControlCenter;
import com.caibowen.gplume.web.IAction;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.annotation.Handle;
import com.caibowen.gplume.web.annotation.Intercept;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Usage:
 * 
 *     
    <bean class="com.caibowen.gplume.web.misc.ControllerScanner">
        <property id="pkg" value="com.caibowen.web.conroller"/>
        <property id="doInject" value="true"/>
        <property id="controlCenterCallBack" ref="controlCenter"/>
    </bean>
 * 
 * 
 * @author BowenCai
 *
 */
public class ControllerScanner implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(ControllerScanner.class.getName());
	
	List<Object> controllers;
	BaseControlCenter controlCenter;

	public List<Object> getControllers() {
		return controllers;
	}

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
	public void setControlCenter(BaseControlCenter controlCenter) throws Exception {
		this.controlCenter = controlCenter;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		controlCenter.setControllers(controllers);
	}
	
	private static List<Object> findControllers(String pkg) {
		
		if (Str.Utils.isBlank(pkg)) {
			throw new IllegalArgumentException(
					"no pkg specified["+pkg+']');
		}
		
		ArrayList<Object> ctrls = new ArrayList<>(64);

		List<Class<?>> allClazz =  ClassFinder.find(pkg, 
									Thread.currentThread().getContextClassLoader());
			for (Class<?> class1 : allClazz) {
				if (mayBeController(class1)) {
					LOG.debug("\t>>> find controller[" + class1.getName() + "]");
                    Object ctl = null;
                    try {
                        ctl = class1.newInstance();
                    } catch (Exception e) {
                        LOG.error("Could not instantiate web controller[" + class1.getName() + "] with default constructor", e);
                        continue;
                    }
                    ctrls.add(ctl);
				}
			}

		return ctrls;
	}
	
	/**
	 * has method annotated with @Handle
	 * private classes and private methods will be set accessible
	 * @param clazz
	 * @return
	 */
	private static boolean mayBeController(Class<?> clazz) {
		
//		/**
//		 * example:
//		 * 
//		 *  @Controller("base_path")
//		 * 	class WebHandler {}
//		 * 
//		 * @Handle(value={"/test"})
//		 * class WebHandler {}
//		 */
//		if (clazz.isAnnotationPresent(Controller.class)) {
//			return true;
//		}
		final int mod = clazz.getModifiers();
		final boolean isAbstract = Modifier.isAbstract(mod);
		final boolean isNonStaticInner = 
				clazz.isMemberClass() && !Modifier.isStatic(mod);
		
		for (Method method : clazz.getMethods()) {
			
			if (method.isAnnotationPresent(Handle.class)) {
				
				if (isAbstract) {
				 	String urls = Str.Utils.join(method.getAnnotation(Handle.class).value(), " ");
				 	LOG.warn(
				 			"handle for [{0}] of method [{1}] "
				 			+ "in class [{0}] is not operational "
				 			+ "because the class is abstract"
				 			, urls
				 			, method.toString()
				 			, clazz.getName());
					 return false;
					 
				} else if (isNonStaticInner) {
					 String urls = Str.Utils.join(method.getAnnotation(Handle.class).value(), " ");
					 LOG.warn(
							 "handle for [{0}] of method [{1}] "
							 + "in class [{0}] is not operational "
							 + "because the class is a non-static nested class"
							 , urls
							 , method.toString()
							 , clazz.getName());
					 return false;
					 
				} else {
					return true;
				}
				
			} else if (method.isAnnotationPresent(Intercept.class)) {
				Class<?> params[] = method.getParameterTypes();
				boolean matchParams = params.length == 2
						&& params[0].equals(RequestContext.class)
						&& params[1].equals(IAction.class);
				
				if (!matchParams) {
					return false;
					
				} else if (isAbstract) {
				 	String urls = Str.Utils.join(method.getAnnotation(Handle.class).value(), " ");
				 	LOG.warn(
				 			"Intercept for [{0}] of method [{1}] "
				 			+ "in class [{0}] is not accessible "
				 			+ "because the class is abstract"
				 			, urls
				 			, method.toString()
				 			, clazz.getName());
					 return false;
					 
				} else if (isNonStaticInner) {
					 String urls = Str.Utils.join(method.getAnnotation(Handle.class).value(), " ");
					 LOG.warn(
							 "Intercept for [{0}] of method [{1}] "
							 + "in class [{0}] is not accessible "
							 + "becase the class is a non-static nested class"
							 , urls
							 , method.toString()
							 , clazz.getName());
					 return false;
					 
				} else {
					return true;
				}
				
			}
		}

		
		return false;
	}

}
