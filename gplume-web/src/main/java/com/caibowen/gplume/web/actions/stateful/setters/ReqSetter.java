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

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.stateful.IStateSetter;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

/**
 * default setter 
 * set value from RequestContext : 
 * @ContextAttr
 * @CookieVal
 * @ReqAttr
 * @ReqParam // no default val
 * @SessionAttr
 *  
 * @author BowenCai
 *
 */
public class ReqSetter implements IStateSetter {

	private static final long serialVersionUID = 8117499794418545935L;


    /**
     * get value from RequestContext
     */
	@Nonnull 
	protected final MethodHandle getter;
	
	@Nonnull
	protected final String name;
	
	// field to be set
	@Nonnull 
	protected final Field field;
	
	protected final boolean nullable;
	
	public ReqSetter(MethodHandle getter, String name, Field field,
			boolean nullable) {
		this.getter = getter;
		this.name = name;
		this.field = field;
		this.nullable = nullable;
	}

	@Override
	public void setWith(@Nonnull RequestContext req, 
					@Nonnull Object state) {

		Object val = null;
		try {
			val = getter.invoke(req, name);
		} catch (Throwable e) {
			if (!nullable)
				throw new RuntimeException(
			"request [" + req.path + "]\r\n"
			+ "failed invoking getter [" + getter + "] to get val named [" + name 
			+ "]\r\n for field [" + field.getName() 
			+ "]\r\n in class [" + state.getClass().getName() + "]" , e);
		}
		if (val == null && !nullable) {
			throw new RuntimeException(
			"null request attribute for non-null property [" 
					+ name + "]\r\n for field [" + field.getName()
					+ "] in class [" + state.getClass().getName() + "] ");
		}
		try {
			field.set(state, val);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			
			if (!nullable)
				throw new RuntimeException(
		"request [" + req.path + "]\r\n"
		+ "failed setting field [" + field.getName()
		+ "]\r\n in class [" + state.getClass().getName() + "]" 
		+ " with val named [" + name 
			+ "] \r\n and value [" + (val == null ? "null" : val.toString()) + "]"
		, e);
		}
		
	}

	@Override
	public String toString() {
		return "ReqSetter [getter=" + getter + ", name=" + name + ", field="
				+ field + ", required=" + nullable + "]";
	}

}
