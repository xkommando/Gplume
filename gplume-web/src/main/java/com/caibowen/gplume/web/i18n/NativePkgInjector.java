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
