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
package com.caibowen.gplume.context;

import java.io.IOException;
import java.io.InputStream;


/**
 * 
 * @author BowenCai
 *
 */
public class InputStreamSupport {
	
	private InputStreamProvider streamProvider;
	
	public InputStreamSupport(){}
	
	public InputStreamSupport(InputStreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}

	public InputStreamProvider getStreamProvider() {
		return streamProvider;
	}
	
	public void setStreamProvider(InputStreamProvider streamProvider) {
		this.streamProvider = streamProvider;
	}


	public void withPath(String path, InputStreamCallback callback) {
        InputStream inputStream = null;
        try {
            inputStream = streamProvider.getStream(path);
            if (inputStream.available() < 1)
                throw new IllegalStateException("not avaliable");
        } catch (Exception e) {
            throw new IllegalArgumentException("resource unavailable[" + path
                    + "] with provider [" + streamProvider.getClass().getName()
                    + "]", e);
		}

        Exception ex = null;
		try {
			callback.doInStream(inputStream);
		} catch (Exception e) {
			ex = e;
		} finally {
			try {
                if (inputStream != null)
    				inputStream.close();
			} catch (IOException e) {
				ex = e;
			}
		}
		if (ex != null) {
			throw new RuntimeException(ex);
		}
	}
}
