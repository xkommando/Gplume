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
package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.core.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * 
 * @author BowenCai
 *
 */
public class PathValResolver implements IPathValResolver {

	final int startIdx; // start idx of arg
	final String argName;
	final Class<?> argType;
	final String suffix;
	
	public PathValResolver(int startIdx, String argName, Class<?> argType,
			String suffix) {
		this.startIdx = startIdx;
		this.argName = argName;
		this.argType = argType;
		this.suffix = suffix;
	}
	
	/* (non-Javadoc)
	 * @see com.caibowen.gplume.web.actions.IpathValResolver#resolveAndCast(java.lang.String, java.lang.String)
	 */
	@Override
	@Nullable
	public Object resolveAndCast(String path, String name) {
		return Converter.slient.translateStr(
				parseArg(path, name), argType);
	}
	
	/* (non-Javadoc)
	 * @see com.caibowen.gplume.web.actions.IpathValResolver#parseArg(java.lang.String, java.lang.String)
	 */
	@Override
	@Nonnull
	public String parseArg(@Nonnull String uri, String name) {
		
		String argVar;
		if (suffix.endsWith("*")) {
			//e.g., abc/arg={}*
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
