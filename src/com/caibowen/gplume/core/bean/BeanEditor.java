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
package com.caibowen.gplume.core.bean;

import java.lang.reflect.Method;
import java.util.List;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.core.TypeTraits;

/**
 * 
 * @author BowenCai
 *
 */
public class BeanEditor {

	public static void setBeanProperty(Class<?> bnClass, Object bean,
			String propName, Object var) throws Exception {

		Method method = TypeTraits.findSetter(bnClass, propName);
		if (method == null) {
			throw new IllegalStateException("in class [" + bnClass.getName()
					+ "]  cannot find setter for [" + propName + "]");
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length == 1) {
			method.invoke(bean, var);

		} else {
			throw new IllegalStateException("in class [" + bnClass.getName()
					+ "]   setter [" + method.getName()
					+ "] has more than one parameters");
		}
	}

	public static void setStrProperty(Class<?> bnClass, Object bean,
			String propName, String varStr) throws Exception {

		Method setter = TypeTraits.findSetter(bnClass, propName);

		Class<?>[] paramTypes = setter.getParameterTypes();

		if (paramTypes.length == 1) {
			Object var = Converter.slient.translateStr(varStr, paramTypes[0]);
//System.out.println(bean.getClass().getSimpleName() + "   " + varStr + "   " + var.getClass().getSimpleName());
			setter.invoke(bean, var);

		} else {
			throw new IllegalStateException("in class [" + bnClass.getName()
					+ "]  setter [" + setter.getName()
					+ "]  has more than one parameters");
		}
	}

	/**
	 * assign list or array
	 * 
	 * @param bnClass
	 * @param bean
	 * @param propName
	 * @param varList
	 * @throws Exception
	 */
	public static void setListProperty(Class<?> bnClass, Object bean,
			String propName, List<?> varList) throws Exception {

		Method setter = TypeTraits.findSetter(bnClass, propName);

		if (setter == null) {
			throw new IllegalStateException("in class [" + bnClass.getName()
					+ "]  cannot find setter for [" + propName + "]");
		}

		Class<?>[] paramTypes = setter.getParameterTypes();

		if (paramTypes != null && paramTypes.length == 1) {
			if (paramTypes[0].isAssignableFrom(varList.getClass())) {
				setter.invoke(bean, varList);
			} else {
				@SuppressWarnings("unchecked")
				Object var = Converter.translateList((List<String>) varList,
						bnClass, propName);
				setter.invoke(bean, var);
			}

		} else {
			throw new IllegalStateException("in class [" + bnClass.getName()
					+ "], setter[" + setter.getName()
					+ "] has more than one parameters");
		}
	}

}
