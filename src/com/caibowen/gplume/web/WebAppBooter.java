package com.caibowen.gplume.web;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.caibowen.gplume.core.AppContext;


/**
 * 
 * @author BowenCai
 *
 */
public class WebAppBooter implements ServletContextListener{
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		
		ServletContext servletContext = event.getServletContext();

		String manifest = servletContext.getInitParameter(AppContext.MANIFEST);

		InputStream inStream = servletContext.getResourceAsStream(manifest);
		

		try {
			AppContext.init(inStream);
		} finally {

			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				
					throw new RuntimeException(
						"cannot close manifest file"
								+ "\n" + e.getMessage()
								+ "\n" + e.getCause(), e);
			}
		}
		
	}
	
	
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {


	}


}
