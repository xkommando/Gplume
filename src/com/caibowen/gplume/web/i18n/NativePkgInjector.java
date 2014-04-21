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
package com.caibowen.gplume.web.i18n;

import javax.inject.Inject;

import com.caibowen.gplume.web.IRequestProcessor;
import com.caibowen.gplume.web.RequestContext;


/**
 * inject native pkg to session ot request if abscent
 * @author BowenCai
 *
 */
public class NativePkgInjector implements IRequestProcessor  {

	@Inject WebI18nService i18nService;
	
	@Override
	public void process(RequestContext context) {
		i18nService.attachPkgTo(context);
		if (next != null) {
			next.process(context);
		}
	}

	IRequestProcessor next;
	@Override
	public IRequestProcessor getNext() {
		return next;
	}

	@Override
	public void setNext(IRequestProcessor preProcessor) {
		this.next = preProcessor;
	}

	/**
	 * @return the i18nService
	 */
	public WebI18nService getI18nService() {
		return i18nService;
	}

	/**
	 * @param i18nService the i18nService to set
	 */
	public void setI18nService(WebI18nService i18nService) {
		this.i18nService = i18nService;
	}

}
