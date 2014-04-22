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
