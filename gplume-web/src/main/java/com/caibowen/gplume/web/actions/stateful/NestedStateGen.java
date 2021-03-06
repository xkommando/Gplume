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
package com.caibowen.gplume.web.actions.stateful;

import com.caibowen.gplume.web.RequestContext;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;



/**
 * 
 * for non-static nested class, 
 * to get new instance of nested class,
 * a reference to the object of its declaring class is needed
 * 
 * @author BowenCai
 *
 */
class NestedStateGen extends StateGen {

	@Nonnull final Object referred;
	
	public NestedStateGen(List<? extends IStateSetter> setters,
			Constructor<?> ctor, Object ref) {
		super(setters, ctor);
		this.referred = ref;
	}

	
	@Override
	public Object gen(RequestContext req) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Object state = ctor.newInstance(referred);
		try {
			for (IStateSetter setter : setters)
				setter.setWith(req, state);
			return state;
		} catch(Throwable t) {
			return null;
		}
	}
}
