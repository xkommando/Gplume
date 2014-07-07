package com.caibowen.gplume.web.builder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.JspRestAction;
import com.caibowen.gplume.web.builder.actions.RestAction;
import com.caibowen.gplume.web.builder.actions.ViewRestAction;
import com.caibowen.gplume.web.view.IView;
import com.sun.istack.internal.Nullable;

public class RestActionBuilder extends ActionBuilder {

	private static final Logger LOG = Logger.getLogger(RestActionBuilder.class.getName());

	/**
	 * 
	 * four type:
	 * 
	 * String 	func(Arg arg, RequestContext context);
	 * void 	func(Arg arg, RequestContext context);
	 * 
	 * 
	 * String 	func(RequestContext context);
	 * void 	func(RequestContext context);
	 * 
	 * for uri:
	 * <pre><code>
	 * uri={"/param2={number}*"}  
	 * 			=> /param2=abcdefgetiji...
	 * 					   ^ str value 	  ^ 
	 * 
	 * uri={"/param3={number::double}.html"} 
	 * 			=> /param2=abcdefgetiji....html  		
	 * 					   ^ double var  ^ ^ suffix
	 * 		OR  => /param2=abcdefgetiji...
	 * 					   ^ double var   ^  no '.html' found, no suffix 
	 *   
	 * uri={"/param3={number::double}"} 
	 * 			=>  /param2=abcdefgetiji...
	 * 						^ double var   ^ , no '.html' found, no suffix   
	 * </code></pre>
	 * @param uri
	 * @param object
	 * @param method
	 * @return
	 */
	protected static final RestAction 
	build(String uri, Object object, Method method) {

		final int lq = uri.indexOf('{');
		final int rq = uri.lastIndexOf('}');
		
		StringBuilder builder = new StringBuilder(uri.length());
		for (int i = 0; i < lq; i++) {
			builder.append(uri.charAt(i));
		}
		builder.append('*');
		final String effectiveURI = builder.toString();
		builder.setLength(0);
		//------------------------------- effectiveURI
		
		int stop = uri.lastIndexOf('*');
		stop = stop == -1 ? uri.length() : stop;
		for (int i = rq + 1; i < stop; i++) {
			builder.append(uri.charAt(i));
		}
		final String suffix = builder.toString();
		//------------------------------- suffix, argType
		
		String argName = uri.substring(lq + 1, rq);
		final int typeIdx = argName.lastIndexOf("::");
		Class<?> argType;
		if (typeIdx != -1) {
			argType = Converter.getClass(argName.substring(typeIdx + 2));
			
			if (argType == null) {
				argType = String.class;
			} else {
				argName = argName.substring(0, typeIdx);
			}
		} else {
			argType = String.class;
		}
		if (argType.isPrimitive()) {
			LOG.warning(
			"Handle argument type is primitive, which may cause NullPointerException with invalid input" +
			"\r\nhttp handle [" + uri + "], in class[" 
					+ method.getDeclaringClass().getName() 
					+ "] method [" 
					+ method.getName()  +"]");
		}
		
		//------------------------------- argName  MethodType
		
		
		// whether put arg as parameter or put arg as attribute
		Class<?>[] $ = method.getParameterTypes();
		boolean hasRequset = $.length > 0 && $[$.length - 1].equals(RequestContext.class);
		
		MethodHandle handle = getRestHandle(method, argType, object, hasRequset);
		
		boolean inMethodCall = $.length > 0 && ( !$[0].equals(RequestContext.class));
		
		Class<?> retKalss = method.getReturnType();
		
		if (retKalss.equals(String.class)) {
			return new JspRestAction(effectiveURI, handle, lq, argName, argType, suffix, inMethodCall, hasRequset);
		} else if (retKalss.equals(void.class)) {
			return new RestAction(effectiveURI, handle, lq, argName, argType, suffix, inMethodCall);
		
		} else if (IView.class.isAssignableFrom(retKalss)) {
			return new ViewRestAction(effectiveURI, method, object, lq, argName, argType, suffix, inMethodCall, hasRequset);
		} else {
			throw new IllegalArgumentException(
			"unidentified method[" + method + "] in object[" + object + "]");
		}
	
	}
	
	

	
	/**
	 * <pre>
	 * String 	func(Arg arg, RequestContext context);
	 * void 	func(Arg arg, RequestContext context);
	 * 
	 * 
	 * String 	func(RequestContext context);
	 * void 	func(RequestContext context);
	 * </pre>
	 * returns null if 
	 * SomeCustomerView 	func(RequestContext context);
	 * 
	 * @param method
	 * @param argClass
	 * @param obj
	 * @return null if not found
	 */
	private static @Nullable MethodHandle getRestHandle(Method method, 
											Class<?> argClass, 
											Object obj,
											boolean hasRequest) {
		
		Class<?> retClass = method.getReturnType();
		if (!retClass.equals(String.class) && !retClass.equals(void.class)) {
			return null;
		}
		
		
		/**
		 * first try find method that the path variable is one of the method's parameter
		 */
		MethodType mType = null;
		if (hasRequest) 
			mType = MethodType.methodType(
					retClass, 
					argClass, 
					RequestContext.class);
		else
			mType = MethodType.methodType(retClass, argClass);
		
		/**
		 * check if path variable is the parameter, e.g.
		 * 
		 * Ret cotrollerMethod(Type arg, RequestContext c) or
		 * Ret cotrollerMethod(RequestContext c)
		 */
		boolean isMatch = isTypeMatch(method, mType);
		
		/**
		 * not in parameter list
		 */
		if (!isMatch) {
			if (!hasRequest) {
				throw new IllegalArgumentException(
						"path variable is not decleared as method parameters,"
						+ " and RequstContext is not decleared as parameters."
						+ " in method[" + method.getName() + "]");
			}
			
			if (retClass.equals(String.class)) {
				mType = RET_JSP_TYPE;
			} else {
				mType = SIMPLE_TYPE;
			}
		}
//		mType = isMatch ? mType : retClass.equals(String.class) ?  RET_JSP_TYPE : SIMPLE_TYPE;
		MethodHandle handle = findMethodeHandle(method, mType);
		return obj != null ? handle.bindTo(obj) : handle;
	}
	
	
	/**
	 * real method params has arg and value is assignable
	 * 
	 *  cotrollerMethod(Type arg, RequestContext c) {
	 * 	// use the arg here, arg may be null
 	 * }
 	 * 
	 * @param method
	 * @param mType
	 * @return
	 */
	private static boolean isTypeMatch(Method method, MethodType mType) {
		
		Class<?>[] params = method.getParameterTypes();
		Class<?>[] typParams = mType.parameterArray();

		if (typParams.length == params.length) {
			for (int i = 0; i < typParams.length; i++) {
				if ( !Klass.isAssignable(params[i], typParams[i])) {
					throw new IllegalArgumentException(
							"type mismatch, method param type[" + params[i] + "]"
							+ " param type[" + typParams[i] + "]"
							+"\n in class[" + method.getDeclaringClass().getName() + "]"
							+ "  method [" + method.getName() + "]");
				}
			}
			return true;
			
		} else if (params.length == 1 && params[0].equals(RequestContext.class)) {
			return false;
		} else {
			throw new IllegalArgumentException("cannot build action from method["
					+ method.getName() + "] in class[" 
					+ method.getDeclaringClass().getClass().getName() + "]"
					+ "\n check your method type and @Handle uri");
		}
	}
	
}