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
package com.caibowen.gplume.web.builder.stateful;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import com.caibowen.gplume.web.RequestContext;



/**
 * set object from the IoC rather than object from servlet.
 * 
 * @author BowenCai
 *
 */
class BeanSetter implements IStateSetter {

	private static final long serialVersionUID = -7793827519595236754L;
	
	@Nonnull 
	protected final Field field;
	@Nonnull 
	protected final Object bean;
	
	protected final boolean nullable;
	
	BeanSetter(Field field, Object bean, boolean nullable) {
		this.field = field;
		this.bean = bean;
		this.nullable = nullable;
	}
	
	@Override
	public void setWith(RequestContext req, Object state) {
		try {
			field.set(state, bean);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			
			if (!nullable)
				throw new RuntimeException(
		"request [" + req.path + "]\r\n"
		+ "failed setting field [" + field.getName()
		+ "]\r\n in class [" + state.getClass().getName() + "]" 
		+ " with bean named [" + (bean == null ? "null" : bean.toString()) + "]"
		, e);
		}
	}

}
