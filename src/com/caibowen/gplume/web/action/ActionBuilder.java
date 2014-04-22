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
package com.caibowen.gplume.web.action;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.misc.Classes;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.Interception;
import com.caibowen.gplume.web.RequestContext;


/**
 * Build Action Object base on handle method and uri
 * 
 * if the method is non-static, controller object will be binded to the methodHandle
 * 
 * For RestAction(parameter in URI), you can declare the argument in the method interface
 * (as the first parameter, and the second also the last parameter, is RequestContext)
 *
 * you can also write the method as other plain handle, 
 * this way, the argument will be put into the RequstContext as a attribute, with the name specified in your URI
 * example:
 * with annotation @Hanlde({"/abc/def/{arg-name::Integer}.html"})
 * you can write method as 
 * cotrollerMethod(Type arg, RequestContext c) {
 * 	// use the arg here, arg may be null
 * }
 * or
 * cotrollerMethod(RequestContext c) {
 * 		Type var = c.getAttr("arg-name");
 * }
 * 
 * it is highly recommended that you use no primitive class in the method declaration
 * since the string arg extracted from actual URL will be convert to a non-primitive value first, 
 * and if the conversion failed, null is returned, 
 * which will cause a NullPointerException in the auto-boxing
 * 
 * this class is for ActionFactory and is not visible for outer classes
 * 
 * @author BowenCai
 *
 */
class ActionBuilder {
	
	private static final Logger LOG = Logger.getLogger(ActionBuilder.class.getName());

	// from java.lang.invoke
	protected static final Lookup LOOKUP = MethodHandles.publicLookup();

	protected static final MethodType HANDLE_TYPE = MethodType.methodType(
			void.class, RequestContext.class);

	protected static final MethodType INTERCEPT_TYPE = MethodType.methodType(
			void.class, RequestContext.class, Action.class);
	
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
				if ( !Classes.isAssignable(params[i], typParams[i])) {
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
	/**
	 * @Handle(uri={"/abc/{erf::date}"})
	 * void handle(RequestContext req); OK, value put to request attribute
	 * void handle(Date date, RequestContext req); OK, value as arg
	 * 
	 * why not unreflect ? 
	 * to get detailed info in debugging
	 * 
	 * @param controller
	 * @param method
	 * @param typ
	 * @return
	 */
	private static MethodHandle findMethodeHandle(Method method, 
													MethodType typ) {

		String mName = method.getName();
		Class<?> ctrlClazz = method.getDeclaringClass();
		MethodHandle actionhandle = null;
		
		try {
			if (Modifier.isStatic(method.getModifiers())) {
				
				actionhandle = LOOKUP.findStatic(ctrlClazz, mName,
						typ);
			} else {
				actionhandle = LOOKUP.findVirtual(ctrlClazz, mName,
						typ);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(
					"\nError making Interception : cannot find method ["
							+ method
							+ "]\nfrom type [" + typ + "]"
							+"\nin class ["
							+ ctrlClazz.getName()
							+ "]");
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
					"Error making Interception : cannot access method ["
							+ method
							+ "] in class ["
							+ ctrlClazz.getName()
							+ "]");
		}
		return actionhandle;
	}
	
	
	static Interception 
	buildInterception(String u, 
						Object object, 
						Method method) {
		
		MethodHandle handle = findMethodeHandle(method, INTERCEPT_TYPE);
//		method.
		handle = object == null ? handle : handle.bindTo(object);
		return new Interception(u , handle);
	}
//	public static void main(String...a) {
//		String uri = "/uri/{test::int}/aas*";
//		System.out.println("012345678901234567890");
//		System.out.println(uri);
//		RestAction action = (RestAction) build(uri, null, null);
//		System.out.println("getEffectiveURI: " + action.getEffectiveURI());
//		System.out.println("idx : " + action.startIdx);
//		System.out.println("name : " + action.argName);
//		System.out.println("suffix : " + action.suffix);
//		System.out.println("arg: " + action.parseArg("/uri/78923/aas--asdf"));
//		System.out.println("type : " + action.argType.getSimpleName());
//	}
	
	/**
	 * @param uri
	 * @param object
	 * @param handle
	 * @return Action or RestAction
	 * @see RestAction
	 */
	static Action buildAction(String uri, @Nullable Object object, Method method) {
		
		if (uri.indexOf('{') != -1) {// rest uri
			if (uri.lastIndexOf('}') != -1) {
				return buildRest(uri, object, method);
				
			} else {
				throw new IllegalArgumentException("illegal uri ["
						+ uri + "] miss right '{'" + "in class["
						+ object.getClass().getName() + "]  method ["
						+ method + "]");
			}
			
		} else {// average action
			
			MethodHandle handle = findMethodeHandle(method, HANDLE_TYPE);
			if (handle != null) {
				handle = object == null ?
							handle : handle.bindTo(object);
				
				return new Action(uri, handle);
				
			} else {
				throw new NullPointerException(
						"null uri || null controller object || null method handle");
			}
		}
	}

	/**
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
	private static final RestAction buildRest(String uri, Object object, Method method) {

		final int lq = uri.indexOf('{');
		final int rq = uri.lastIndexOf('}');
		
		StringBuilder builder = new StringBuilder(uri.length());
		for (int i = 0; i < lq; i++) {
			builder.append(uri.charAt(i));
		}
		builder.append('*');
		final String effectiveURI = builder.toString();
		//------------------------------- effectiveURI
		
		builder.setLength(0);
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
					"\r\nhttp handle [" + uri + "], in class[" + method.getDeclaringClass().getName() + "] method [" + method.getName()  +"]");
		}
		
		
		//------------------------------- argName  MethodType
		if (Str.Utils.notBlank(argName)) {
			
			MethodType type = MethodType.methodType(void.class,
													argType, RequestContext.class);
			/**
			 * cotrollerMethod(Type arg, RequestContext c)
			 * or
			 * cotrollerMethod(RequestContext c)
			 */
			final boolean isMatch = isTypeMatch(method, type);
			MethodType mType = isMatch ? type : HANDLE_TYPE;
			
			MethodHandle handle = findMethodeHandle(method, mType);
			
			handle = object == null ? handle : handle.bindTo(object);

			return new RestAction(effectiveURI, handle,
								lq, argName, argType, suffix, 
								isMatch);
			
		} else {
			throw new IllegalArgumentException(
					"arg name cannot be empty. class[" 
						+ object.getClass().getName() 
						+ "] method  [" + method + "]");
		}
	}
}
