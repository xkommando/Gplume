package com.caibowen.gplume.orm.hibernate;

import java.lang.reflect.Array;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import com.caibowen.gplume.core.bean.IBeanAssembler;
import com.caibowen.gplume.core.bean.IBeanAssemblerAware;
import com.caibowen.gplume.core.bean.InitializingBean;

/**
 * wrapper for org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean
 * usage:
 * bean of name sessionFactory will be added to beanAssembler automatically.
 * 	
 * 
	<bean id="sessionFactoryBuilder"
		class="com.caibowen.gplume.orm.hibernate.sessionFactoryBuilder">
		<property name="dataSource" ref="dataSource"/> 
		<property name="hibernateProperties">
			<props>
				<prop key="hibernate.dialect">org.hibernate.dialect.MySQL5Dialect</prop>
				<prop key="hibernate.hbm2ddl.auto">update</prop>
				<prop key="hibernate.show_sql">true</prop>
			</props>
		</property>
		
		<property name="annotatedClasses">
            <list>
                <value>com.caibowen.gplume.sample.model.Chapter</value>
            </list>
        </property>
	</bean>
 * 
 *@author BowenCai
 *@since 9:16:10 PM
 */
public class SessionFactoryBuilder extends AnnotationSessionFactoryBean 
										implements IBeanAssemblerAware, InitializingBean{
	
	public void setAnnotatedPackages(List<String> pkgs) {
		String[] arr = (String[])Array.newInstance(String.class, pkgs.size());
		for (int i = 0; i < arr.length; i++) {
			arr[i] = pkgs.get(i);
		}
		super.setAnnotatedPackages(arr);
	}
	
	public void setAnnotatedClasses(List<String> modelNames) throws Exception {
		Class<?>[] klasses = new Class[modelNames.size()];
		for (int i = 0; i < klasses.length; i++) {
			klasses[i] = Class.forName(modelNames.get(i));
		}
		super.setAnnotatedClasses(klasses);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		beanAssembler.removeBean("sessionFactory");
		SessionFactory sessionFactory = super.getSessionFactory();
		beanAssembler.addBean("sessionFactory", sessionFactory);
	}

	IBeanAssembler beanAssembler;
	@Override
	public void setBeanAssembler(IBeanAssembler factory) {
		beanAssembler = factory;
	}
}