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
package com.caibowen.gplume.web.taglib;

import java.io.IOException;
import java.io.Writer;

import com.caibowen.gplume.core.i18n.NativePackage;
import com.caibowen.gplume.web.taglib.WriterTag;

/**
 * 
 * @author BowenCai
 *
 */
public class TagMessage extends WriterTag {
	
	String k;
	
	@Override
	public String write(Writer writer) throws IOException {
		NativePackage pkg = getNatives();
		if (k != null && pkg != null) {
			writer.write(pkg.getStr(k));
			return SUCCESS;
		} else {
			return "key or properties is NULL";
		}
	}

	public String getK() {
		return k;
	}

	public void setK(String k) {
		this.k = k;
	}

}
