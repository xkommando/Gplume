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

import java.io.Serializable;
import java.util.Properties;

/**
 * 
 * compiled script
 * 
 * @author BowenCai
 *
 */
public interface BinScript extends Serializable, Runnable {
	
	public static final int RUNNABLE = 1;
	public static final int RUNNING = 2;
	public static final int FINISHED = 3;
	//....
	
	public void init(Properties config);
	@Override
	public void run();
	public Properties endState();
}
