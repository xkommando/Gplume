/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.web.taglib;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.caibowen.gplume.core.context.AppContext;
import com.caibowen.gplume.core.i18n.NativePackage;
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
	
	protected Logger LOG = Logger.getLogger(WriterTag.class.getName());

	public static final String SUCCESS = "SUCCESS"; // success return
	
	protected NativePackage getNatives() {
		NativePackage pkg = (NativePackage) 
					getJspContext().getAttribute(NativePackage.NAME, 
							PageContext.SESSION_SCOPE);
		if (pkg == null) {// unlikely
			pkg = (NativePackage) getJspContext().getAttribute(NativePackage.NAME, 
					PageContext.REQUEST_SCOPE);
			LOG.log(Level.WARNING, "cannot get localProperties from session, try get from request");
		}
		if (pkg == null) {//unlikely
			WebI18nService service = AppContext.beanAssembler.getBean("i18nService");
			pkg = service.getDefaultPkg();
			LOG.log(Level.SEVERE, "cannot get localProperties from session or request, using default");
		}
		return pkg;
	}
	
	
	abstract public String write(Writer writer) throws IOException;
	
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
				writer.flush();
				
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "error writing jsp", e);
			}
			
		} else {
			PrintWriter writer = new PrintWriter(System.out);
			try {
				String ret = write(writer);
				if (SUCCESS != ret) {
					PrintWriter errWriter = new PrintWriter(System.err);
					errWriter.write("\nError in tag[" + getClass().getSimpleName() + "]\n"
							+"msg[" + ret +"]\n");
					errWriter.flush();
				}
			} catch (Exception e) {
				PrintWriter errWriter = new PrintWriter(System.err);
				errWriter.write("Exception\r\n");
				e.printStackTrace(errWriter);
				errWriter.write("\r\n");
				errWriter.flush();
			}
			writer.flush();
		}
	}
}



