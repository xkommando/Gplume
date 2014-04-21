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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.caibowen.gplume.core.SemaphoreKeeper;

/**
 * 
 * @author BowenCai
 *
 */
public class QoSHelper {

	public static int getPriority(HttpServletRequest request) {
		
		if (request.getUserPrincipal() != null) {
			return SemaphoreKeeper.HIGH;
			
		} else {
			HttpSession session = request.getSession(false);
			return (session != null && !session.isNew()) 
					? SemaphoreKeeper.MEDIAN 
						: SemaphoreKeeper.LOW;
		}
	}
	
}
