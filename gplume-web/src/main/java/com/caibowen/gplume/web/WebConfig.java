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
package com.caibowen.gplume.web;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.context.bean.InitializingBean;
import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.misc.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caibowen.gplume.web.actions.ActionFactory;
import com.caibowen.gplume.web.misc.ControllerScanner;
import com.caibowen.gplume.web.misc.DefaultErrorHandler;
import com.caibowen.gplume.web.views.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * 
 * using WebConfig to reduce xml configuration
 * 
 * @author BowenCai
 *
 */
public class WebConfig implements InitializingBean, Serializable {

    @Inject List<String>  pkgs;
    public void setPkgs(List<String> pkgs) {
        this.pkgs = pkgs;
    }


    // optional
	@Inject IRequestProcessor preProcessor;
	public void setPreProcessor(IRequestProcessor preProcessor) {
		this.preProcessor = preProcessor;
	}

    // optional
	@Inject IErrorHandler errorHandler;
	public void setErrorHandler(IErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

    // optional
    // by default it is JSP views resolver as the String resolver
    @Inject List<IViewResolver> viewResolvers;
    public void setViewResolvers(List<IViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    // optional
    @Inject String viewPrefix;
    public void setViewPrefix(String viewPrefix) {
        this.viewPrefix = viewPrefix;
    }

    // optional
    @Inject String viewSuffix;
    public void setViewSuffix(String viewSuffix) {
        this.viewSuffix = viewSuffix;
    }


    @Override
	public void afterPropertiesSet() throws Exception {
		try {
            SimpleControlCenter center = new SimpleControlCenter();

            // 1. injector
            Injector injector = AppContext.beanAssembler.getBean("injector");
            if (injector == null) {
                injector = new Injector();
                AppContext.beanAssembler.addBean("injector", injector);
            }
            center.setInjector(injector);
            LOG.debug("injector {} setted", injector.getClass().getName());

            IRequestProcessor iter = this.preProcessor;
            while (iter != null) {
                injector.inject(iter);
                iter = iter.getNext();
            }
            // 2. preprocessor chain
            center.setPreProcessor(this.preProcessor);

            // 3. error handler
            IErrorHandler _h = this.errorHandler != null ? errorHandler
                    : new DefaultErrorHandler();
            center.setErrorHandler(_h);

            LOG.debug("setting default error handler {}", _h.getClass().getName());

            // 4. action factory
            IActionFactory factory = new ActionFactory();
            factory.setViewResolvers(getViewResolvers());

            center.setActionFactory(factory);
            LOG.debug("setting action factory {}", factory.getClass().getName());

            // add controllers
            ControllerScanner scanner = new ControllerScanner();
            scanner.setPackages(this.pkgs);
            scanner.setControlCenterCallBack(center);
            scanner.afterPropertiesSet();

			boolean boo = AppContext.beanAssembler.addBean("controlCenter", center);

            if (boo) LOG.debug("ControlCenter set up");
            else LOG.debug("cannot add [controlCenter] to beanAssembler");

		} catch (Exception e) {
			throw new RuntimeException("cannot build controlCenter", e);
		}
	}

    public List<IViewResolver> getViewResolvers() {
        IViewResolver _rStr = JspViewResolvers.get(viewPrefix, viewSuffix);
        LOG.debug("setting default string view resolver {}", _rStr.getClass().getName());

        HashSet<IViewResolver> _s;
        if (viewResolvers != null && !viewResolvers.isEmpty())
            _s = new HashSet<>(viewResolvers);
        else
            _s = new HashSet<>();

        /**
         * default resolvers
         */
        _s.add(_rStr);
        _s.add(new TextViewResolver());
        _s.add(new JumpViewResolver());

        return new ArrayList<>(_s);
    }



    private static final long serialVersionUID = 657513014059796966L;

    private static final Logger LOG = LoggerFactory.getLogger(WebConfig.class.getName());

}
