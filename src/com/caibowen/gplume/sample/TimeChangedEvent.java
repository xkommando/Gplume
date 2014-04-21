package com.caibowen.gplume.sample;

import java.util.Date;

import com.caibowen.gplume.event.AppEvent;

public class TimeChangedEvent extends AppEvent {

	private static final long serialVersionUID = -4793675731955435687L;

	Date time;
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public TimeChangedEvent(Object arg0) {
		super(arg0);
	}

}
