/*
 * *****************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.web.i18n;

import com.caibowen.gplume.i18n.Dialect;
import com.caibowen.gplume.i18n.HotSwapI18nService;
import com.caibowen.gplume.i18n.NativePackage;
import com.caibowen.gplume.web.RequestContext;

import javax.servlet.http.HttpSession;
import java.util.Set;

/**
 * @author bowen.cbw
 * @since 9/3/2014.
 */
public class HotSwapWebI18n extends HotSwapI18nService implements IWebI18nService {

    private static final long serialVersionUID = 1549119471829125437L;

    protected NativePackage findPkg(RequestContext context) {
        Dialect dialect = resolve(context.request.getHeader("accept-language"));

        NativePackage pkg = getPkg(dialect);
        if (pkg == null) {
            pkg = getDefaultPkg();
        }
        return pkg;
    }

    public void attachPkgTo(RequestContext ctx) {
        HttpSession session = ctx.request.getSession(true);
        final NativePackage pkg = findPkg(ctx);
        if (session != null) {
            Set<Dialect> all = getSupportedDialects();
            session.setAttribute(NativePackage.NAME, pkg);
            session.setAttribute(ALTERNATIVES, all);
        }
        ctx.putAttr(ALTERNATIVES, getSupportedDialects());
        ctx.putAttr(NativePackage.NAME, findPkg(ctx));
    }

}
