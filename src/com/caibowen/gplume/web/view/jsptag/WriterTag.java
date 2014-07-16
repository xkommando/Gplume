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
package com.caibowen.gplume.web.view.jsptag;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.i18n.NativePackage;
import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;
import com.caibowen.gplume.web.i18n.WebI18nService;

/**
 * 
 * @author BowenCai
 *
 */
public abstract class WriterTag extends SimpleTagSupport {

	/**
	 * compiler flag
	 */
	protected static final boolean TEST_MODE = false;
	
	protected Logger LOG = LoggerFactory.getLogger(WriterTag.class.getName());

	public static final String SUCCESS = "SUCCESS;"
			+ "Do not try to return your own 'SUCCESS', it will fail 'str == str' and be treated as error"; // success return
	
	protected NativePackage getNatives() {
		NativePackage pkg = (NativePackage) 
					getJspContext().getAttribute(NativePackage.NAME, 
							PageContext.SESSION_SCOPE);
		if (pkg == null) {// unlikely
			pkg = (NativePackage) getJspContext().getAttribute(NativePackage.NAME, 
					PageContext.REQUEST_SCOPE);
			LOG.warn("cannot get localProperties from session, try get from request");
		}
		if (pkg == null) {//unlikely
			WebI18nService service = AppContext.beanAssembler.getBean("i18nService");
			if (service != null) {
				pkg = service.getDefaultPkg();
				LOG.error("cannot get localProperties from session or request, using default");
			} else {
				LOG.error("cannot get localProperties from session or request, null i18nService");
			}
		}
		return pkg;
	}
	
	
	abstract public String write(JspWriter writer) throws IOException;
	
	@Override
	public void doTag() {
		
		if (!TEST_MODE) {
			try {
				JspWriter writer = getJspContext().getOut();
				String ret = write(writer);
				if (SUCCESS != ret) {
					writer.write("<h1 align=\"center\"><font color=\"red\"> ERROR in tag["
							+ getClass().getSimpleName()
							+ "]  </font> </h1>"
							+ "<br/>\r\nMessage:<pre>\r\n" + ret + "\r\n</pre>");
				}
				
			} catch (Exception e) {
				LOG.error("writing jsp", e);
			}
			
		} else {
			try {
				@SuppressWarnings("resource")
				String ret = write(new JspWriterMock());
				if (SUCCESS != ret) {
					PrintWriter errWriter = new PrintWriter(System.err);
					errWriter.write("\nError in tag[" + getClass().getSimpleName() + "]\n"
							+"msg[" + ret +"]\n");
					errWriter.flush();
				}
			} catch (Exception e) {
				PrintWriter errWriter = new PrintWriter(System.err);
				errWriter.write("Exception\r\n");
				LOG.error("", e);
				errWriter.write("\r\n");
				errWriter.flush();
			}
		}
	}
}



