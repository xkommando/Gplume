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
class BeanEditor {

	static void setBeanProperty(Class<?> bnClass, Object bean,
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

	static void setStrProperty(Class<?> bnClass, Object bean,
			String propName, String varStr) throws Exception {

		Method setter = TypeTraits.findSetter(bnClass, propName);

		Class<?>[] paramTypes = setter.getParameterTypes();

		if (paramTypes.length == 1) {
			Object var = Converter.slient.translateStr(varStr, paramTypes[0]);
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
	static void setListProperty(Class<?> bnClass, Object bean,
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
