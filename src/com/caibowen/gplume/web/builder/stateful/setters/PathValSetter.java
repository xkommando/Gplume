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
package com.caibowen.gplume.web.builder.stateful.setters;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.PathValParser;



/**
 * set value from uri
 * no default value
 * @PathValue
 * 
 * @author BowenCai
 *
 */
public class PathValSetter extends ReqSetter  {

	private static final long serialVersionUID = 5064087603625572454L;
	
	final PathValParser parser;
	
	PathValSetter(MethodHandle getter, String name, Field field,
			boolean nullable, PathValParser p) {
		super(getter, name, field, nullable);
		this.parser = p;
	}
	
	@Override
	public void setWith(@Nonnull RequestContext req, 
					@Nonnull Object state) {

		Object val = null;
		try {
			val = parser.parseAndCast(req.path, null);
		} catch (Throwable e) {
			if (!nullable)
				throw new RuntimeException(
			"request [" + req.path + "]\r\n"
			+ "failed invoking getter [" + getter + "] to get val named [" + name 
			+ "]\r\n for field [" + field.getName() 
			+ "]\r\n in class [" + state.getClass().getName() + "]" , e);
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

}
