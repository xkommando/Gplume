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
