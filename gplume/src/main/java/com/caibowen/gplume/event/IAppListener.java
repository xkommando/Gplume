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

import com.caibowen.gplume.annotation.Functional;

import java.util.EventListener;

/**
 * a listener will receive only events that it support
 * a lister class can implement one and one only AppListener in its type hierarchy
 * 
 * @author BowenCai
 *
 * @param <T> the event type that this listener supports
 */
@Functional
public interface IAppListener<T extends AppEvent> extends EventListener {
	
	public void onEvent(T event);

}
