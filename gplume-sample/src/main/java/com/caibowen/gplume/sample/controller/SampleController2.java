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
//package com.caibowen.gplume.sample.controller;
//
//import java.security.PublicKey;
//import java.util.Date;
//
//import com.caibowen.gplume.misc.Str;
//import com.caibowen.gplume.sample.feature.PublicKeyService;
//import com.caibowen.gplume.sample.feature.Validator;
//import com.caibowen.gplume.web.HttpMethod;
//import com.caibowen.gplume.web.RequestContext;
//import com.caibowen.gplume.web.annotation.Controller;
//import com.caibowen.gplume.web.annotation.CookieVal;
//import com.caibowen.gplume.web.annotation.Handle;
//import com.caibowen.gplume.web.annotation.ReqAttr;
//import com.caibowen.gplume.web.annotation.ReqParam;
//import com.caibowen.gplume.web.annotation.SessionAttr;
//import com.caibowen.gplume.web.IView;
//
///**
// * 
// * @author BowenCai
// *
// */
////this sample login function is for demo only and is insecure
//@Controller("/async/")
//public class SampleController2 {
//	
////	@Inject Validator validator;
////	@Inject PublicKeyService keyService;
////	@Inject UserService userService;
//	Validator validator;
//	PublicKeyService keyService;
//	UserService userService;
//	class MyState {
//		@ReqAttr(value="alias", defaultVal="1992-6-14")
//		Date birthday;
//
//		@CookieVal
//		Double testData;
//		
//		@ReqParam("psw_cipher")
//		String passwordCipher;
//		
//		@ReqParam(value="email_address", required = false)
//		String email;
//		
//		@SessionAttr(value="this_pubkey",required = false)
//		PublicKey key;
//		
//		User user;
//		boolean ok() {
//			String psw = keyService.decrypt(key, passwordCipher);
//			if (!Str.Utils.notBlank(psw)
//					||!validator.matchEmail(email, psw))
//				return false;
//			else {
//				user = userService.getUser(email);
//				return true;
//			}
//		}
//	}
//	
//	@Handle(value={"login"}, httpMethods={HttpMethod.POST})
//	public IView login(MyState reqScope, RequestContext req) {
//		if (reqScope == null) //non-null requirements are not met.
//			return IView.get.textView("no public key in session");
//		else if (!reqScope.ok())
//			return IView.get.textView("password and email mismatch");
//		else {
//			req.session(true).setAttribute("this-user", reqScope.user);
//			return IView.get.jump("/user/" + reqScope.user.getNameURL());
//		}
//	}
//}
