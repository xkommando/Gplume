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
package com.caibowen.gplume.task;


/**
 * 
 * @author BowenCai
 *
 */
public abstract class OneTimeTask implements Runnable{

    final Object lock = new Object();

    int state = VIRGIN;

    static final int VIRGIN = 0;

    static final int SCHEDULED   = 1;

    static final int EXECUTED    = 2;

    static final int CANCELLED   = 3;

    long executionTime;

    public boolean cancel() {
        synchronized(lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    public long scheduledExecutionTime() {
    	return executionTime;
    }

}
