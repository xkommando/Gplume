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
import com.caibowen.gplume.web.actions.PathValResolver;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;



/**
 * set value from uri
 * no default value
 * @PathVal
 * 
 * @author BowenCai
 *
 */
public class PathValSetter extends ReqSetter  {

	private static final long serialVersionUID = 5064087603625572454L;
	// resolver resolve
	final PathValResolver resolver;
	
	public PathValSetter(MethodHandle getter, Field field,
			boolean nullable, PathValResolver p) {
		super(getter, null, field, nullable);
		this.resolver = p;
	}
	
	@Override
	public void setWith(@Nonnull RequestContext req, 
					@Nonnull Object state) {

		Object val = null;
		try {
			val = resolver.resolveAndCast(req.path, null);
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
		+ " with val named [" + resolver.getArgName() 
			+ "] \r\n and value [" + (val == null ? "null" : val.toString()) + "]"
		, e);
		}
	}

}
