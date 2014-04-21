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

public class RestQoSAction extends QoSAction {

	private static final long serialVersionUID = -3117262195337455584L;

	public RestQoSAction(String u, MethodHandle handle, int limit) {
		super(u, handle, limit);
	}

}
