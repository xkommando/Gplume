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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.web.RequestContext;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;



/**
 * set value from RequestContext : 
 * @ContextAttr
 * @CookieVal
 * @ReqAttr
 * @SessionAttr
 * 
 * setter with default value
 * if cannot get value from request, use the default value.
 * the default value should has been casted from String to the target class.
 * 
 * @author BowenCai
 *
 */
public class ReqDefaultValSetter extends ReqSetter {

	private static final long serialVersionUID = 7151958851575269082L;
	private static final Logger LOG = LoggerFactory.getLogger(ReqDefaultValSetter.class);
	
	@Nullable
	protected final Object defaultVal;
	
	public ReqDefaultValSetter(MethodHandle getter, String name, Field field,
			boolean nullable, Object defaultValue) {
		super(getter, name, field, nullable);
		defaultVal = defaultValue;
	}
	
	@Override
	public void setWith(RequestContext req, Object state) {
		Object var = null;
		try {
			var = getter.invoke(req, name);
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
		+ " with val named [" + name 
			+ "] \r\n and value [" + (var == null ? "null" : var.toString()) + "]"
		, e);
		}
	}

	@Override
	public String toString() {
		return "ReqDefaultValSetter [defaultVal=" + defaultVal + ", getter="
				+ getter + ", name=" + name + ", field=" + field
				+ ", nullable=" + nullable + "]";
	}
}
