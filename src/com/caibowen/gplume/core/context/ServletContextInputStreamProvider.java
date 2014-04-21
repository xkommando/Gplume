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
package com.caibowen.gplume.core.context;

import java.io.InputStream;
import java.io.Serializable;

import javax.servlet.ServletContext;



/**
 * 
 * @author BowenCai
 *
 */
public class ServletContextInputStreamProvider implements InputStreamProvider, Serializable {

	private static final long serialVersionUID = -3973425249230485036L;
	ServletContext context;

	public ServletContextInputStreamProvider(ServletContext context) {
		this.context = context;
	}

	@Override
	public InputStream getStream(String path) {
		return context.getResourceAsStream(path);
	}

	@Override
	public String getContextPath() {
		throw new UnsupportedOperationException();
	}

}
