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

/**
 * 
 * @author BowenCai
 *
 */
public class AppStartedEvent extends AppEvent {

	private static final long serialVersionUID = 6239892164123250715L;
	
	private long time;

	public AppStartedEvent(Object arg0) {
		super(arg0);
	}
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
