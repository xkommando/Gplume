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
package com.caibowen.gplume.web;

import com.caibowen.gplume.event.AppEvent;

/**
 * 
 * @author BowenCai
 *
 */
public class WebAppStartedEvent extends AppEvent {
	private static final long serialVersionUID = 1L;

	private long time;
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public WebAppStartedEvent(Object arg0) {
		super(arg0);
	}

}
