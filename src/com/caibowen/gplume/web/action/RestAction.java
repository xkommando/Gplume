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

import com.caibowen.gplume.core.Converter;
import com.caibowen.gplume.web.RequestContext;

/**
 * 
 * @author BowenCai
 *
 */
public class RestAction extends Action {
	
	private static final long serialVersionUID = 7479824844662522176L;

	final int startIdx; // start idx of arg
	final String argName;
	final Class<?> argType;
	final String suffix;
	final boolean inMethodCall;
	
	public RestAction(String uri, MethodHandle handle, 
						int start, String name, Class<?> type, String s, boolean call) {
		
		super(uri, handle);
		startIdx = start;
		argName = name;
		argType = type;
		suffix = s;
		inMethodCall = call;
	}
	
	@Override
	public void perform(RequestContext requestContext) throws Throwable {
		Object var = Converter.castStr(parseArg(requestContext.uri), argType);
		// System.out.println("name[" + argName + "]   type[" +
		// argType.getSimpleName() + "] suffix[" + suffix + "]");
		// System.out.println("  value  ["+parseArg(requestContext.uri)+"]  ");
		requestContext.putAttr(ACTION_NAME, this);

		if (inMethodCall) {
			methodHandle.invoke(var, requestContext);
		} else {
			requestContext.putAttr(argName, var);
			methodHandle.invoke(requestContext);
		}

	}
	
	/**
	 * 
	 * 
	 * @param uri
	 * @return
	 * @see ActionBuilder
	 */
	public String parseArg(String uri) {
		
		String argVar;
		if (suffix.endsWith("*")) {
			return argVar = uri.substring(startIdx);
		} else {
			int idx = uri.lastIndexOf(suffix);
			if (idx != -1) {
				/**
				 * e.g. abc/arg=123.html
				 * 				   ^ suffix
				 */
				argVar = uri.substring(startIdx, idx);
			} else {
				/**
				 * e.g. abc/arg=123
				 */
				argVar = uri.substring(startIdx);
			}
		}
		return argVar;
	}
	
	public String getArgName() {
		return argName;
	}
	
	public Class<?> getArgType() {
		return argType;
	}

	public int getStartIdx() {
		return startIdx;
	}

	public String getSuffix() {
		return suffix;
	}
}
