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

import java.lang.reflect.Method;

import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.Interception;

/**
 * manage all actions
 * @author BowenCai
 *
 */
public interface IActionFactory {

	/**
	 * @param controller
	 * @param method
	 */
	public void 			registerHandle(Object controller, Method method);
	public void 			registerIntercept(Object controller, Method method);
	
	public Action 			findAction(HttpMethod httpmMthod, String uri);

	public Interception 	findInterception(String uri);

	public boolean 			removeHandle(String uri);

	public boolean 			removeInterception(final String uri);

	public void 			destroy();
}
