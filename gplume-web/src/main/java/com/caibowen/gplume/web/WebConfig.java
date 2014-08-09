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

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.context.bean.InitializingBean;
import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import com.caibowen.gplume.web.builder.ActionFactory;
import com.caibowen.gplume.web.builder.IActionFactory;
import com.caibowen.gplume.web.misc.ControllerScanner;
import com.caibowen.gplume.web.misc.DefaultErrorHandler;
import com.caibowen.gplume.web.views.*;


/**
 * 
 * using WebConfig to reduce xml configuration
 * 
 * @author BowenCai
 *
 */
public class WebConfig implements InitializingBean, Serializable {

    // optional
	@Inject IRequestProcessor preProcessor;
	public void setPreProcessor(IRequestProcessor preProcessor) {
		this.preProcessor = preProcessor;
	}
	
	@Inject List<String>  pkgs;
	public void setPkgs(List<String> pkgs) {
		this.pkgs = pkgs;
	}

    // optional
	@Inject IErrorHandler errorHandler;
	public void setErrorHandler(IErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}


    // optional
    // by default it is JSP views resolver
    @Inject IStrViewResolver defaultViewResolver;
    public void setDefaultViewResolver(IStrViewResolver defaultViewResolver) {
        this.defaultViewResolver = defaultViewResolver;
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
            LOG.info("setting injector {0}", injector.getClass().getName());

            // 2. preprocessor chain
            center.setPreProcessor(this.preProcessor);

            // 3. error handler
            IErrorHandler _h = this.errorHandler != null ? errorHandler
                    : new DefaultErrorHandler();
            center.setErrorHandler(_h);

            LOG.info("setting default error handler {0}", _h.getClass().getName());

            // 4. action factory
			IActionFactory factory = new ActionFactory();
            IStrViewResolver _r = getDefaultViewResolver();
            factory.setDefaultViewResolver(_r);
            LOG.info("setting default view resolver {0}", _r.getClass().getName());

            center.setActionFactory(factory);
            LOG.info("setting action factory {0}", factory.getClass().getName());

            // add controllers
            ControllerScanner scanner = new ControllerScanner();
            scanner.setPackages(this.pkgs);
            scanner.setControlCenterCallBack(center);
            scanner.afterPropertiesSet();

			boolean boo = AppContext.beanAssembler.addBean("controlCenter", center);

            if (boo) LOG.info("ControlCenter set up");
            else LOG.info("cannot add [controlCenter] to beanAssembler");


		} catch (Exception e) {
			throw new RuntimeException("cannot build controlCenter", e);
		}
	}

    /**
     * case 1 : viewPrefix=""   -> PrefixResolver
     * case 2 : viewSuffix=""   -> SuffixResolver
     * case 3 : prefix="" viewSuffix="" -> PrefixSuffixResolver
     * otherwise : CompletePathViewResolver
     *
     * @return
     */
    @Nonnull
    private IStrViewResolver getDefaultViewResolver() {

        int resolverType = 0;
        if (Str.Utils.notBlank(viewPrefix)) {
            resolverType++;
            LOG.debug("views prefix {0}", viewPrefix);
        }
        if (Str.Utils.notBlank(viewSuffix)) {
            resolverType += 3;
            LOG.debug("views suffix {0}", viewSuffix);
        }
        if (defaultViewResolver != null) {
            return defaultViewResolver;

        } else {

            if (resolverType == 1)
                return new JspPrefixResolver(viewPrefix);
            else if (resolverType == 3)
                return new JspSuffixResolver(viewSuffix);
            else if (resolverType == 4)
                return new JspPrefixSuffixResolver(viewPrefix, viewSuffix);
            else
                return new JspCompletePathViewResolver();
        }
    }

    private static final long serialVersionUID = 657513014059796966L;

    private static final Logger LOG = LoggerFactory.getLogger(WebConfig.class.getName());

}
