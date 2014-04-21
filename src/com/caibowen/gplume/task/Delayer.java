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

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;



/**
 * 
 * 
 * Delayer is used to sync small amount of async operations that usually comes concurrently
 * 
 * 							| scheduled task start in this interval |
 * time: ---------------------------------------------------------------
 * 		 ^					^										^
 * 		 |					|										|		
 * 		time committed	  start TimeLine 						end TimeLine
 * 
 * @author BowenCai
 *
 */
public class Delayer {
	

	@Inject 
	private int startTimeLine = 500;
	public int getStartTimeLine() {return startTimeLine;}
	public void setStartTimeLine(int startTimeLine) { this.startTimeLine = startTimeLine;}

	/**
	 * endTimeLine is 8 sec by default.
	 * 
	 *  yes, you must clean all the mess in 10 - 8 = 2 sec
	 */
	@Inject 
	private int endTimeLine = 8000;
	public int getEndTimeLine() {return endTimeLine;}
	public void setEndTimeLine(int endTimeLine) {this.endTimeLine = endTimeLine;}
	
	
	LinkedBlockingQueue<OneTimeTask> queue = new LinkedBlockingQueue<OneTimeTask>();
	private static final Random rand = new Random();
	
	public static int randInt(int lowerb, int upperb) {
		int interval = upperb - lowerb;
		assert(interval > 0);
		return (int) (lowerb + rand.nextFloat() * interval);
	}
	
	public void schedule(OneTimeTask task) {
		schedule(task, randInt(startTimeLine, endTimeLine));
		
	}
	
	/**
	 * 
	 * @param task
	 * @param delay in mill sec
	 */
	public void schedule(OneTimeTask task, int delay) {
		
		if (delay < startTimeLine || delay > endTimeLine) {
			throw new IllegalArgumentException(
					" delay time["+delay+"] out of bound"
					+"  delay must between [" + startTimeLine + "]  and  [" + endTimeLine + "]");
		}
		
		
		
	}

}
