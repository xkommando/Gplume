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

import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import com.caibowen.gplume.event.IAppListener;
import com.caibowen.gplume.i18n.*;
import com.caibowen.gplume.web.RequestContext;

/**
 * 
 * @author BowenCai
 *
 */
public class WebI18nService extends GenI18nService implements IWebI18nService {

	private static final long serialVersionUID = 4782972973617957393L;

    protected NativePackage findPkg(RequestContext context) {
        Dialect dialect = resolve(context.request.getHeader("accept-language"));

        NativePackage pkg = getPkg(dialect);
        if (pkg == null) {
            pkg = getDefaultPkg();
        }
        return pkg;
	}

    /**
     * find and attach pkg to session and request
     *
     * @param ctx
     */
	@Override
    public void attachPkgTo(RequestContext ctx) {
		HttpSession session = ctx.request.getSession(true);
		if (session != null) {
			if (null == session.getAttribute(NativePackage.NAME)) {
				Set<Dialect> all = getSupportedDialects();
				session.setAttribute(NativePackage.NAME, findPkg(ctx));
				session.setAttribute(ALTERNATIVES, all);
			}
        }
    }

}
