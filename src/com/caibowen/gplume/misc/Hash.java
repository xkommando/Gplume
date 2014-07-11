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
package com.caibowen.gplume.misc;


/**
 * 
 * @author BowenCai
 *
 */
public class Hash {

	
	public static int hash(Object...args) {
		int h = 1;
		for (int i = 0; i < args.length; i++) {
			Object object = args[i];
			h = 31 * h + (object == null ? 0 : object.hashCode());
		}
		return h;
	}
}
