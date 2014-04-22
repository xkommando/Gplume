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
package com.caibowen.gplume.web.misc;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.core.context.AppContext;
import com.caibowen.gplume.event.IAppListener;
import com.caibowen.gplume.web.ControlCenter;
import com.caibowen.gplume.web.IErrorHandler;
import com.caibowen.gplume.web.IRequestProcessor;
import com.caibowen.gplume.web.SimpleControlCenter;
import com.caibowen.gplume.web.WebAppStartedEvent;
import com.caibowen.gplume.web.action.ActionFactory;


/*
	<bean id="controlCenter" class="com.caibowen.gplume.web.SimpleControlCenter">
		<property name="preProcessor" ref="headPrePrcessor"/>
        
		<property name="actionFactory">
		    <bean class="com.caibowen.gplume.web.action.ActionFactory" />
		</property>
		
		<property name="injector" ref="injector"/>
		
		<property name="errorHandler">
		    <bean class="com.caibowen.web.misc.ErrorHandler" />
		</property>
		    <list> 
		    	<bean class="com.caibowen.Test2" />
		    	<bean class="com.caibowen.Test3" />
		    </list>
		</property>
	</bean>
	
	NEW
	
	<bean class="com.caibowen.gplume.web.EasyConfig">
		<property name="preProcessor" ref="somePreprocessor"/>
		<property name="errorHandler" ref=""/>
		<property name="pkgs">
			<list>
				<value>package1<value>
				<value>package2<value>
			<list>
		<property/>
	</bean>
	
	
		<bean class="com.caibowen.gplume.web.EasyConfig">
		<property name="pkg" value="com.caibowen" />
		<property name="preProcessor">
			<bean class="com.caibowen.gplume.web.l10n.LocalePreProcessor">
				<property name="localeResolver">
					<bean class="com.caibowen.gplume.web.l10n.BilingualLocaleResolver" />
				</property>
			</bean>
		</property>
	</bean>
*/
/**
 * add EasyConfig in your .xml and get the gplume.web.controlCenter is ready to go!
 * 
 * 
 * 
 * @author BowenCai
 *
 */
public class EasyConfig implements IAppListener<WebAppStartedEvent>, Serializable {
	
	private static final long serialVersionUID = 657513014059796966L;

	Logger LOG = Logger.getLogger(EasyConfig.class.getName());
	
	public EasyConfig() {
		AppContext.broadcaster.register(this);
	}

	
	@Override
	public void onEvent(WebAppStartedEvent event) {
		ControlCenter center = build(pkgs);
		AppContext.beanAssembler.addBean("controlCenter", center);		
	}
	
	@Inject IRequestProcessor preProcessor;
	public void setPreProcessor(IRequestProcessor preProcessor) {
		this.preProcessor = preProcessor;
	}
	
	@Inject List<String>  pkgs;
	public void setPkgs(List<String> pkgs) {
		this.pkgs = pkgs;
	}

	@Inject IErrorHandler errorHandler;
	public void setErrorHandler(IErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}


	private ControlCenter build(List<String> pkgs) {
		try {
			SimpleControlCenter center = new SimpleControlCenter();
			
			Injector injector = new Injector();
			injector.setBeanAssembler(AppContext.beanAssembler);
			center.setInjector(injector);
			
			center.setActionFactory(new ActionFactory());
			center.setPreProcessor(this.preProcessor);
			center.setErrorHandler(this.errorHandler != null ? errorHandler
									: new DefaultErrorHandler());

			ControllerScanner scanner = new ControllerScanner();
			scanner.setPackages(this.pkgs);
			scanner.setControlCenterCallBack(center);
			
			return center;
			
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "cannot build controlCenter", e);
			e.printStackTrace();
			return null;
		}
	}
	


	
}
