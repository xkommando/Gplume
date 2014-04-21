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

/**
 * 
 * @author BowenCai
 *
 */
public interface IScriptReader {
	
	public void setLoaderProvider(IClassLoaderProvider provider);
	/**
	 * 
	 * @param script
	 * @return null if failed
	 */
	public String toStr(BinScript script);
	
	
	/**
	 * 
	 * @param literal
	 * @return null if failed
	 */
	public BinScript readStr(String literal);

}
