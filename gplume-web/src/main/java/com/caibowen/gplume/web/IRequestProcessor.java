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
package com.caibowen.gplume.web;

/**
 * 
 * Request processor
 * Can be used to build process chain.
 * Keep in mind that each processor is just one element of the processing chaning,
 * so remember to pass the RequestContext to the next one once your process has finished
 * 
 * @author BowenCai
 *
 */
public interface IRequestProcessor {
	
//	default public void goOn(RequestContext context) {
//		if (getNext() != null) {
//			getNext().process(context);
//		}
//	}
	
	public void process(RequestContext context);
	/**
	 * chain of preprocessors
	 * @return
	 */
	public IRequestProcessor getNext();
	public void setNext(IRequestProcessor preProcessor);
}
