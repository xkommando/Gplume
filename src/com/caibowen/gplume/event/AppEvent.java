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

	public AppEvent(Object arg0) {
		super(arg0);
	}

}
