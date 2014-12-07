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

import com.caibowen.gplume.context.IBeanAssembler;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.stateful.IStateSetter;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;



/**
 * set object from the IoC rather than object from servlet.
 * this setter will try to get bean 
 * from IBeanAssembler with the given ID each time bean is needed.
 * @Inject
 * @Named
// * Note that instead of retrieving bean from bean factory each time needed
// * this setter will keep a ref to the bean at instantiation
// * thus the bean injected maybe outdated.
 * 
 * @author BowenCai
 *
 */
public class BeanSetter implements IStateSetter {

	private static final long serialVersionUID = -7793827519595236754L;
	
	@Nonnull 
	protected final Field field;

	@Nonnull 
	protected IBeanAssembler assembler;
	@Nonnull
	protected final String id;
	
	protected final boolean nullable;
	
	public BeanSetter(Field field, IBeanAssembler assembler, String name, boolean nullable) {
		this.field = field;
		this.id = name;
		this.nullable = nullable;
		this.assembler = assembler;
	}
	
	@Override
	public void setWith(RequestContext req, Object state) {
		Object bean = assembler.getBean(id);
		if (bean == null && !nullable) {
			throw new RuntimeException("request [" + req.path + "]\r\n"
					+ "failed setting field [" + field.getName()
					+ "]\r\n in class [" + state.getClass().getName() + "]"
					+ "\r\n Cause: Cannot get bean with ID [" + id + "]");
		}
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
