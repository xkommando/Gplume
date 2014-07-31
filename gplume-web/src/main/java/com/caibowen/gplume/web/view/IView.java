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
package com.caibowen.gplume.web.view;

import com.caibowen.gplume.web.RequestContext;


/**
 * 
 * @author BowenCai
 *
 */
public interface IView {
	
	
	void resolve(RequestContext context);
	
	static class get{
		
		public static TextView textView(String encoding, String type) {
			return new TextView(encoding, type);
		}
		public static TextView textView(String encoding, String type, String cnt) {
			return new TextView(encoding, type).setContent(cnt);
		}
		
		public static TextView textView(String cnt) {
			return new TextView().setContent(cnt);
		}
		
		public static IView jump(String cnt) {
			return new JumpView(cnt);
		}
		
	}
	
}
