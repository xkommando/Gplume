/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.event;

import java.util.EventListener;

/**
 * a listener will receive only events that it support
 * a lister class can implement one and one only AppListener in its type hierarchy
 * 
 * @author BowenCai
 *
 * @param <T> the event type that this listener supports
 */
public interface IAppListener<T extends AppEvent> extends EventListener {
	
	public void onEvent(T event);

}
