package com.caibowen.gplume.web.builder.stateful.setters;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import com.caibowen.gplume.web.RequestContext;



/**
 * <pre>
 * for @ReqParam with default val
 * the getter is :
 * public int getIntParam(String name, int defaultVar)
 * instead of 
 * public Integer getIntParam(String name)
 * </pre>
 * @author BowenCai
 *
 */
public class ReqParamDefaultValSetter extends ReqDefaultValSetter {

	private static final long serialVersionUID = -1127541424658339130L;
	private static final Logger LOG = LoggerFactory.getLogger(ReqParamDefaultValSetter.class);
	
	public ReqParamDefaultValSetter(MethodHandle getter, String name, Field field,
			boolean nullable, Object defaultValue) {
		super(getter, name, field, nullable, defaultValue);
	}
	
	@Override
	public void setWith(RequestContext req, Object state) {
		Object var = null;
		try {
			var = getter.invoke(req, name, defaultVal);
		} catch (Throwable e) {
			LOG.warn(
			"request [" + req.path + "]\r\n"
			+ "failed invoking getter [" + getter + "] to get val named [" + name 
			+ "]\r\n for field [" + field.getName() 
			+ "]\r\n in class [" + state.getClass().getName(),
			e);
		}
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
}
