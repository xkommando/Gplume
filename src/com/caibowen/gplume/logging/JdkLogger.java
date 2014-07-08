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

import java.text.MessageFormat;
import java.util.logging.Level;

/**
 */
public class JdkLogger implements Logger {


//-----------------------------------------------------------------------------
//					Logger Impl
    
    
    private java.util.logging.Logger log;

    public JdkLogger(String name) {
        this.log = java.util.logging.Logger.getLogger(name);
    }

    @Override
	public boolean isTraceEnabled() {
        return log.isLoggable(Level.FINEST);
    }
	
    @Override
	public void trace(String msg, Object... args) {
        log.log(Level.FINEST, MessageFormat.format(msg, args));
	}
	
    @Override
	public void trace(String msg, Throwable ex, Object... args) {
        log.log(Level.FINEST, MessageFormat.format(msg, args), ex);
    }


    
    @Override
	public boolean isInfoEnabled() {
        return log.isLoggable(Level.INFO);
    }

	@Override
	public void info(String msg, Object... args) {
        log.log(Level.INFO, MessageFormat.format(msg, args));
	}
	
    @Override
	public void info(String msg, Throwable ex, Object... args) {
        log.log(Level.INFO, MessageFormat.format(msg, args), ex);
    }

    
    @Override
	public boolean isWarnEnabled() {
        return log.isLoggable(Level.WARNING);
    }

    @Override
	public void warn(String msg, Throwable ex, Object... args) {
        log.log(Level.WARNING, MessageFormat.format(msg, args), ex);
    }
    
    @Override
	public void warn(String msg, Object... args) {
        log.log(Level.WARNING, MessageFormat.format(msg, args));
    }

    
    @Override
	public boolean isDebugEnabled() {
        return log.isLoggable(Level.FINE);
    }

    @Override
	public void debug(String msg, Object... args) {
        log.log(Level.FINE, MessageFormat.format(msg, args));
    }

    @Override
	public void debug(String msg, Throwable ex, Object... args) {
        log.log(Level.FINE,  MessageFormat.format(msg, args), ex);
    }
    
    @Override
	public boolean isErrorEnabled() {
        return log.isLoggable(Level.SEVERE);
    }
    @Override
	public void error(String msg, Throwable ex, Object... args) {
        log.log(Level.SEVERE, MessageFormat.format(msg, args), ex);
    }    
    
    @Override
	public void error(String msg, Object... args) {
        log.log(Level.SEVERE, MessageFormat.format(msg, args));
    }
    

    @Override
	public boolean isFatalEnabled() {
        return log.isLoggable(Level.SEVERE);
    }
    
    @Override
	public void fatal(String msg, Object... args) {
        log.log(Level.SEVERE, MessageFormat.format(msg, args));
    }

    @Override
	public void fatal(String msg, Throwable ex, Object... args) {
        log.log(Level.SEVERE, MessageFormat.format(msg, args), ex);
    }

}
