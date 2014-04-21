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


/**
 * 
 * @author BowenCai
 *
 */
public class ClassLoaderInputStreamProvider implements InputStreamProvider{
	
	ClassLoader classLoader;
	
	public ClassLoaderInputStreamProvider(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public InputStream getStream(String path) {
		return classLoader.getResourceAsStream(path);
	}

	@Override
	public String getContextPath() {
//		classLoader.
		return null;
	}

}
