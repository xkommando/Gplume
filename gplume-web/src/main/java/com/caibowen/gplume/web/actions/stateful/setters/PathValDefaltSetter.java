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
package com.caibowen.gplume.web.actions.stateful.setters;

import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.PathValResolver;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;


/**
 * 
 * set value from uri
 * if parse or cast failed use default value
 * @author BowenCai
 *
 */
public class PathValDefaltSetter extends ReqDefaultValSetter {

	private static final long serialVersionUID = -4078115932844698160L;
	private static final Logger LOG = LoggerFactory.getLogger(ReqDefaultValSetter.class);
	
	final PathValResolver resolver;
	
	public PathValDefaltSetter(MethodHandle getter, Field field,
			boolean nullable, Object defaultValue
			 , PathValResolver p) {
		super(getter, null, field, nullable, defaultValue);
		resolver = p;
	}
	
	@Override
	public void setWith(RequestContext req, Object state) {
		Object var = null;
		try {
			var = resolver.resolveAndCast(req.path, null);
		} catch (Throwable e) {
			LOG.warn(
			"request [" + req.path + "]\r\n"
			+ "failed invoking getter [" + getter + "] to get val named [" + name 
			+ "]\r\n for field [" + field.getName() 
			+ "]\r\n in class [" + state.getClass().getName()
			+ "]\r\n using default [" + defaultVal + "]",
			e);
		}
		if (var == null) 
			var = defaultVal;
		try {
			field.set(state, var);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			
			if (!nullable)
				throw new RuntimeException(
		"request [" + req.path + "]\r\n"
		+ "failed setting field [" + field.getName()
		+ "]\r\n in class [" + state.getClass().getName() + "]" 
		+ " with arg named [" + resolver.getArgName() 
			+ "] \r\n and value [" + (var == null ? "null" : var.toString()) + "]"
		, e);
		}
	}
}
