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
package com.caibowen.gplume.misc.test.jsp.tag;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import com.caibowen.gplume.i18n.NativePackage;

/**
 * i18n message writer
 * 
 * @author BowenCai
 *
 */
public class TagMessage extends WriterTag {
	
	String k;
	
	@Override
	public String write(JspWriter writer) throws IOException {
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
