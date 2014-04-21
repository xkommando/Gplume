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
