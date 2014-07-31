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
package com.caibowen.gplume.misc.logging;

import javax.annotation.Nullable;

/**
 * 
 * @author BowenCai
 *
 */
public interface Logger {
	
	String name();
	
    void trace(String msg, @Nullable Object... args);

    void trace(String msg, Throwable ex, @Nullable Object... args);

    boolean isTraceEnabled();

    boolean isDebugEnabled();
    
    void debug(String msg, @Nullable Object... args);

    void debug(String msg, Throwable ex, @Nullable Object... args);

    boolean isInfoEnabled();
    
    void info(String msg, @Nullable Object... args);

    void info(String msg, Throwable ex, @Nullable Object... args);

    boolean isWarnEnabled();

    void warn(String msg, @Nullable Object... args);

    void warn(String msg, Throwable ex, @Nullable Object... args);


    boolean isErrorEnabled();
    
    void error(String msg, @Nullable Object... args);

    void error(String msg, Throwable ex, @Nullable Object... args);

    boolean isFatalEnabled();
    
    void fatal(String msg, @Nullable Object... args);

    void fatal(String msg, Throwable ex, @Nullable Object... args);

}
