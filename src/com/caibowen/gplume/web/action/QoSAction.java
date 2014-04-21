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
package com.caibowen.gplume.web.action;

import java.lang.invoke.MethodHandle;

import com.caibowen.gplume.web.RequestContext;


/**
 * 
 * @author BowenCai
 *
 */
public class QoSAction extends Action{

	private static final long serialVersionUID = 542208480144382166L;

	final int limit;
	
	public QoSAction(String u, 
						MethodHandle handle,
						int limit) {
		
		super(u, handle);
		this.limit = limit;
	}
	
	
	@Override
	public void perform(RequestContext context) {
		
	}
	

}
