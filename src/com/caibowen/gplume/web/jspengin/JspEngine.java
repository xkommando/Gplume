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
package com.caibowen.gplume.web.jspengin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import com.caibowen.gplume.web.AbstractControlCenter;


/**
 * compile jsp template and get out put string
 * @author BowenCai
 *
 */
public class JspEngine {

	private static final Logger LOG  = Logger.getLogger(JspEngine.class.getName());
	
	@Inject AbstractControlCenter controlCenter;
	
	public String compile(Input input,
							String jspFile, PrintWriter errWriter) {
		
		RequestDispatcher engine = getControlCenter().getServletContext().getRequestDispatcher(jspFile);
		
		Output output = new Output();
		
		try {
			engine.forward(input, output);
			return output.toString();
		} catch (ServletException | IOException e) {

			StringWriter writer = new StringWriter(512);
			errWriter = new PrintWriter(writer);
			e.printStackTrace(errWriter);
			String errMsg = 
					"Error Compiling JSP [" + jspFile
					+"]\r\n StackTrace:\r\n" + writer.toString();

			LOG.log(Level.SEVERE, errMsg, e);
			
			errWriter.print(errMsg);
			return errMsg;
		}
	}

	public AbstractControlCenter getControlCenter() {
		return controlCenter;
	}

	public void setControlCenter(AbstractControlCenter controlCenter) {
		this.controlCenter = controlCenter;
	}
}
