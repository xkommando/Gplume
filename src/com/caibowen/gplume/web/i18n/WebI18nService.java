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
