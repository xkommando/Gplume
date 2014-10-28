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
package com.caibowen.gplume.event;

import javax.annotation.Nonnull;
import java.util.EventObject;

/**
 * In applications, you must use specific event class,
 *  distinguished event classes makes it possible to separate 
 * listeners to different slot for the specific event.
 * 
 * Thereby, this class is made abstract.
 *  in this way, client won't be able to use java.util.EventObject 
 * which is unclear and not specified. 
 * 
 * 
 * @author BowenCai
 * @since 2013-12-29
 */
@SuppressWarnings("serial")
public abstract class AppEvent extends EventObject {

	public AppEvent(@Nonnull Object arg0) {
		super(arg0);
	}

}
