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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * 
 * @author BowenCai
 *
 */
public class URLInputStreamProvider implements InputStreamProvider{

	@Override
	public InputStream getStream(String path) {
		try {
			return new URI(path).toURL().openStream();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("", e);
		}
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
