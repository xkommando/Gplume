package com.caibowen.gplume.sample;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.caibowen.gplume.event.IAppListener;


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
