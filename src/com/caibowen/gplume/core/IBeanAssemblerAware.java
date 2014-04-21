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
package com.caibowen.gplume.core;


/**
 * BeanAssemblerAware objects will retain a reference to the BeanFactory that created it
 * 
 * @author BowenCai
 *
 * @see Injector
 */
public interface IBeanAssemblerAware {
	
	public void setBeanAssembler(IBeanAssembler factory);
}
