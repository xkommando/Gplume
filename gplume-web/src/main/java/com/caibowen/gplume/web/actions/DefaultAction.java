package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.web.IAction;
import com.caibowen.gplume.web.RequestContext;

import javax.servlet.RequestDispatcher;
import java.lang.reflect.Method;

/**
 * Created by Bowen Cai on 12/31/2014.
 */
public class DefaultAction implements IAction {

    private static final long serialVersionUID = -3100604287249212948L;

    public final RequestDispatcher defaultDispatcher;

    public DefaultAction(RequestDispatcher defaultDispatcher) {
        this.defaultDispatcher = defaultDispatcher;
    }

    @Override
    public void perform(RequestContext requestContext) throws Throwable {
        defaultDispatcher.forward(requestContext.request, requestContext.response);
    }

    @Override
    public Method method() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String effectiveURI() {
        throw new UnsupportedOperationException();
    }
}
