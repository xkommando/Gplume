package com.caibowen.gplume.sample.test;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import com.caibowen.gplume.core.bean.BeanIDAware;
import com.caibowen.gplume.core.bean.IBeanAssembler;
import com.caibowen.gplume.core.bean.IBeanAssemblerAware;
import com.caibowen.gplume.core.bean.InitializingBean;

/**
 * 
 *@author BowenCai
 *@since 9:16:10 PM
 */
public class SessionFactoryBuilder extends AnnotationSessionFactoryBean 
										implements IBeanAssemblerAware, 
										InitializingBean,
										BeanIDAware{
	
	private IBeanAssembler beanAssembler;
	@Override
	public void setBeanAssembler(IBeanAssembler factory) {
		beanAssembler = factory;
	}
	private String sessionFactoryID;
	public void setSessionFactoryID(String sessionFactoryID) {
		this.sessionFactoryID = sessionFactoryID;
	}
	
	private String thisID;
	@Override
	public void setBeanID(String id) {
		thisID = id;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		beanAssembler.removeBean(thisID);
		SessionFactory sessionFactory = super.getSessionFactory();
		beanAssembler.addBean(sessionFactoryID, sessionFactory);
	}




}