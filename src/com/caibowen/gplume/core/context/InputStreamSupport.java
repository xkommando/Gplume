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


/**
 * 
 * @author BowenCai
 *
 */
public class InputStreamSupport {
	
	private InputStreamProvider streamProvider;

	public InputStreamSupport() {}
	
	public InputStreamSupport(InputStreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}

	public InputStreamProvider getStreamProvider() {
		return streamProvider;
	}
	
	public void setStreamProvider(InputStreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}


	public void doInStream(String path, InputStreamCallback callback) {
		Exception ex = null;
		InputStream inputStream = streamProvider.getStream(path);
		if (inputStream == null) {
			throw new IllegalArgumentException("resource unavailable[" + path
					+ "] with provider [" + streamProvider.getClass().getName()
					+ "]");
		}
		try {
			callback.doWithStream(inputStream);
		} catch (Exception e) {
			ex = e;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				ex = e;
			}
		}
		if (ex != null) {
			throw new RuntimeException("Exception In I/O Operation\r\n", ex);
		}
	}
}
