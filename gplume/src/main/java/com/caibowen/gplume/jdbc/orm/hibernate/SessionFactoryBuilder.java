package com.caibowen.gplume.jdbc.orm.hibernate;
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
///*******************************************************************************
// * Copyright 2014 Bowen Cai
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *   http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// ******************************************************************************/
//package com.caibowen.gplume.orm.hibernate;
//
//import org.hibernate.SessionFactory;
//
//import com.caibowen.gplume.context.bean.BeanIDAware;
//import com.caibowen.gplume.context.IBeanAssembler;
//import com.caibowen.gplume.context.bean.IBeanAssemblerAware;
//import com.caibowen.gplume.context.bean.InitializingBean;
//
///**
// * 
// *@author BowenCai
// *@since 9:16:10 PM
// */
//public class SessionFactoryBuilder 
//	extends org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBeanAnnotationSessionFactoryBean 
//	implements com.caibowen.gplume.context.bean.IBeanAssemblerAware, 
//			,com.caibowen.gplume.context.bean.InitializingBean,
//			,com.caibowen.gplume.context.bean.BeanIDAware{
//	
//	private IBeanAssembler beanAssembler;
//	@Override
//	public void setBeanAssembler(IBeanAssembler factory) {
//		beanAssembler = factory;
//	}
//	private String sessionFactoryID;
//	public void setSessionFactoryID(String sessionFactoryID) {
//		this.sessionFactoryID = sessionFactoryID;
//	}
//	
//	private String thisID;
//	@Override
//	public void setBeanID(String id) {
//		thisID = id;
//	}
//	
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		super.afterPropertiesSet();
//		beanAssembler.removeBean(thisID);
//		SessionFactory sessionFactory = super.getSessionFactory();
//		beanAssembler.addBean(sessionFactoryID, sessionFactory);
//	}
//
//
//
//
//}
