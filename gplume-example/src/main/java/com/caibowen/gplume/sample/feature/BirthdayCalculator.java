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
package com.caibowen.gplume.sample.feature;

import com.caibowen.gplume.event.IAppListener;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class BirthdayCalculator implements IAppListener<TimeChangedEvent> {

	Date time = new Date();
	
	// let's ignore leap years
	public int dateDistance(Date t1) {
		int d = (int) TimeUnit.DAYS.convert(t1.getTime() - time.getTime(), TimeUnit.MILLISECONDS);
		d %= 365;
		if (d < 0) {
			d += 365;
		}
		return d;
	}

	@Override
	public void onEvent(TimeChangedEvent event) {
		this.time = event.getTime();
	}
}
