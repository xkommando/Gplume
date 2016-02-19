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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;



/**
 * generate state object, all its properties are set within this generator
 * 
 * @author BowenCai
 *
 */
public class StateGen {

	List<? extends IStateSetter> setters;
    private static Logger LOG = LoggerFactory.getLogger(StateGen.class);

	/**
	 * should be set accessible if necessary
	 */
	Constructor<?> ctor;
	
	public StateGen(List<? extends IStateSetter> setters, Constructor<?> ctor) {
		this.setters = setters;
		this.ctor = ctor;
	}
	
	public Object gen(RequestContext req) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object state = ctor.newInstance();
		try {
			for (IStateSetter setter : setters)
				setter.setWith(req, state);
			return state;
		} catch(Throwable t) {
            LOG.debug("exception on setting properties for [" + state + "]", t);
			return null;
		}
	}
}



