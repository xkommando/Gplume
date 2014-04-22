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

import java.util.Set;

import javax.servlet.http.HttpSession;

import com.caibowen.gplume.core.i18n.Dialect;
import com.caibowen.gplume.core.i18n.I18nService;
import com.caibowen.gplume.core.i18n.NativePackage;
import com.caibowen.gplume.web.RequestContext;

/**
 * 
 * @author BowenCai
 *
 */
public class WebI18nService extends I18nService {

	private static final long serialVersionUID = 4782972973617957393L;

	public static final String ALTERNATIVE = "gplume_web_alternative_dialect";
	
	public void attachPkgTo(RequestContext context) {
		
		HttpSession session = context.request.getSession(true);
		if (session != null) {
			if (null == session.getAttribute(NativePackage.NAME)) {
				Dialect dialect = resolve(context.request.getHeader("accept-language"));
				if (dialect == Dialect.Unknown) {
					dialect = Dialect.SimplifiedChinese;
				}
				Set<Dialect> all = getAll();
				session.setAttribute(NativePackage.NAME, pkgTable.get(dialect));
				session.setAttribute(ALTERNATIVE, all);
			}
		} else {
			Dialect dialect = resolve(context.request.getHeader("accept-language"));
			if (dialect == Dialect.Unknown) {
				dialect = Dialect.SimplifiedChinese;
			}
			context.putAttr(NativePackage.NAME, pkgTable.get(dialect));
		}

	}
	
}
