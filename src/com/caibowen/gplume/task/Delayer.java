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
