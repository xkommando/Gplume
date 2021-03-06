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

package com.caibowen.gplume.web.views;

import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;

/**
 * @author bowen.cbw
 * @since 8/21/2014.
 */
public class JumpViewResolver implements IViewResolver {

    @Override
    public int fitness(Class klass) {
        return klass == JumpView.class ? 1 : -1;
    }

    @Override
    public void resolve(RequestContext ctx, Object view) throws Exception {
        JumpView jv = (JumpView)view;
        switch (jv.type) {
            case 1:
                ctx.passOff(jv.newURL);
                return;
            case 2:
                ctx.jumpTo(jv.newURL);
                return;
            default:
                throw new IllegalStateException();
        }
    }
}
