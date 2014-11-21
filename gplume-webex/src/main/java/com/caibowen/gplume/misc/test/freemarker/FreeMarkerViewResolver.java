package com.caibowen.gplume.misc.test.freemarker;

import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;

/**
 * Created by BowenCai on 8/25/2014.
 */
public class FreeMarkerViewResolver implements IViewResolver {

    @Override
    public int fitness(Class val) {
        return val == String.class ? 1 : -1;
    }

    @Override
    public void resolve(RequestContext ctx, Object view) throws Exception {

    }
}
