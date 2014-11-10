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
package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.web.*;
import com.caibowen.gplume.web.actions.builder.BuilderAux;
import com.caibowen.gplume.web.actions.builder.BuilderProxy;
import com.caibowen.gplume.web.actions.builder.ViewMatcher;
import com.caibowen.gplume.web.annotation.Handle;
import com.caibowen.gplume.web.annotation.Intercept;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * container for action, interception
 * 
 * @author BowenCai
 *
 */
public class ActionFactory implements IActionFactory, Serializable {
	
	private static final long serialVersionUID = -4677797873703541912L;

	private static final Logger LOG = LoggerFactory.getLogger(ActionFactory.class);
	
	private ActionMapper<IAction>[] mappers;
    private ViewMatcher matcher;
	public ActionFactory() {
		
		final int enumCount = HttpMethod.class.getEnumConstants().length;

		// last one is for interception
		mappers = new ActionMapper[enumCount + 1];

		for (int i = 0; i < enumCount; i++) {
			mappers[i] = new ActionMapper<>();
		}
		mappers[enumCount] = new ActionMapper<>();

        matcher = new ViewMatcher();
	}


    public void setViewResolvers(List<IViewResolver> resolvers) {
        matcher.setResolvers(resolvers);
        BuilderAux.viewMatcher = this.matcher;
    }

	@Override
	public IAction findAction(HttpMethod httpmMthod, String uri) {
		return mappers[httpmMthod.ordinal()].getAction(uri);
	}
	
	@Override
	public Interception findInterception(String uri) {
		return (Interception) mappers[mappers.length - 1].getAction(uri);
	}
	
	@Override
	public void registerIntercept(@Nullable String prefix, 
									@Nonnull Object ctrl, 
									@Nonnull Method method) {

		String[] uris = method.getAnnotation(Intercept.class).value();
		if (uris != null && uris.length > 0) {
			for (String uri : uris) {
				if (Str.Utils.notBlank(prefix))
					uri = prefix + uri;
				
				checkURI(uri);
				Interception i = BuilderProxy
								.buildInterception(uri, ctrl, method);
				
				mappers[mappers.length - 1].add(i);
			}
		} else {
			throw new NullPointerException(
					"No URI specified for Intercept method[" + method.getName()
							+ "] in class [" + ctrl.getClass().getName()
							+ "]");
		}
	}

	@Override
	public void registerHandles(@Nullable String prefix, 
								@Nullable Object ctrl,
								@Nonnull Method method) {
		
		Handle info = method.getAnnotation(Handle.class);
		String[] uris = info.value();
		
		if (uris == null || uris.length == 0) {
			throw new NullPointerException("cannot found uri"
					+ " check object["
					+ ctrl.getClass().getName() + "]");
		}
		
		boolean addPrefix = Str.Utils.notBlank(prefix);
		for (String uri : uris) {
			if (addPrefix)
				uri = prefix + uri;
			
			IAction action = BuilderProxy
							.buildAction(uri, method, ctrl);
			
			checkURI(action.effectiveURI());
			HttpMethod[] methods = info.httpMethods();
//			System.out.print(uri);
			for (HttpMethod hm : methods) {
//				System.out.println(hm);
				if (LOG.isDebugEnabled())
					LOG.debug("adding action from method[{}]"
							+ " URI [{}] Method [{}]"
							, method.toString(), action.effectiveURI(), hm);
				
				mappers[hm.ordinal()].add(action);
			} // methods
		}
		
	}
	
	
	protected void checkURI(final String uri) {

//System.out.println("[" + uri +"]");
		if (!Str.Patterns.MAPPING_URI.matcher(uri).matches()) {
			
			throw new IllegalArgumentException("URI [" + uri
					+ "] must match \"" + Str.Patterns.MAPPING_URI.pattern()
					+ "\"");
		}
	}
	
	@Override
	public boolean removeHandle(String uri) {
		boolean weDidIt = false;
		for (ActionMapper<IAction> am : mappers) {
			weDidIt = weDidIt ||  am.remove(uri);
		}
		return weDidIt;
	}

	@Override
	public boolean removeInterception(final String uri) {
		return mappers[mappers.length - 1].remove(uri);
	}

	@Override
	public void destroy() {
		mappers = null;
	}

}
