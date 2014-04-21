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

import javax.inject.Inject;

public class ScriptRunner {

	@Inject IScriptReader reader;
	public void setReader(IScriptReader reader) {
		this.reader = reader;
	}
	
	public void run(String literal) {
		BinScript script = reader.readStr(literal);
		if (script != null) {
			try {
//				script.init(null);
//				script.run();
			} catch (Throwable e) {
				
			}
		}
	}
}




