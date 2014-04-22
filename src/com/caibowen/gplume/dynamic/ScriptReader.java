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
package com.caibowen.gplume.dynamic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caibowen.gplume.common.Base64;


/**
 * 
 * @author BowenCai
 *
 */
public class ScriptReader implements IScriptReader {

	private static final Logger LOG = Logger.getLogger(ScriptReader.class.getName());

	IClassLoaderProvider loaderProvider;
	@Override
	public void setLoaderProvider(IClassLoaderProvider provider) {
		loaderProvider = provider;
	}
	
	@Override
	public String toStr(BinScript script) {
		
		if (script == null) {
			return null;
		}
		
		byte[] bits = null;
		ByteArrayOutputStream baos = null;
		ObjectOutputStream objOut = null;
		try {
			baos = new ByteArrayOutputStream();
			objOut = new ObjectOutputStream(baos);
			objOut.writeObject(script);
			objOut.flush();
			objOut.close();
			bits = baos.toByteArray();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Error serialize BinScript", e);
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (Exception e2) {}
			}
			if (objOut != null) {
				try {
					objOut.close();
				} catch (Exception e2) {}
			}
		}
		
		return (bits != null && bits.length > 0 )
				? Base64.getEncoder().encodeToString(bits)
				: null;
	}

	@Override
	public BinScript readStr(String literal) {
		
		byte[] bits = Base64.getDecoder().decode(literal);
		ByteArrayInputStream baos = null;
		Object response = null;
		try {
			baos = new ByteArrayInputStream(bits);

			ObjectInputStream objIn = new ObjectInputStream(baos) {
				@Override
				protected Class<?> resolveClass(ObjectStreamClass objectStreamClass)
						throws ClassNotFoundException, IOException {
					
					return ScriptReader.this.loaderProvider.geClassLoader()
							.loadClass(objectStreamClass.getName());
				}
			};

			response = objIn.readObject();
			objIn.close();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Error deserialized object", e);
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (Exception e2) {}
			}
		}
		
		if (response instanceof BinScript) {
			return (BinScript) response;
		} else {
			return null;
		}
	}

}
