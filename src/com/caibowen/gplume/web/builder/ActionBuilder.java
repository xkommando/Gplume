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
package com.caibowen.gplume.web.builder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.builder.actions.Interception;
import com.caibowen.gplume.web.builder.actions.JspAction;
import com.caibowen.gplume.web.builder.actions.RestAction;
import com.caibowen.gplume.web.builder.actions.SimpleAction;
import com.caibowen.gplume.web.builder.actions.ViewAction;
import com.caibowen.gplume.web.view.IView;


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
public class ActionBuilder {
	
//	private static final Logger LOG = Logger.getLogger(ActionBuilder.class.getName());

	// from java.lang.invoke
	protected static final Lookup LOOKUP = MethodHandles.publicLookup();

	protected static final MethodType SIMPLE_TYPE = MethodType.methodType(
			void.class, RequestContext.class);
	
	protected static final MethodType RET_JSP_TYPE = MethodType.methodType(
			String.class, RequestContext.class);
	
	protected static final MethodType RET_JSP_NOPARAM_TYPE = MethodType.methodType(
			String.class);
	
	protected static final MethodType INTERCEPT_TYPE = MethodType.methodType(
			void.class, RequestContext.class, SimpleAction.class);

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
	protected static @Nonnull MethodHandle 
	findMethodeHandle(Method method, MethodType typ) {

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
							+ "]\n of type [" + typ + "]"
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
	static IAction buildAction(String uri, 
									@Nullable Object object, 
									Method method) {
		
		if (uri.indexOf('{') != -1) {// rest uri
			if (uri.lastIndexOf('}') != -1) {
				return RestActionBuilder.build(uri, object, method);
				
			} else {
				throw new IllegalArgumentException("illegal uri ["
						+ uri + "] miss right '{'" + "in class["
						+ object.getClass().getName() + "]  method ["
						+ method + "]");
			}
			
		} else {// average action
			Class<?> retKlass = method.getReturnType();
			Class<?>[] $ = method.getParameterTypes();
			boolean hasRequestContext = $.length > 0 && $[$.length - 1].equals(RequestContext.class);
			
			if (retKlass.equals(String.class)) {
				
				MethodHandle handle = 
				findMethodeHandle(method, 
						hasRequestContext ? 
								RET_JSP_TYPE 
								: RET_JSP_NOPARAM_TYPE);
				
				handle = object != null ? handle.bindTo(object) : handle;
				return new JspAction(uri, handle, hasRequestContext); 
				
			} else if (retKlass.equals(void.class)) {
				MethodHandle handle = findMethodeHandle(method, SIMPLE_TYPE);
				handle = object != null ? handle.bindTo(object) : handle;
				return new SimpleAction(uri, handle);
				
			} else if (IView.class.isAssignableFrom(retKlass)){
				return new ViewAction(uri, method, object, hasRequestContext);
				
			} else {
				throw new NullPointerException(
						"null uri || null controller object || null method handle");
			}
		}
	}

}




