package com.caibowen.gplume.sample.controller;

import java.security.PublicKey;

import javax.inject.Inject;

import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.sample.feature.PublicKeyService;
import com.caibowen.gplume.sample.feature.Validator;
import com.caibowen.gplume.web.HttpMethod;
import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.meta.Controller;
import com.caibowen.gplume.web.meta.Handle;
import com.caibowen.gplume.web.meta.ReqParam;
import com.caibowen.gplume.web.meta.SessionAttr;
import com.caibowen.gplume.web.view.IView;
import com.caibowen.gplume.web.view.TextView;


@Controller("/async/")
public class ObjController {
	@Inject Validator validator;
	@Inject PublicKeyService keyService;
	@Inject UserService userService;
	
	class MyState {
		@ReqParam("psw_cipher")
		String passwordCipher;
		@ReqParam(value="email_address", nullable = false)
		String email;
		@SessionAttr(nullable = false)
		String publikKeyId;
		
		User user;
		boolean ok() {
			PublicKey pk = keyService.getPublicKey(publikKeyId);
			if (pk == null)
				return false;			
			String psw = keyService.decrypt(pk, passwordCipher);
			if (!Str.Utils.notBlank(psw))
				return false;
			if (!validator.matchEmail(email, psw))
				return false;
			else {
				user = userService.getUser(email);
				return true;
			}
		}
	}
// sample login function, insecure in real application
	@Handle(value={"login"}, httpMethods={HttpMethod.POST})
	public IView login(MyState requestScop, RequestContext req) {
		if (requestScop == null)
			return new TextView()
					.setContent("no public key in session");
		else if (!requestScop.ok())
			return new TextView()
					.setContent("password email mismatch");
		req.getSession(true).setAttribute("this-user", requestScop.user);
		req.jumpTo("/user/" + requestScop.user.getNameURL());
		return null;
	}
}
