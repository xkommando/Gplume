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
package com.caibowen.gplume.logging;


/**
 * 
 * @author BowenCai
 *
 */
public abstract class LoggerFactory {

	private static LoggerFactory factory;
    
	synchronized public static void setLoggerFactory(LoggerFactory f) {
    	factory = f;
    }
	
    public static LoggerFactory getLoggerFactory() {
		if (factory != null)
			factory = new JdkLoggerFactory();
		
		return factory;
	}
    
    public static Logger getLogger(Class<?> cls) {
    	return getLoggerFactory().getLoggerImpl(cls);
    }

    public static Logger getLogger(String name) {
    	return getLoggerFactory().getLoggerImpl(name);
    }
    


    protected abstract Logger getLoggerImpl(Class<?> cls);

    protected abstract Logger getLoggerImpl(String name);
}
