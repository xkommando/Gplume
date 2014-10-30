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
package com.caibowen.gplume.web.actions.builder;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Klass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.web.IAction;
import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.actions.PathValResolver;
import com.caibowen.gplume.web.actions.RestAction;
import com.caibowen.gplume.web.actions.ViewRestAction;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 
 * middle class between base and real-impl, providing support for rest
 * 
 * @author BowenCai
 *
 */
class RestActionBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RestActionBuilder.class.getName());

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
	public static IAction buildAction(String uri, Object object, Method method) {

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
			LOG.warn(
			"Handle argument type is primitive, which may cause NullPointerException with invalid input" +
			"\r\nhttp handle [" + uri + "], in class[" 
					+ method.getDeclaringClass().getName() 
					+ "] method [" 
					+ method.getName()  +"]");
		}
		PathValResolver pr = new PathValResolver(lq, argName, argType, suffix);
		//------------------------------- argName  MethodType
		
		
		// whether put arg as parameter or put arg as attribute
		Class<?>[] $ = method.getParameterTypes();
		boolean hasRequset = $.length > 0 && $[$.length - 1].equals(RequestContext.class);
		
		MethodHandle handle = getRestHandle(method, argType, object, hasRequset);
		
		boolean inMethodCall = $.length > 0 && ( !$[0].equals(RequestContext.class));
		
		Class<?> retKalss = method.getReturnType();
        IViewResolver resolver;
		if (retKalss.equals(void.class)) {
			return new RestAction(effectiveURI, handle, method, inMethodCall, pr);

		} else if (null != (resolver = BuilderAux.viewMatcher.findMatch(retKalss))) {
			return new ViewRestAction(effectiveURI, method, object,
                    inMethodCall, hasRequset, pr, resolver);

		} else {
			throw new IllegalArgumentException(
			"cannot find view resolver for  method[" + method + "] in object[" + object + "]");
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
	private @Nullable static MethodHandle
	getRestHandle(Method method, Class<?> argClass, 
					Object obj, boolean hasRequest) {
		
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
				mType = BuilderAux.RET_JSP_TYPE;
			} else {
				mType = BuilderAux.SIMPLE_TYPE;
			}
		}
//		mType = isMatch ? mType : retClass.equals(String.class) ?  RET_JSP_TYPE : SIMPLE_TYPE;
		MethodHandle handle = BuilderAux.findMethodeHandle(method, mType);
		
		return Modifier.isStatic(method.getModifiers()) 
				? handle : handle.bindTo(obj);
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
